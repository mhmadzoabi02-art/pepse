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
import pepse.world.daynight.Night;
import pepse.world.daynight.Sun;
import pepse.world.daynight.SunHalo;

public class PepseGameManager extends GameManager {
    private static final int SEED = 12745;
    public static void main(String[] args) {
        new PepseGameManager().run();
    }

    @Override
    public void initializeGame(ImageReader imageReader,
                               SoundReader soundReader,
                               UserInputListener inputListener,
                               WindowController windowController) {
        super.initializeGame(imageReader, soundReader, inputListener, windowController);
        var windowDimensions=windowController.getWindowDimensions();
        final float CYCLE_LENGTH=30f;

        //sky
        GameObject sky= Sky.create(windowDimensions);
        gameObjects().addGameObject(sky, Layer.BACKGROUND);

        //terrain
        Terrain terrain = new Terrain(windowDimensions, SEED);
        int minX=0;
        int maxX=(int)windowController.getWindowDimensions().x();
        for(Block b : terrain.createInRange(minX, maxX)){
            gameObjects().addGameObject(b, Layer.STATIC_OBJECTS);
        }

        // Sun + halo
        GameObject sun= Sun.create(windowDimensions,CYCLE_LENGTH);
        GameObject halo= SunHalo.create(sun);
        gameObjects().addGameObject(halo, Layer.DEFAULT-1);
        gameObjects().addGameObject(sun, Layer.DEFAULT);

        // Night overlay on top of everything
        GameObject night = Night.create(windowDimensions, CYCLE_LENGTH);
        gameObjects().addGameObject(night, Layer.FOREGROUND);


    }
}
