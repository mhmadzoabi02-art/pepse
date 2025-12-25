package pepse.world.avatar;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.TextRenderable;
import danogl.util.Vector2;
import java.awt.Color;
import java.util.function.Supplier;
/**
 * Utility class responsible for creating an on-screen energy display for the avatar.
 * The created {@link GameObject} updates its text every frame according to the supplied energy function.
 */
public class Energy {
    private static final Vector2 TEXT_POSITION = new Vector2(20, 20);
    private static final Vector2 TEXT_DIMENSIONS = new Vector2(100, 30);
    private static final String ENERGY_UI_TAG = "energyDisplay";
    /**
     * Creates a UI {@link GameObject} that displays the current energy as a percentage.
     * The display is placed in camera coordinates so it remains fixed on the screen.
     *
     * @param energyFunction supplier that returns the current energy value (expected in [0, 100]).
     * @return a {@link GameObject} that renders the energy value and updates it every frame.
     */
    public static GameObject create(Supplier<Float> energyFunction) {
        TextRenderable textRenderable = new TextRenderable("100%");
        textRenderable.setColor(Color.WHITE); // Or any visible color

        GameObject energyDisplay = new GameObject(
                TEXT_POSITION,
                TEXT_DIMENSIONS,
                textRenderable
        );

        energyDisplay.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);

        energyDisplay.setTag(ENERGY_UI_TAG);

        energyDisplay.addComponent(deltaTime -> {
            float currentEnergy = energyFunction.get();

            textRenderable.setString(String.format("%.0f%%", currentEnergy));
        });

        return energyDisplay;
    }
}
