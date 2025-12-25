package pepse.world;

import danogl.gui.rendering.RectangleRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;
import pepse.utils.ColorSupplier;
import pepse.utils.NoiseGenerator;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
/**
 * Responsible for generating and managing the ground terrain.
 * Terrain height is computed using Perlin-like noise (via {@link NoiseGenerator}) around a baseline
 * height at x=0, and the terrain is created as vertical columns of {@link Block}s.
 */
public class Terrain {
    private static final int TERRAIN_DEPTH = 20;
    private static final Color BASE_GROUND_COLOR = new Color(212, 123, 74);
    /** Tag used to identify ground blocks. */
    public static final String GROUND_TAG = "ground";
    private final float groundHeightAtX0;
    private final HashSet<Integer> generatedColumns= new HashSet<>();
    private final NoiseGenerator noiseGenerator;
    /**
     * Constructs a terrain generator.
     *
     * @param windowDimensions window dimensions used to compute the baseline ground height.
     * @param seed             fixed seed for deterministic terrain generation.
     */
    public Terrain(Vector2 windowDimensions, int seed){
        this.groundHeightAtX0=windowDimensions.y()*((float) 2 /3);
        this.noiseGenerator = new NoiseGenerator(seed,(int)groundHeightAtX0);
    }
    /**
     * Computes the ground height at the given x coordinate.
     * The returned height is the baseline height at x=0 plus a noise-based offset.
     *
     * @param x x coordinate in world units.
     * @return ground y coordinate at x.
     */
    public float groundHeightAt(float x){
        double noise=noiseGenerator.noise(x,Block.SIZE*7.0);
        return groundHeightAtX0+(float)noise;
    }
    /**
     * Creates terrain blocks in the requested x-range. Generation is snapped to the block grid,
     * and overlapping calls do not duplicate already-created columns.
     *
     * @param minX left boundary (inclusive) in world coordinates.
     * @param maxX right boundary (inclusive) in world coordinates.
     * @return list of newly created {@link Block}s in the requested range.
     */
    public List<Block> createInRange(int minX, int maxX){
       List<Block> blocks = new ArrayList<>();
       int size=Block.SIZE;
       int startX=(int) Math.floor((double) minX / size) * size;
       int endX=(int) Math.ceil((double) maxX / size) * size;
        for (int x = startX; x <= endX; x += size) {
            if(generatedColumns.contains(x)){
                continue;
            }
            generatedColumns.add(x);
            int topY = (int) (Math.floor(groundHeightAt(x) / size) * size);

            for (int i = 0; i < TERRAIN_DEPTH; i++) {
                int y = topY + i * size;

                Renderable renderable = new RectangleRenderable(
                        ColorSupplier.approximateColor(BASE_GROUND_COLOR)
                );

                Block block = new Block(new Vector2(x, y), renderable); // adjust if your ctor differs
                block.setTag(GROUND_TAG);
                blocks.add(block);
            }
        }
        return blocks;
    }
}
