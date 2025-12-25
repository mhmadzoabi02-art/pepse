package pepse.world.infinite;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.collisions.Layer;
import pepse.world.Block;
import pepse.world.Terrain;
import pepse.world.trees.Flora;

import java.util.List;

import static pepse.world.trees.Flora.LEAF_TAG;
import static pepse.world.trees.Flora.TRUNK_TAG;

/**
 * Handles infinite-world generation by expanding terrain and flora around the avatar
 * as it moves. The generator maintains the range already created and extends it when
 * the avatar approaches the edges of the generated area.
 */
public class InfiniteWorldGenerator {
    /** Tag for fruit objects. */
    private static final String FRUIT_TAG = "fruit";
    private final GameObjectCollection gameObjects;
    private final Terrain terrain;
    private final Flora flora;
    private final int bufferPx;
    private int generatedMinX;
    private int generatedMaxX;
    /**
     * Constructs an infinite-world generator.
     *
     * @param gameObjects  world object collection to insert generated objects into.
     * @param terrain      terrain generator used for ground creation.
     * @param flora        flora generator used for vegetation creation.
     * @param initialMinX  initial left boundary already generated.
     * @param initialMaxX  initial right boundary already generated.
     * @param bufferPx     extra distance (pixels) to keep generated beyond the avatar position.
     */
    public InfiniteWorldGenerator(GameObjectCollection gameObjects,
                                  Terrain terrain,
                                  Flora flora,
                                  int initialMinX,
                                  int initialMaxX,
                                  int bufferPx){
        this.gameObjects = gameObjects;
        this.terrain = terrain;
        this.flora = flora;
        this.bufferPx = bufferPx;
        this.generatedMinX = snapDown(initialMinX, Block.SIZE);
        this.generatedMaxX = snapUp(initialMaxX, Block.SIZE);
    }
    /**
     * Updates the generated world range according to the avatar x-position.
     * If the avatar approaches beyond the existing range (minus buffer), extends the world
     * by generating terrain and flora for the missing range.
     *
     * @param avatarX current x coordinate of the avatar center.
     */
    public void update(float avatarX){
        int targetMin = snapDown((int) avatarX - bufferPx, Block.SIZE);
        int targetMax = snapUp((int) avatarX + bufferPx, Block.SIZE);

        // extend left
        if (targetMin < generatedMinX) {
            generateRange(targetMin, generatedMinX);
            generatedMinX = targetMin;
        }

        // extend right
        if (targetMax > generatedMaxX) {
            generateRange(generatedMaxX, targetMax);
            generatedMaxX = targetMax;
        }

    }
    private void generateRange(int minX, int maxX) {
        // Terrain
        for (Block b : terrain.createInRange(minX, maxX)) {
            gameObjects.addGameObject(b, Layer.STATIC_OBJECTS);
        }

        // Flora
        List<GameObject> objs = flora.createInRange(minX, maxX);
        for (GameObject obj : objs) {
            addFloraObject(obj);
        }
    }
    private void addFloraObject(GameObject obj) {
        String tag = obj.getTag();
        if (TRUNK_TAG.equals(tag)) {
            gameObjects.addGameObject(obj, Layer.STATIC_OBJECTS);
        } else if (LEAF_TAG.equals(tag)) {
            gameObjects.addGameObject(obj, Layer.STATIC_OBJECTS + 1);
        } else if (FRUIT_TAG.equals(tag)) {
            gameObjects.addGameObject(obj, Layer.DEFAULT);
        } else {
            gameObjects.addGameObject(obj, Layer.DEFAULT);
        }
    }

    private static int snapDown(int x, int size) {
        return Math.floorDiv(x, size) * size;
    }

    private static int snapUp(int x, int size) {
        int d = Math.floorDiv(x, size) * size;
        return (d == x) ? x : d + size;
    }
}
