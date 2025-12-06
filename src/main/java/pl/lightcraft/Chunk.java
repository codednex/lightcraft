package pl.lightcraft;

import org.lwjgl.BufferUtils;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;

public class Chunk {
    public static final int SIZE = 16;
    public static final int HEIGHT = 128; // Wyższy świat

    public BlockType[][][] blockData = new BlockType[SIZE][HEIGHT][SIZE];
    private int vao, vbo, vertexCount;
    public final int chunkX, chunkZ; // Współrzędne chunka w świecie

    public Chunk(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    public void generate(PerlinNoise noise) {
        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                // Obliczanie globalnej pozycji
                double globalX = (chunkX * SIZE) + x;
                double globalZ = (chunkZ * SIZE) + z;

                // Generowanie wysokości (Amplitude 20, Base 30)
                // Używamy mniejszej skali (0.05) dla łagodniejszych wzgórz
                double n = noise.noise(globalX * 0.05, globalZ * 0.05);
                int h = (int) (n * 20 + 30);

                for (int y = 0; y < HEIGHT; y++) {
                    if (y == 0) blockData[x][y][z] = BlockType.STONE; // Bedrock (udawany)
                    else if (y < h - 4) blockData[x][y][z] = BlockType.STONE;
                    else if (y < h) blockData[x][y][z] = BlockType.DIRT;
                    else if (y == h) blockData[x][y][z] = BlockType.GRASS;
                    else blockData[x][y][z] = BlockType.AIR;
                }
            }
        }
    }

    public void rebuildMesh() {
        if (vao != 0) cleanup(); // Usuń stary mesh przed zrobieniem nowego

        List<Float> vertices = new ArrayList<>();

        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                for (int z = 0; z < SIZE; z++) {
                    BlockType type = blockData[x][y][z];
                    if (type == BlockType.AIR) continue;

                    // Pozycje globalne (aby renderować chunki obok siebie)
                    float gx = (chunkX * SIZE) + x;
                    float gz = (chunkZ * SIZE) + z;

                    // Prosta optymalizacja (nie renderuj ścian między blokami w tym samym chunku)
                    if (shouldRenderFace(x, y+1, z)) addFace(vertices, gx, y, gz, "TOP", type);
                    if (shouldRenderFace(x, y-1, z)) addFace(vertices, gx, y, gz, "BOTTOM", type);
                    if (shouldRenderFace(x, y, z+1)) addFace(vertices, gx, y, gz, "FRONT", type);
                    if (shouldRenderFace(x, y, z-1)) addFace(vertices, gx, y, gz, "BACK", type);
                    if (shouldRenderFace(x-1, y, z)) addFace(vertices, gx, y, gz, "LEFT", type);
                    if (shouldRenderFace(x+1, y, z)) addFace(vertices, gx, y, gz, "RIGHT", type);
                }
            }
        }
        updateVBO(vertices);
    }

    private boolean shouldRenderFace(int x, int y, int z) {
        // Jeśli wychodzi poza ten chunk, na razie renderujemy (prosta metoda bez sąsiadów)
        if (x < 0 || x >= SIZE || y < 0 || y >= HEIGHT || z < 0 || z >= SIZE) return true;
        return blockData[x][y][z] == BlockType.AIR;
    }

    private void addFace(List<Float> list, float x, float y, float z, String face, BlockType type) {
        float u1=0, v1=0, u2=0, v2=0;
        float S = 0.5f; // Atlas 2x2

        // Mapowanie UV
        if (type == BlockType.STONE) { u1=0; v1=0; }
        else if (type == BlockType.DIRT) { u1=0; v1=S; }
        else if (type == BlockType.GRASS) {
            if (face.equals("TOP")) { u1=S; v1=0; }
            else if (face.equals("BOTTOM")) { u1=0; v1=S; }
            else { u1=S; v1=S; }
        }
        u2 = u1 + S; v2 = v1 + S;

        if (face.equals("TOP")) {
            list.add(x); list.add(y+1); list.add(z);   list.add(u1); list.add(v1);
            list.add(x); list.add(y+1); list.add(z+1); list.add(u1); list.add(v2);
            list.add(x+1); list.add(y+1); list.add(z+1); list.add(u2); list.add(v2);
            list.add(x); list.add(y+1); list.add(z);   list.add(u1); list.add(v1);
            list.add(x+1); list.add(y+1); list.add(z+1); list.add(u2); list.add(v2);
            list.add(x+1); list.add(y+1); list.add(z); list.add(u2); list.add(v1);
        }
        if (face.equals("BOTTOM")) {
            list.add(x); list.add(y); list.add(z+1); list.add(u1); list.add(v1);
            list.add(x); list.add(y); list.add(z);   list.add(u1); list.add(v2);
            list.add(x+1); list.add(y); list.add(z); list.add(u2); list.add(v2);
            list.add(x); list.add(y); list.add(z+1); list.add(u1); list.add(v1);
            list.add(x+1); list.add(y); list.add(z); list.add(u2); list.add(v2);
            list.add(x+1); list.add(y); list.add(z+1); list.add(u2); list.add(v1);
        }
        if (face.equals("FRONT")) {
            list.add(x); list.add(y); list.add(z+1); list.add(u1); list.add(v2);
            list.add(x+1); list.add(y); list.add(z+1); list.add(u2); list.add(v2);
            list.add(x+1); list.add(y+1); list.add(z+1); list.add(u2); list.add(v1);
            list.add(x); list.add(y); list.add(z+1); list.add(u1); list.add(v2);
            list.add(x+1); list.add(y+1); list.add(z+1); list.add(u2); list.add(v1);
            list.add(x); list.add(y+1); list.add(z+1); list.add(u1); list.add(v1);
        }
        if (face.equals("BACK")) {
            list.add(x+1); list.add(y); list.add(z); list.add(u1); list.add(v2);
            list.add(x); list.add(y); list.add(z); list.add(u2); list.add(v2);
            list.add(x); list.add(y+1); list.add(z); list.add(u2); list.add(v1);
            list.add(x+1); list.add(y); list.add(z); list.add(u1); list.add(v2);
            list.add(x); list.add(y+1); list.add(z); list.add(u2); list.add(v1);
            list.add(x+1); list.add(y+1); list.add(z); list.add(u1); list.add(v1);
        }
        if (face.equals("LEFT")) {
            list.add(x); list.add(y); list.add(z); list.add(u1); list.add(v2);
            list.add(x); list.add(y); list.add(z+1); list.add(u2); list.add(v2);
            list.add(x); list.add(y+1); list.add(z+1); list.add(u2); list.add(v1);
            list.add(x); list.add(y); list.add(z); list.add(u1); list.add(v2);
            list.add(x); list.add(y+1); list.add(z+1); list.add(u2); list.add(v1);
            list.add(x); list.add(y+1); list.add(z); list.add(u1); list.add(v1);
        }
        if (face.equals("RIGHT")) {
            list.add(x+1); list.add(y); list.add(z+1); list.add(u1); list.add(v2);
            list.add(x+1); list.add(y); list.add(z); list.add(u2); list.add(v2);
            list.add(x+1); list.add(y+1); list.add(z); list.add(u2); list.add(v1);
            list.add(x+1); list.add(y); list.add(z+1); list.add(u1); list.add(v2);
            list.add(x+1); list.add(y+1); list.add(z); list.add(u2); list.add(v1);
            list.add(x+1); list.add(y+1); list.add(z+1); list.add(u1); list.add(v1);
        }
    }

    private void updateVBO(List<Float> vertices) {
        vertexCount = vertices.size() / 5;
        if (vao == 0) vao = glGenVertexArrays();
        if (vbo == 0) vbo = glGenBuffers();

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.size());
        for (float f : vertices) buffer.put(f);
        buffer.flip();

        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);
    }

    public void render() {
        if (vertexCount > 0) {
            glBindVertexArray(vao);
            glDrawArrays(GL_TRIANGLES, 0, vertexCount);
        }
    }

    public void cleanup() {
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
    }
}