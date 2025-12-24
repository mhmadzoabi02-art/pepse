package pepse.world.trees;

import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.components.ScheduledTask;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;
import pepse.world.avatar.Avatar;

import java.awt.Color;

public class Fruit extends GameObject {
    private static final String FRUIT_TAG = "fruit";
    private static final Color FRUIT_COLOR = Color.RED;
    private static final float CYCLE_LENGTH = 30f;
    private static final float ENERGY_GAIN = 10f;

    public Fruit(Vector2 topLeftCorner, Vector2 dimensions) {
        super(topLeftCorner, dimensions, new OvalRenderable(FRUIT_COLOR));
        setTag(FRUIT_TAG);
    }

    @Override
    public void onCollisionEnter(GameObject other, Collision collision) {
        super.onCollisionEnter(other, collision);

        if (other.getTag().equals("avatar") && renderer().getOpaqueness() == 1f) {


            ((Avatar) other).addEnergy(ENERGY_GAIN);


            renderer().setOpaqueness(0f);

            new ScheduledTask(
                    this,
                    CYCLE_LENGTH,
                    false,
                    () -> renderer().setOpaqueness(1f)
            );
        }
    }
}