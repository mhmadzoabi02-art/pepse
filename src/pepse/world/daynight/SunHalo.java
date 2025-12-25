package pepse.world.daynight;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;

import java.awt.*;
/**
 * Creates a halo effect around the sun.
 * The halo follows the sun by updating its center every frame.
 */
public class SunHalo {
    private static final Color HALO_COLOR=new Color(255,255,0,20);
    private static final float HALO_RADIUS=80f;
    private static final String HALO_TAG = "halo";
    /**
     * Creates a halo {@link GameObject} that tracks the provided sun object.
     *
     * @param sun the sun object whose center the halo should follow.
     * @return a {@link GameObject} representing the sun halo.
     */
    public static GameObject create(GameObject sun){
        GameObject sunHalo=new GameObject(
                Vector2.ZERO,
                Vector2.ONES.mult(HALO_RADIUS*2),
                new OvalRenderable(HALO_COLOR)
        );
        sunHalo.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        sunHalo.setCenter(sun.getCenter());
        sunHalo.setTag(HALO_TAG);
        sunHalo.addComponent(deltaTime -> sunHalo.setCenter(sun.getCenter()));
        return sunHalo;

    }
}
