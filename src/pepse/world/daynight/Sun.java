package pepse.world.daynight;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.components.Transition;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;

import java.awt.*;
/**
 * Creates the sun object and animates it in a circular path to simulate a day cycle.
 * The sun is rendered in camera coordinates so its motion is relative to the screen.
 */
public class Sun {
    private static final float SUN_RADIUS = 50f;
    private static final String SUN_TAG = "sun";
    /**
     * Creates a sun {@link GameObject} that moves in a circular trajectory over time.
     *
     * @param windowDimensions window size used to compute the cycle center and initial position.
     * @param cycleLength      full length of one sun cycle in seconds.
     * @return a {@link GameObject} representing the sun.
     */
    public static GameObject create(Vector2 windowDimensions,float cycleLength){
        GameObject sun=new GameObject(
                Vector2.ZERO,
                Vector2.ONES.mult(SUN_RADIUS*2),
                new OvalRenderable(Color.YELLOW));
        sun.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        sun.setTag(SUN_TAG);
        // groundHeightAtX0 is defined in the Terrain section as ~ 2/3 of the window height
        float groundHeightAtX0 = windowDimensions.y() * 2f / 3f;
        Vector2 cycleCenter=new Vector2(windowDimensions.x()/2f,groundHeightAtX0);
        Vector2 initialSunCenter = new Vector2(windowDimensions.x()/2f,
                groundHeightAtX0/2f);
        sun.setCenter(initialSunCenter);
        new Transition<>(sun,
                (Float angle)-> sun.setCenter(
                        initialSunCenter.subtract(cycleCenter)
                                .rotated(angle)
                                .add(cycleCenter)
                        ),
                0f,
                360f,
                Transition.LINEAR_INTERPOLATOR_FLOAT,
                cycleLength,
                Transition.TransitionType.TRANSITION_LOOP,
                null);
        return sun;
    }
}
