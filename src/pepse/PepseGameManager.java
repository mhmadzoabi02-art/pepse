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
    public static final  String FRUIT_TAG = "fruit";
    /** Tag used to identify the avatar object in collisions. */
    public static final String AVATAR_TAG = "avatar";
    public static final String TRUNK_TAG = "trunk";
    public static final String LEAF_TAG = "leaf";
    public static final String GROUND_TAG = "ground";
    /** cycle length */
    public static final float CYCLE_LENGTH = 30f;
    private static final int SEED = 12345;
    private static final float HALF =0.5f ;

    private static final int HALO_LAYER = Layer.BACKGROUND+2;
    private static final int SUN_LAYER = Layer.BACKGROUND+1;
    private static final int LEAF_LAYER = SUN_LAYER;

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
        int minX=0;
        int maxX=(int)windowController.getWindowDimensions().x();

        // Sky.
        createSky(windowDimensions);
        // Sun + halo
        createSunAndHalo(windowDimensions);
        //Terrain
        Terrain terrain = createTerrain(windowDimensions, minX, maxX);
        // Night overlay on top of everything
        createNight(windowDimensions);
        // Avatar
        this.avatar = createAvatar(windowDimensions, terrain, inputListener, imageReader);
        // Energy UI
        createEnergy(avatar);
        // Tree
        Flora flora = createFlora(terrain, minX, maxX);
        // Infinite world generator initialization
        this.worldGen = createInfiniteWorld(windowDimensions, terrain, flora);

    }

    private void createSky(Vector2 windowDimensions) {
        GameObject sky= Sky.create(windowDimensions);
        gameObjects().addGameObject(sky, Layer.BACKGROUND);
    }

    private void createSunAndHalo(Vector2 windowDimensions) {
        GameObject sun= Sun.create(windowDimensions,CYCLE_LENGTH);
        GameObject halo= SunHalo.create(sun);
        gameObjects().addGameObject(halo, HALO_LAYER);
        gameObjects().addGameObject(sun, SUN_LAYER);
    }

    private Terrain createTerrain(Vector2 windowDimensions, int minX, int maxX) {
        Terrain terrain = new Terrain(windowDimensions, SEED);
        for(Block b : terrain.createInRange(minX, maxX)){
            gameObjects().addGameObject(b, Layer.STATIC_OBJECTS);
        }
        return terrain;
    }

    private void createNight(Vector2 windowDimensions) {
        GameObject night = Night.create(windowDimensions, CYCLE_LENGTH);
        gameObjects().addGameObject(night, Layer.FOREGROUND);
    }

    private Avatar createAvatar(Vector2 windowDimensions,
                                Terrain terrain, UserInputListener inputListener,
                                ImageReader imageReader) {
        float initialX = (float)(Math.floor((windowDimensions.x()/2f) / Block.SIZE) * Block.SIZE);

        float halfW = Avatar.AVATAR_DIMENSIONS.x() / 2f;
        float leftX  = initialX - halfW;
        float rightX = initialX + halfW;

        float leftTopY  = snapDown(terrain.groundHeightAt(leftX), Block.SIZE);
        float midTopY   = snapDown(terrain.groundHeightAt(initialX), Block.SIZE);
        float rightTopY = snapDown(terrain.groundHeightAt(rightX), Block.SIZE);

        float groundTopY = Math.min(leftTopY, Math.min(midTopY, rightTopY));

        float initialY = groundTopY - Avatar.AVATAR_DIMENSIONS.y();
        Vector2 initialAvatarLocation = new Vector2(initialX, initialY);

        Avatar avatar = new Avatar(initialAvatarLocation, inputListener, imageReader);
        setCamera(new Camera(avatar,
                windowDimensions.mult(HALF).subtract(initialAvatarLocation)
                ,windowDimensions
                ,windowDimensions));
        gameObjects().addGameObject(avatar, Layer.DEFAULT);
        return avatar;
    }

    private void createEnergy(Avatar avatar) {
        GameObject energyDisplay = Energy.create(avatar::getEnergy);
        gameObjects().addGameObject(energyDisplay, Layer.UI);
    }

    private Flora createFlora(Terrain terrain, int minX, int maxX) {
        Flora flora = new Flora(terrain::groundHeightAt,SEED);
        java.util.List<GameObject> trees = flora.createInRange(minX, maxX);

        for (GameObject obj : trees) {
            String tag = obj.getTag();
            switch (tag) {
                case TRUNK_TAG:
                    gameObjects().addGameObject(obj, Layer.STATIC_OBJECTS);
                    break;
                case LEAF_TAG:
                    gameObjects().addGameObject(obj, LEAF_LAYER);
                    break;
                case FRUIT_TAG:
                    gameObjects().addGameObject(obj, Layer.DEFAULT);
                    break;
            }
        }
        return flora;
    }

    private pepse.world.infinite.InfiniteWorldGenerator
    createInfiniteWorld(Vector2 windowDimensions, Terrain terrain, Flora flora) {
        int initialMinX = 0;
        int initialMaxX = (int) windowDimensions.x();
        int bufferPx = (int) (2 * windowDimensions.x());

        return new pepse.world.infinite.InfiniteWorldGenerator(
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
        worldGen.update(avatar.getCenter().x());
    }
    private static float snapDown(float v, float size) {
        return (float) (Math.floor(v / size) * size);
    }
}
