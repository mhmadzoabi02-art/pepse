package pepse.world.daynight;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;

import java.awt.*;

public class SunHalo {
    private static final Color HALO_COLOR=new Color(255,255,0,20);
    private static final float HALO_RADIUS=80f;
    private static final String HALO_TAG = "halo";

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
