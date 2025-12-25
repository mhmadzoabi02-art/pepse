package pepse.world.trees;

import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.components.ScheduledTask;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;
import pepse.world.avatar.Avatar;

import java.awt.Color;


/**
 * A collectible fruit object. When the avatar collides with the fruit, the fruit grants
 * energy once, becomes invisible, and reappears after one day-night cycle.
 */
public class Fruit extends GameObject {
    private static final String FRUIT_TAG = "fruit";
    private static final Color FRUIT_COLOR = Color.RED;
    private static final float ENERGY_GAIN = 10f;
    private final float respawnTimeSeconds;

    /**
     * Constructs a fruit.
     *
     * @param topLeftCorner        fruit top-left position.
     * @param dimensions           fruit dimensions.
     * @param respawnTimeSeconds   time (in seconds) until the fruit becomes visible again after collection.
     */
    public Fruit(Vector2 topLeftCorner, Vector2 dimensions,float respawnTimeSeconds) {
        super(topLeftCorner, dimensions, new OvalRenderable(FRUIT_COLOR));
        setTag(FRUIT_TAG);
        this.respawnTimeSeconds = respawnTimeSeconds;
    }
    /**
     * Called when a collision begins. If colliding with the avatar and the fruit is visible,
     * grants energy, hides the fruit, and schedules it to reappear after {@link #respawnTimeSeconds}.
     *
     * @param other     the other object involved in the collision.
     * @param collision collision information provided by the engine.
     */
    @Override
    public void onCollisionEnter(GameObject other, Collision collision) {
        super.onCollisionEnter(other, collision);

        if (Avatar.AVATAR_TAG.equals(other.getTag()) && renderer().getOpaqueness() == 1f) {
            ((Avatar) other).addEnergy(ENERGY_GAIN);
            renderer().setOpaqueness(0f);

            new ScheduledTask(
                    this,
                    respawnTimeSeconds,
                    false,
                    () -> renderer().setOpaqueness(1f)
            );
        }
    }
}
