package pepse.world;

import danogl.GameObject;
import danogl.components.GameObjectPhysics;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;
/**
 * A single square block used to build the terrain and other grid-based world elements.
 * Blocks are immovable and prevent intersections (act as solid tiles).
 */
public class Block extends GameObject {
    /** Size of a block edge in pixels. */
    public static final int SIZE=30;
    /**
     * Constructs an immovable block at the given position.
     *
     * @param topLeftCorner top-left position of the block in world coordinates.
     * @param renderable    renderable used to draw the block.
     */
    public Block(Vector2 topLeftCorner, Renderable renderable) {
        super(topLeftCorner, Vector2.ONES.mult(SIZE), renderable);
        physics().preventIntersectionsFromDirection(Vector2.ZERO);
        physics().setMass(GameObjectPhysics.IMMOVABLE_MASS);

    }
}
