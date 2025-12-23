package pepse;

import danogl.GameManager;
import danogl.GameObject;
import danogl.collisions.Layer;
import danogl.gui.ImageReader;
import danogl.gui.SoundReader;
import danogl.gui.UserInputListener;
import danogl.gui.WindowController;
import pepse.world.Block;
import pepse.world.Sky;
import pepse.world.Terrain;

public class PepseGameManager extends GameManager {
    private static final int SEED = 12345;
    public static void main(String[] args) {
        new PepseGameManager().run();
    }

    @Override
    public void initializeGame(ImageReader imageReader, SoundReader soundReader, UserInputListener inputListener, WindowController windowController) {
        super.initializeGame(imageReader, soundReader, inputListener, windowController);
        gameObjects().addGameObject(Sky.create(windowController.getWindowDimensions()), Layer.STATIC_OBJECTS);
        Terrain terrain = new Terrain(windowController.getWindowDimensions(), SEED);
        int minX=0;
        int maxX=(int)windowController.getWindowDimensions().x();
        for(Block b : terrain.createInRange(minX, maxX)){
            gameObjects().addGameObject(b, Layer.STATIC_OBJECTS);
        }
    }
}
