package pl.lightcraft;

import org.lwjgl.BufferUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImage.*;

public class Texture {
    private int id;

    public Texture(String path) throws IOException {
        id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, id);

        // Pixel art style
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        try (InputStream in = getClass().getClassLoader().getResourceAsStream(path)) {
            if (in == null) throw new IOException("Brak tekstury: " + path);
            byte[] bytes = in.readAllBytes();
            ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length);
            buffer.put(bytes).flip();

            IntBuffer w = BufferUtils.createIntBuffer(1);
            IntBuffer h = BufferUtils.createIntBuffer(1);
            IntBuffer c = BufferUtils.createIntBuffer(1);

            ByteBuffer image = stbi_load_from_memory(buffer, w, h, c, 4);
            if (image == null) throw new IOException("Błąd STB: " + stbi_failure_reason());

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, w.get(0), h.get(0), 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
            stbi_image_free(image);
        }
    }

    public void bind() { glBindTexture(GL_TEXTURE_2D, id); }
    public void unbind() { glBindTexture(GL_TEXTURE_2D, 0); }
    public void cleanup() { glDeleteTextures(id); }
}