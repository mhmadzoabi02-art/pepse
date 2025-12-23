package pepse.world;

import danogl.gui.rendering.RectangleRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;
import pepse.utils.ColorSupplier;
import pepse.utils.NoiseGenerator;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Terrain {
    private static final int TERRAIN_DEPTH = 20;
    private static final Color BASE_GROUND_COLOR = new Color(212, 123, 74);
    private final float groundHeightAtX0;
    private final NoiseGenerator noiseGenerator;
    public Terrain(Vector2 windowDimensions, int seed){
        this.groundHeightAtX0=windowDimensions.y()*((float) 2 /3);
        this.noiseGenerator = new NoiseGenerator(seed,(int)groundHeightAtX0);
    }
    public float groundHeightAt(float x){
        double noise=noiseGenerator.noise(x,Block.SIZE*7.0);
        return groundHeightAtX0+(float)noise;
    }
    public List<Block> createInRange(int minX, int maxX){
       List<Block> blocks = new ArrayList<>();
       int size=Block.SIZE;
       int startX=(int) Math.floor((double) minX / size) * size;
       int endX=(int) Math.ceil((double) maxX / size) * size;
        for (int x = startX; x <= endX; x += size) {
            // snap the ground height to the block grid
            int topY = (int) (Math.floor(groundHeightAt(x) / size) * size);

            for (int i = 0; i < TERRAIN_DEPTH; i++) {
                int y = topY + i * size;

                Renderable renderable = new RectangleRenderable(
                        ColorSupplier.approximateColor(BASE_GROUND_COLOR)
                );

                Block block = new Block(new Vector2(x, y), renderable); // adjust if your ctor differs
                block.setTag("ground");
                blocks.add(block);
            }
        }
        return blocks;



    }
}
