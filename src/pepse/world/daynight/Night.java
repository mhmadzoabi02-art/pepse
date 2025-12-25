package pepse.world.daynight;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.components.Transition;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;

import java.awt.*;
/**
 * Creates a fullscreen night overlay that fades in and out in a loop.
 * The overlay is rendered in camera coordinates so it stays fixed to the screen.
 */
public class Night {
    private static final Float MIDNIGHT_OPACITY = 0.5f;
    private static final String NIGHT_TAG = "night";

    /**
     * Creates a fullscreen overlay that transitions between day and night by changing its opacity.
     *
     * @param windowDimensions the window dimensions used to size the overlay.
     * @param cycleLength      full length of one day-night cycle in seconds.
     * @return a {@link GameObject} representing the night overlay.
     */
    public static GameObject create(Vector2 windowDimensions,float cycleLength) {
        GameObject night =new GameObject(Vector2.ZERO,windowDimensions,new RectangleRenderable(Color.BLACK));
        night.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        night.setTag(NIGHT_TAG);
        new Transition<>(night,
                night.renderer()::setOpaqueness,
                0f,
                MIDNIGHT_OPACITY,
                Transition.CUBIC_INTERPOLATOR_FLOAT,
                cycleLength/2f,
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                null);
        return night;
    }
}
