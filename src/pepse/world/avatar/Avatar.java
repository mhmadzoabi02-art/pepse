package pepse.world.avatar;

import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.gui.ImageReader;
import danogl.gui.UserInputListener;
import danogl.gui.rendering.AnimationRenderable;
import danogl.util.Vector2;
import pepse.world.trees.Flora;

import java.awt.event.KeyEvent;

import static pepse.world.Terrain.GROUND_TAG;


enum AvatarState {
    IDLE,
    RUN,
    JUMP
}
/**
 * Represents the player-controlled avatar.
 * The avatar supports horizontal movement, jumping (including a conditional double-jump),
 * and an energy resource that is consumed by actions and regenerated while idle.
 */
public class Avatar extends GameObject {
    private static final float VELOCITY_X = 400;
    private static final float JUMP_VELOCITY_Y = -600;
    private static final float GRAVITY = 600;
    /** Avatar dimensions in pixels. */
    public static final Vector2 AVATAR_DIMENSIONS = new Vector2(50, 50);

    private static final float MAX_ENERGY = 100f;
    private static final float RUN_ENERGY_COST = 0.5f;
    private static final float JUMP_ENERGY_COST = 20f;
    private static final float DOUBLE_JUMP_ENERGY_COST = 50f;
    private static final float IDLE_ENERGY_GAIN = 1f;
    private static final float STAND_NORMAL_Y = -0.9f;   // must be strongly upward
    private static final float STAND_MAX_ABS_X = 0.2f;
    private static final float WALL_NORMAL_X = 0.8f;   // strong side push
    private static final float WALL_MAX_ABS_Y = 0.2f;
    // tune these if needed
    private static final float STAND_NY = -0.7f;     // must be strongly "from above"
    private static final float STAND_MAX_NX = 0.25f; // almost no sideways normal

    private static final float WALL_NX = 0.7f;       // strongly from side
    private static final float WALL_MAX_NY = 0.25f;




    private static final float TIME_BETWEEN_CLIPS = 0.2f;
    /** Tag used to identify the avatar object in collisions. */
    public static final String AVATAR_TAG = "avatar";

    private static final String[] IDLE_PATHS = {
            "assets/idle_0.png", "assets/idle_1.png", "assets/idle_2.png", "assets/idle_3.png"
    };
    private static final String[] JUMP_PATHS = {
            "assets/jump_0.png", "assets/jump_1.png", "assets/jump_2.png", "assets/jump_3.png"
    };
    private static final String[] RUN_PATHS = {
            "assets/run_0.png", "assets/run_1.png", "assets/run_2.png",
            "assets/run_3.png", "assets/run_4.png", "assets/run_5.png"
    };

    private float energy = MAX_ENERGY;
    private AvatarState state = AvatarState.IDLE;
    private final UserInputListener inputListener;

    // Renderables for animations
    private final AnimationRenderable idleAnimation;
    private final AnimationRenderable runAnimation;
    private final AnimationRenderable jumpAnimation;
    private boolean isOnGround = false;
    private float prevBottomY = 0f;
    private boolean onGround = false;
    private static final float GROUND_EPS_VY = 0.5f; // tolerance for "standing"
    private boolean grounded = false;
    private static final float LANDING_EPS = 1f;
    private static final float GROUND_NORMAL_THRESHOLD = -0.5f; // collision from above
    private static final String TRUNK_TAG = "trunk";
    private boolean touchingWallLeft = false;
    private boolean touchingWallRight = false;
    private static final float VX_EPS = 0.5f; // treat tiny vx as 0

    private boolean spaceWasDown = false;
    private boolean doubleJumpUsed = false;




    /**
     * Constructs a new avatar.
     *
     * @param topLeftCorner initial top-left position of the avatar.
     * @param inputListener input listener used to read movement/jump keys.
     * @param imageReader   image reader used to load animation frames.
     */
    public Avatar(Vector2 topLeftCorner, UserInputListener inputListener,
                  ImageReader imageReader) {
        super(topLeftCorner, AVATAR_DIMENSIONS,
                imageReader.readImage(IDLE_PATHS[0], true));
        this.inputListener = inputListener;
        setTag(AVATAR_TAG);

        this.idleAnimation = new AnimationRenderable(IDLE_PATHS,
                imageReader, true, TIME_BETWEEN_CLIPS);
        this.runAnimation = new AnimationRenderable(RUN_PATHS,
                imageReader, true, TIME_BETWEEN_CLIPS);
        this.jumpAnimation = new AnimationRenderable(JUMP_PATHS,
                imageReader, true, TIME_BETWEEN_CLIPS);

        physics().preventIntersectionsFromDirection(Vector2.ZERO);
        transform().setAccelerationY(GRAVITY);
    }
    /**
     * Updates avatar movement, jumping, animation state and energy.
     *
     * @param deltaTime time (in seconds) since the last frame.
     */
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        // Grounded from LAST physics step (set in onCollisionStay/Enter)
        boolean onGroundNow = grounded || Math.abs(getVelocity().y()) < GROUND_EPS_VY;

        // Reset double jump when actually on ground
        if (onGroundNow) {
            doubleJumpUsed = false;
        }

        AvatarState prevState = state;

        boolean leftDown  = inputListener.isKeyPressed(KeyEvent.VK_LEFT);
        boolean rightDown = inputListener.isKeyPressed(KeyEvent.VK_RIGHT);

        float xVel = 0f;

        // wall info from last collision step
        boolean blockLeft  = touchingWallLeft;
        boolean blockRight = touchingWallRight;

        if (leftDown ^ rightDown) { // exactly one is pressed
            if (leftDown && !blockLeft && (!onGroundNow || energy >= RUN_ENERGY_COST)) {
                xVel = -VELOCITY_X;
                renderer().setIsFlippedHorizontally(true);
            } else if (rightDown && !blockRight && (!onGroundNow || energy >= RUN_ENERGY_COST)) {
                xVel = VELOCITY_X;
                renderer().setIsFlippedHorizontally(false);
            }
        }
        // else: none or both -> stay 0

        transform().setVelocityX(xVel);

        // Jump "just pressed"
        boolean spaceDown = inputListener.isKeyPressed(KeyEvent.VK_SPACE);
        boolean spaceJustPressed = spaceDown && !spaceWasDown;
        spaceWasDown = spaceDown;

        if (spaceJustPressed) {
            if (onGroundNow) {
                if (energy >= JUMP_ENERGY_COST) {
                    transform().setVelocityY(JUMP_VELOCITY_Y);
                    energy -= JUMP_ENERGY_COST;
                }
            } else if (!doubleJumpUsed && getVelocity().y() > 0 && energy >= DOUBLE_JUMP_ENERGY_COST) {
                transform().setVelocityY(JUMP_VELOCITY_Y);
                energy -= DOUBLE_JUMP_ENERGY_COST;
                doubleJumpUsed = true;
            }
        }

        // State depends on INPUT xVel (prevents "running in place")
        if (!onGroundNow) state = AvatarState.JUMP;
        else if (xVel != 0f) state = AvatarState.RUN;
        else state = AvatarState.IDLE;

        if (state != prevState) updateAnimation();

        // Energy depends on INPUT xVel too
        if (onGroundNow && xVel == 0f) {
            energy = Math.min(MAX_ENERGY, energy + IDLE_ENERGY_GAIN);
        } else if (onGroundNow && xVel != 0f) {
            energy = Math.max(0f, energy - RUN_ENERGY_COST);
        }

        // Clear flags for next physics step (collisions will set them)
        grounded = false;
        touchingWallLeft = false;
        touchingWallRight = false;
    }






    private void updateAnimation() {
        switch (state) {
            case IDLE:
                renderer().setRenderable(idleAnimation);
                break;
            case RUN:
                renderer().setRenderable(runAnimation);
                break;
            case JUMP:
                renderer().setRenderable(jumpAnimation);
                break;
        }
    }

    private void updateEnergy() {
        switch (state) {
            case IDLE:
                if (energy < MAX_ENERGY) {
                    energy += IDLE_ENERGY_GAIN;
                }
                break;
            case RUN:
                if (energy > 0) {
                    energy -= RUN_ENERGY_COST;
                }
                break;
            case JUMP:
                break;
        }

        // Asserting energy is in range
        energy = Math.min(energy, MAX_ENERGY);
        energy = Math.max(energy, 0f);
    }
    /**
     * Returns the current energy value of the avatar.
     *
     * @return current energy in range [0, MAX_ENERGY].
     */
    public float getEnergy() {
        return energy;
    }
    /**
     * Adds energy to the avatar, clamped by {@link #MAX_ENERGY}.
     *
     * @param amount amount of energy to add (can be negative if needed).
     */
    public void addEnergy(float amount) {
        this.energy += amount;
        // Assert energy is in range
        this.energy = Math.min(this.energy, MAX_ENERGY);
    }
    /**
     * Called when a collision begins. If colliding with ground, ensures the avatar does not
     * continue sinking downward by zeroing the vertical velocity.
     *
     * @param other     the other object involved in the collision.
     * @param collision collision information provided by the engine.
     */
    @Override
    public void onCollisionEnter(GameObject other, Collision collision) {
        super.onCollisionEnter(other, collision);
        handleStandCollision(other, collision);
        handleWallCollision(other, collision);
    }

    @Override
    public void onCollisionStay(GameObject other, Collision collision) {
        super.onCollisionStay(other, collision);
        handleStandCollision(other, collision);
        handleWallCollision(other, collision);
    }


    private void handleStandCollision(GameObject other, Collision collision) {
        String tag = other.getTag();
        boolean solidSurface = GROUND_TAG.equals(tag) || Flora.TRUNK_TAG.equals(tag);
        if (!solidSurface) return;

        float ny = collision.getNormal().y();
        float nx = collision.getNormal().x();

        // Standing only if mostly vertical AND we are not moving up
        if (ny < STAND_NY && Math.abs(nx) < STAND_MAX_NX && getVelocity().y() >= 0) {
            grounded = true;
            if (getVelocity().y() > 0) {
                transform().setVelocityY(0);
            }
        }
    }




    private void handleWallCollision(GameObject other, Collision collision) {
        if (!Flora.TRUNK_TAG.equals(other.getTag())) return;

        float nx = collision.getNormal().x();
        float ny = collision.getNormal().y();

        if (Math.abs(nx) > WALL_NX && Math.abs(ny) < WALL_MAX_NY) {
            if (nx > 0) touchingWallLeft = true;
            else        touchingWallRight = true;
        }
    }


}
