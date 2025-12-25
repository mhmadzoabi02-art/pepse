package pepse.world.trees;

import danogl.GameObject;
import danogl.components.ScheduledTask;
import danogl.components.Transition;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;
import pepse.utils.ColorSupplier;
import pepse.world.Block;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
/**
 * Procedurally generates trees (trunks, leaves and fruits) in a given x-range.
 * Generation is deterministic per x-coordinate using a fixed seed, and repeated calls
 * with overlapping ranges will not duplicate trees.
 */
public class Flora {
    private static final Color TRUNK_COLOR = new Color(100, 50, 20);
    private static final Color LEAF_COLOR = new Color(50, 200, 30);

    public static final String TRUNK_TAG = "trunk";
    public static final String LEAF_TAG = "leaf";


    private static final int MIN_TREE_HEIGHT = 4;
    private static final int MAX_TREE_HEIGHT = 12;

    private static final int TREE_RADIUS = 2;
    private static final float TREE_APPEARANCE_PROBABILITY = 0.1f;
    private static final float LEAF_APPEARANCE_PROBABILITY = 0.3f;
    private static final float FRUIT_APPEARANCE_PROBABILITY = 0.1f;


    private static final float FOLIAGE_TRANSITION_TIME = 2f;
    private static final float MAX_WIND_ANGLE = 10f;
    private static final float MAX_WIND_WIDTH_CHANGE = 2f;
    private static final float FRUIT_RESPAWN_TIME = 30f;

    private final Function<Float, Float> groundHeightFunc;
    private final int seed;
    private final java.util.HashSet<Integer> generatedTreeXs = new java.util.HashSet<>();

    /**
     * Constructs a flora generator.
     *
     * @param groundHeightFunc function providing the ground height at a given x.
     * @param seed             fixed seed for deterministic generation.
     */
    public Flora(Function<Float, Float> groundHeightFunc, int seed) {
        this.groundHeightFunc = groundHeightFunc;
        this.seed = seed;
    }
    /**
     * Creates flora objects in the given x-range.
     * The returned list includes trunks, leaves and fruits (as {@link GameObject}s).
     *
     * @param minX left boundary (inclusive) in world coordinates.
     * @param maxX right boundary (inclusive) in world coordinates.
     * @return list of generated flora objects in the requested range.
     */
    public List<GameObject> createInRange(int minX, int maxX) {
        List<GameObject> treeParts = new ArrayList<>();
        int startX = (minX / Block.SIZE) * Block.SIZE;
        int endX = (maxX / Block.SIZE) * Block.SIZE;

        for (int x = startX; x <= endX; x += Block.SIZE) {
            if (generatedTreeXs.contains(x)) continue;
            generatedTreeXs.add(x);
            Random r = new Random(java.util.Objects.hash(x, seed));
            if (r.nextFloat() < TREE_APPEARANCE_PROBABILITY) createTree(treeParts, x, r);
        }
        return treeParts;
    }

    private void createTree(List<GameObject> treeParts, int x, Random r) {
        float groundHeight = groundHeightFunc.apply((float) x);
        int groundY = (int) (Math.floor(groundHeight / Block.SIZE) * Block.SIZE);
        int treeHeight = r.nextInt(MAX_TREE_HEIGHT - MIN_TREE_HEIGHT) + MIN_TREE_HEIGHT;
        int trunkTopY = groundY - (treeHeight * Block.SIZE);

        for (int i = 0; i < treeHeight; i++) {
            int blockY = groundY - (i * Block.SIZE) - Block.SIZE;
            GameObject trunkBlock = new Block(
                    new Vector2(x, blockY),
                    new RectangleRenderable(ColorSupplier.approximateColor(TRUNK_COLOR))
            );
            trunkBlock.setTag(TRUNK_TAG);
            treeParts.add(trunkBlock);
        }

        int radius = TREE_RADIUS;
        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                int leafX = x + (i * Block.SIZE);
                int leafY = trunkTopY + (j * Block.SIZE);

                if (r.nextFloat() > LEAF_APPEARANCE_PROBABILITY) {
                    GameObject leaf = new Block(
                            new Vector2(leafX, leafY),
                            new RectangleRenderable(ColorSupplier.approximateColor(LEAF_COLOR))
                    );

                    leaf.setTag(LEAF_TAG);
                    leaf.physics().setMass(0);
                    leaf.physics().preventIntersectionsFromDirection(null);
                    animateLeaf(leaf,r);

                    treeParts.add(leaf);
                }

                if (r.nextFloat() < FRUIT_APPEARANCE_PROBABILITY) {
                    GameObject fruit = new Fruit(
                            new Vector2(leafX, leafY),
                            new Vector2(Block.SIZE, Block.SIZE),
                            FRUIT_RESPAWN_TIME
                    );

                    fruit.physics().setMass(0);
                    fruit.physics().preventIntersectionsFromDirection(null);

                    treeParts.add(fruit);
                }
            }
        }
    }

    private void animateLeaf(GameObject leaf,Random r) {
        float waitTime = r.nextFloat() * 5;

        new ScheduledTask(
                leaf,
                waitTime,
                false,
                () -> {
                    createAngleTransition(leaf);
                    createDimensionsTransition(leaf);
                }
        );
    }

    private void createAngleTransition(GameObject leaf) {
        new Transition<>(
                leaf,
                leaf.renderer()::setRenderableAngle,
                -MAX_WIND_ANGLE,
                MAX_WIND_ANGLE,
                Transition.LINEAR_INTERPOLATOR_FLOAT,
                FOLIAGE_TRANSITION_TIME,
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                null
        );
    }

    private void createDimensionsTransition(GameObject leaf) {
        Vector2 originalDimensions = leaf.getDimensions();

        Vector2 targetDimensions = new Vector2(
                originalDimensions.x() + MAX_WIND_WIDTH_CHANGE,
                originalDimensions.y()
        );

        new Transition<>(
                leaf,
                leaf::setDimensions,
                originalDimensions,
                targetDimensions,
                Transition.LINEAR_INTERPOLATOR_VECTOR,
                FOLIAGE_TRANSITION_TIME,
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                null
        );
    }
}
