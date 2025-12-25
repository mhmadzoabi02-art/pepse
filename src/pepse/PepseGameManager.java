package pepse;

import danogl.GameManager;
import danogl.GameObject;
import danogl.collisions.Layer;
import danogl.gui.ImageReader;
import danogl.gui.SoundReader;
import danogl.gui.UserInputListener;
import danogl.gui.WindowController;
import danogl.gui.rendering.Camera;
import danogl.util.Vector2;
import pepse.world.Block;
import pepse.world.Sky;
import pepse.world.Terrain;
import pepse.world.avatar.Avatar;
import pepse.world.avatar.Energy;
import pepse.world.daynight.Night;
import pepse.world.daynight.Sun;
import pepse.world.daynight.SunHalo;
import pepse.world.trees.Flora;


/**
 * Main game manager for the Pepse project.
 * Responsible for initializing all world elements (sky, terrain, day-night cycle, avatar, flora)
 * and maintaining the infinite-world generation during runtime.
 */
public class PepseGameManager extends GameManager {
    /** cycle length */
    public static final float CYCLE_LENGTH = 30f;
    private static final int SEED = 12345;
    private pepse.world.infinite.InfiniteWorldGenerator worldGen;
    private Avatar avatar;


    /**
     * Program entry point.
     * @param args command-line arguments (unused).
     */
    public static void main(String[] args) {
        new PepseGameManager().run();
    }
    /**
     * Initializes the game world: creates sky, terrain, sun/halo, night overlay, avatar, energy UI,
     * flora and the infinite-world generator. Also sets the camera to follow the avatar.
     *
     * @param imageReader       reader for loading image assets.
     * @param soundReader       reader for loading sound assets.
     * @param inputListener     keyboard input listener.
     * @param windowController  window controller providing window dimensions and window operations.
     */
    @Override
    public void initializeGame(ImageReader imageReader,
                               SoundReader soundReader,
                               UserInputListener inputListener,
                               WindowController windowController) {
        super.initializeGame(imageReader, soundReader, inputListener, windowController);
        var windowDimensions=windowController.getWindowDimensions();

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

        // Avatar
        float initialX = windowDimensions.x() / 2;
        float initialY = terrain.groundHeightAt(initialX) - Avatar.AVATAR_DIMENSIONS.y();
        Vector2 initialAvatarLocation = new Vector2(initialX, initialY);

         this.avatar = new Avatar(initialAvatarLocation, inputListener, imageReader);
        setCamera(new Camera(avatar,
                windowDimensions.mult(0.5f).subtract(initialAvatarLocation)
                ,windowDimensions
                ,windowDimensions));
        gameObjects().addGameObject(avatar, Layer.DEFAULT);


        // Energy UI
        GameObject energyDisplay = Energy.create(avatar::getEnergy);
        gameObjects().addGameObject(energyDisplay, Layer.UI);

        // Tree
        Flora flora = new Flora(terrain::groundHeightAt,SEED);
        java.util.List<GameObject> trees = flora.createInRange(minX, maxX);

        for (GameObject obj : trees) {
            String tag = obj.getTag();
            switch (tag) {
                case "trunk":
                    gameObjects().addGameObject(obj, Layer.STATIC_OBJECTS);
                    break;
                case "leaf":
                    gameObjects().addGameObject(obj, Layer.STATIC_OBJECTS + 1);
                    break;
                case "fruit":
                    gameObjects().addGameObject(obj, Layer.DEFAULT);
                    break;
            }
        }
        // Infinite world generator initialization
        int initialMinX = 0;
        int initialMaxX = (int) windowDimensions.x();
        int bufferPx = (int) (2 * windowDimensions.x());

        worldGen = new pepse.world.infinite.InfiniteWorldGenerator(
                gameObjects(),
                terrain,
                flora,
                initialMinX,
                initialMaxX,
                bufferPx
        );
    }

    /**
     * Per-frame update: delegates to the infinite-world generator to expand the world
     * around the avatar as it moves.
     *
     * @param deltaTime elapsed time since last frame.
     */
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        worldGen.update(avatar.getCenter().x()); // store avatar as a field
    }

}
