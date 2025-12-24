package pepse.world.avatar;

import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.gui.ImageReader;
import danogl.gui.UserInputListener;
import danogl.gui.rendering.AnimationRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

import java.awt.event.KeyEvent;

// Enum defined for Section 6.3
enum AvatarState {
    IDLE,
    RUN,
    JUMP
}

public class Avatar extends GameObject {
    private static final float VELOCITY_X = 400;
    private static final float JUMP_VELOCITY_Y = -600;
    private static final float GRAVITY = 600;
    public static final Vector2 AVATAR_DIMENSIONS = new Vector2(50, 50);

    private static final float MAX_ENERGY = 100f;
    private static final float RUN_ENERGY_COST = 0.5f;
    private static final float JUMP_ENERGY_COST = 20f;
    private static final float DOUBLE_JUMP_ENERGY_COST = 50f;
    private static final float IDLE_ENERGY_GAIN = 1f;

    private static final float TIME_BETWEEN_CLIPS = 0.2f;

    private static final String GROUND_TAG = "ground";
    private static final String AVATAR_TAG = "avatar";

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

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        AvatarState prevState = state;

        // Vertical velocity is zero iff Avatar is on the ground
        boolean onGround = getVelocity().y() == 0;
        float xVel = 0;

        if (inputListener.isKeyPressed(KeyEvent.VK_LEFT)) {
            if (!onGround || energy >= RUN_ENERGY_COST) {
                xVel -= VELOCITY_X;
                renderer().setIsFlippedHorizontally(true);
            }
        }

        if (inputListener.isKeyPressed(KeyEvent.VK_RIGHT)) {
            if (!onGround || energy >= RUN_ENERGY_COST) {
                xVel += VELOCITY_X;
                renderer().setIsFlippedHorizontally(false);
            }
        }

        if (inputListener.isKeyPressed(KeyEvent.VK_LEFT) &&
                inputListener.isKeyPressed(KeyEvent.VK_RIGHT)) {
            xVel = 0;
        }

        transform().setVelocityX(xVel);

        if (inputListener.isKeyPressed(KeyEvent.VK_SPACE)) {
            if (onGround) {
                if (energy >= JUMP_ENERGY_COST) {
                    transform().setVelocityY(JUMP_VELOCITY_Y);
                    energy -= JUMP_ENERGY_COST;
                }
            } else {
                if (getVelocity().y() > 0 && energy >= DOUBLE_JUMP_ENERGY_COST) {
                    transform().setVelocityY(JUMP_VELOCITY_Y);
                    energy -= DOUBLE_JUMP_ENERGY_COST;
                }
            }
        }

        if (!onGround) {
            state = AvatarState.JUMP;
        } else if (getVelocity().x() != 0) {
            state = AvatarState.RUN;
        } else {
            state = AvatarState.IDLE;
        }

        if (state != prevState) {
            updateAnimation();
        }

        updateEnergy();
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

    public float getEnergy() {
        return energy;
    }

    public void addEnergy(float amount) {
        this.energy += amount;
        // Assert energy is in range
        this.energy = Math.min(this.energy, MAX_ENERGY);
    }

    @Override
    public void onCollisionEnter(GameObject other, Collision collision) {
        super.onCollisionEnter(other, collision);

        if(other.getTag().equals(GROUND_TAG)) {
            transform().setVelocityY(0);
        }
    }
}