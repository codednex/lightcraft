package pl.lightcraft;

import org.joml.Vector3f;
import java.util.HashMap;
import java.util.Map;

public class World {
    private Map<String, Chunk> chunks = new HashMap<>();
    private PerlinNoise noise;
    private final int RENDER_DISTANCE = 4; // Promień generowania chunków

    public World() {
        noise = new PerlinNoise(12345L); // Seed
    }

    // Nieskończona generacja wokół gracza
    public void update(Vector3f playerPos) {
        int pChunkX = (int) Math.floor(playerPos.x / Chunk.SIZE);
        int pChunkZ = (int) Math.floor(playerPos.z / Chunk.SIZE);

        for (int x = pChunkX - RENDER_DISTANCE; x <= pChunkX + RENDER_DISTANCE; x++) {
            for (int z = pChunkZ - RENDER_DISTANCE; z <= pChunkZ + RENDER_DISTANCE; z++) {
                String key = x + "," + z;
                if (!chunks.containsKey(key)) {
                    Chunk chunk = new Chunk(x, z);
                    chunk.generate(noise);
                    chunk.rebuildMesh();
                    chunks.put(key, chunk);
                }
            }
        }

        // Opcjonalnie: Usuwanie dalekich chunków (tu pominięte dla prostoty)
    }

    public BlockType getBlock(int x, int y, int z) {
        if (y < 0 || y >= Chunk.HEIGHT) return BlockType.AIR;

        int cx = (int) Math.floor((double)x / Chunk.SIZE);
        int cz = (int) Math.floor((double)z / Chunk.SIZE);

        String key = cx + "," + cz;
        Chunk chunk = chunks.get(key);

        if (chunk == null) return BlockType.AIR;

        // Lokalne koordynaty w chunku
        int lx = x % Chunk.SIZE;
        int lz = z % Chunk.SIZE;
        if (lx < 0) lx += Chunk.SIZE;
        if (lz < 0) lz += Chunk.SIZE;

        return chunk.blockData[lx][y][lz];
    }

    public void setBlock(int x, int y, int z, BlockType type) {
        if (y < 0 || y >= Chunk.HEIGHT) return;

        int cx = (int) Math.floor((double)x / Chunk.SIZE);
        int cz = (int) Math.floor((double)z / Chunk.SIZE);

        String key = cx + "," + cz;
        Chunk chunk = chunks.get(key);

        if (chunk != null) {
            int lx = x % Chunk.SIZE;
            int lz = z % Chunk.SIZE;
            if (lx < 0) lx += Chunk.SIZE;
            if (lz < 0) lz += Chunk.SIZE;

            chunk.blockData[lx][y][lz] = type;
            chunk.rebuildMesh();
        }
    }

    public void render() {
        for (Chunk chunk : chunks.values()) {
            chunk.render();
        }
    }

    public void saveWorld() {
        System.out.println("Zapisywanie jeszcze nie zaimplementowane dla Infinite World (zbyt duży plik w tej lekcji)");
    }

    public void cleanup() {
        for (Chunk chunk : chunks.values()) {
            chunk.cleanup();
        }
    }
}