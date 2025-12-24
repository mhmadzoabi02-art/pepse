package pepse.world.daynight;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.components.Transition;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;

import java.awt.*;

public class Sun {
    private static final float SUN_RADIUS = 50f;
    private static final String SUN_TAG = "sun";
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
