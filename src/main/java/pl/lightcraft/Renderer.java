package pl.lightcraft;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Renderer {
    private int shaderProgram;
    private int uniModel, uniView, uniProjection;
    private Texture textureAtlas;

    // UI
    private int uiVao, uiVbo;

    public Renderer() {
        try {
            shaderProgram = ShaderUtils.load("shaders/vertex.glsl", "shaders/fragment.glsl");
            textureAtlas = new Texture("textures/block.png");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        uniModel = glGetUniformLocation(shaderProgram, "model");
        uniView = glGetUniformLocation(shaderProgram, "view");
        uniProjection = glGetUniformLocation(shaderProgram, "projection");

        initUI();
    }

    public void render(World world, Camera camera) {
        glUseProgram(shaderProgram);
        glEnable(GL_DEPTH_TEST);

        // 3D PASS
        Matrix4f view = camera.getViewMatrix();
        float[] viewData = new float[16];
        view.get(viewData);
        glUniformMatrix4fv(uniView, false, viewData);

        Matrix4f projection = new Matrix4f().perspective((float) Math.toRadians(70.0f), 1280f/720f, 0.1f, 1000.0f);
        float[] projData = new float[16];
        projection.get(projData);
        glUniformMatrix4fv(uniProjection, false, projData);

        Matrix4f model = new Matrix4f();
        float[] modelData = new float[16];
        model.get(modelData);
        glUniformMatrix4fv(uniModel, false, modelData);

        glActiveTexture(GL_TEXTURE0);
        textureAtlas.bind();

        world.render();

        // 2D UI PASS (Ekwipunek)
        renderUI();

        textureAtlas.unbind();
        glUseProgram(0);
    }

    private void initUI() {
        // Prosty kwadrat na dole ekranu
        // X, Y, Z, U, V
        float[] vertices = {
                -0.5f, -0.9f, 0.0f,  0.0f, 0.0f, // Lewy dół (Kamień)
                0.5f, -0.9f, 0.0f,  1.0f, 0.0f,
                0.5f, -0.7f, 0.0f,  1.0f, 0.5f,
                -0.5f, -0.9f, 0.0f,  0.0f, 0.0f,
                0.5f, -0.7f, 0.0f,  1.0f, 0.5f,
                -0.5f, -0.7f, 0.0f,  0.0f, 0.5f
        };
        // Powyższe UV (0-1, 0-0.5) pokaże górną połowę atlasu jako "pasek"

        uiVao = glGenVertexArrays();
        uiVbo = glGenBuffers();
        glBindVertexArray(uiVao);
        glBindBuffer(GL_ARRAY_BUFFER, uiVbo);
        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length);
        buffer.put(vertices).flip();
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);
    }

    private void renderUI() {
        glDisable(GL_DEPTH_TEST); // UI rysowane na wierzchu

        // Reset matrices for 2D
        Matrix4f identity = new Matrix4f();
        float[] idData = new float[16];
        identity.get(idData);
        glUniformMatrix4fv(uniModel, false, idData);
        glUniformMatrix4fv(uniView, false, idData);
        glUniformMatrix4fv(uniProjection, false, idData);

        glBindVertexArray(uiVao);
        glDrawArrays(GL_TRIANGLES, 0, 6);

        glEnable(GL_DEPTH_TEST);
    }

    public void cleanup() {
        glDeleteProgram(shaderProgram);
        textureAtlas.cleanup();
        glDeleteVertexArrays(uiVao);
        glDeleteBuffers(uiVbo);
    }
}