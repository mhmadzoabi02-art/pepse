package pepse.world.infinite;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.collisions.Layer;
import pepse.world.Block;
import pepse.world.Terrain;
import pepse.world.trees.Flora;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static pepse.PepseGameManager.*;


/**
 * Handles infinite-world generation by expanding terrain and flora around the avatar
 * as it moves. The generator maintains the range already created and extends it when
 * the avatar approaches the edges of the generated area.
 */
public class InfiniteWorldGenerator {
    private final Map<Integer, List<Spawned>> spawnedByColumnX = new HashMap<>();

    /** Tag for fruit objects. */
    private final GameObjectCollection gameObjects;
    private final Terrain terrain;
    private final Flora flora;
    private final int bufferPx;
    private int generatedMinX;
    private int generatedMaxX;
    /**
     * Constructs an infinite-world generator.
     *
     * @param gameObjects  world object collection to insert generated objects into.
     * @param terrain      terrain generator used for ground creation.
     * @param flora        flora generator used for vegetation creation.
     * @param initialMinX  initial left boundary already generated.
     * @param initialMaxX  initial right boundary already generated.
     * @param bufferPx     extra distance (pixels) to keep generated beyond the avatar position.
     */
    public InfiniteWorldGenerator(GameObjectCollection gameObjects,
                                  Terrain terrain,
                                  Flora flora,
                                  int initialMinX,
                                  int initialMaxX,
                                  int bufferPx){
        this.gameObjects = gameObjects;
        this.terrain = terrain;
        this.flora = flora;
        this.bufferPx = bufferPx;
        this.generatedMinX = snapDown(initialMinX, Block.SIZE);
        this.generatedMaxX = snapUp(initialMaxX, Block.SIZE);
    }
    /**
     * Updates the generated world range according to the avatar x-position.
     * If the avatar approaches beyond the existing range (minus buffer), extends the world
     * by generating terrain and flora for the missing range.
     *
     * @param avatarX current x coordinate of the avatar center.
     */
    public void update(float avatarX) {
        int targetMin = snapDown((int) avatarX - bufferPx, Block.SIZE);
        int targetMax = snapUp((int) avatarX + bufferPx, Block.SIZE);

        // 1) Fill any missing columns inside the current target window
        generateRange(targetMin, targetMax);

        // 2) Remove everything outside the window
        pruneOutside(targetMin, targetMax);

        // 3) Make the bookkeeping match what we actually keep
        generatedMinX = targetMin;
        generatedMaxX = targetMax;
    }

    private void generateRange(int minX, int maxX) {
        int startX = snapDown(minX, Block.SIZE);
        int endX   = snapDown(maxX, Block.SIZE); // safe even if maxX isn't aligned

        for (int x = startX; x <= endX; x += Block.SIZE) {
            if (spawnedByColumnX.containsKey(x)) continue;

            List<Spawned> spawnedHere = new ArrayList<>();
            spawnedByColumnX.put(x, spawnedHere);

            for (Block b : terrain.createInRange(x, x)) {
                gameObjects.addGameObject(b, Layer.STATIC_OBJECTS);
                spawnedHere.add(new Spawned(b, Layer.STATIC_OBJECTS));
            }

            for (GameObject obj : flora.createInRange(x, x)) {
                int layer = layerFor(obj);
                gameObjects.addGameObject(obj, layer);
                spawnedHere.add(new Spawned(obj, layer));
            }
        }
    }

    private int layerFor(GameObject obj) {
        String tag = obj.getTag();
        if (TRUNK_TAG.equals(tag)) return Layer.STATIC_OBJECTS;
        if (LEAF_TAG.equals(tag))  return Layer.STATIC_OBJECTS + 1;
        if (FRUIT_TAG.equals(tag)) return Layer.DEFAULT;
        return Layer.DEFAULT;
    }
    private void pruneOutside(int targetMin, int targetMax) {
        Iterator<Map.Entry<Integer, List<Spawned>>> it = spawnedByColumnX.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<Integer, List<Spawned>> entry = it.next();
            int x = entry.getKey();

            if (x < targetMin || x > targetMax) {
                for (Spawned s : entry.getValue()) {
                    gameObjects.removeGameObject(s.obj, s.layer);
                }
                it.remove();

                // allow regeneration if we come back:
                terrain.forgetColumn(x);
                flora.forgetTreeX(x);
            }
        }
    }


    private static int snapDown(int x, int size) {
        return Math.floorDiv(x, size) * size;
    }

    private static int snapUp(int x, int size) {
        int d = Math.floorDiv(x, size) * size;
        return (d == x) ? x : d + size;
    }
    private static class Spawned {
        final GameObject obj;
        final int layer;
        Spawned(GameObject obj, int layer) {
            this.obj = obj;
            this.layer = layer;
        }
    }
}
