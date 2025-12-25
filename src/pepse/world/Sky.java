package pepse.world;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;

import java.awt.*;
/**
 * Utility class responsible for creating the sky background object.
 * The sky is rendered in camera coordinates so it always fills the screen.
 */
public class Sky  {
    private static final String SKY_TAG = "sky";
    private static final Color BASIC_SKY_COLOR = Color.decode("#80C6E5");
    /**
     * Creates a fullscreen sky {@link GameObject}.
     *
     * @param windowDimensions window dimensions used to size the sky object.
     * @return a {@link GameObject} representing the sky background.
     */
    public static GameObject create(Vector2 windowDimensions){
        GameObject sky=new GameObject(Vector2.ZERO,windowDimensions,new RectangleRenderable(BASIC_SKY_COLOR));
        sky.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        sky.setTag(SKY_TAG);
        return sky;
    }

}
