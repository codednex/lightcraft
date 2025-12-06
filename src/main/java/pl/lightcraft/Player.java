package pl.lightcraft;

import org.joml.Vector3f;
import static org.lwjgl.glfw.GLFW.*;

public class Player {
    private Camera camera;
    private long window;

    // Fizyka
    private Vector3f velocity = new Vector3f();
    private float speed = 4.5f;
    private float sensitivity = 0.1f;
    private float gravity = -18.0f;
    private float jumpHeight = 6.0f;
    private boolean onGround = false;

    // Mysz
    private double lastX, lastY;
    private boolean firstMouse = true;

    public Player(long window, World world) {
        this.window = window;
        // Start na wysokości 15 (nad ziemią)
        this.camera = new Camera(new Vector3f(8, 15, 8));
    }

    public void update(float dt) {
        handleInput(dt);
        applyPhysics(dt);
    }

    private void handleInput(float dt) {
        // Mysz
        double[] xpos = new double[1], ypos = new double[1];
        glfwGetCursorPos(window, xpos, ypos);

        if (firstMouse) {
            lastX = xpos[0]; lastY = ypos[0];
            firstMouse = false;
        }

        float dx = (float) (xpos[0] - lastX) * sensitivity;
        float dy = (float) (lastY - ypos[0]) * sensitivity; // Bez inwersji (ypos - lastY jeśli chcesz inwersje)

        lastX = xpos[0]; lastY = ypos[0];
        camera.rotate(dx, dy);

        // Klawiatura (ruch poziomy)
        Vector3f moveDir = new Vector3f();
        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) moveDir.add(camera.direction.x, 0, camera.direction.z);
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) moveDir.sub(camera.direction.x, 0, camera.direction.z);
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) moveDir.add(camera.direction.z, 0, -camera.direction.x);
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) moveDir.sub(camera.direction.z, 0, -camera.direction.x);

        if (moveDir.length() > 0) moveDir.normalize().mul(speed);

        velocity.x = moveDir.x;
        velocity.z = moveDir.z;

        // Skok
        if (glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS && onGround) {
            velocity.y = jumpHeight;
            onGround = false;
        }
    }

    private void applyPhysics(float dt) {
        // Grawitacja
        velocity.y += gravity * dt;

        // Prosta kolizja z podłogą (Y=11, bo trawa jest na 10, a gracz ma wysokość)
        // W pełnej wersji tutaj byłoby sprawdzanie AABB bloków
        if (camera.position.y < 12.0f && velocity.y < 0) { // 10 bloków + 2 wysokość oczu
            camera.position.y = 12.0f;
            velocity.y = 0;
            onGround = true;
        } else {
            onGround = false;
        }

        // Aplikowanie ruchu
        camera.position.add(velocity.x * dt, velocity.y * dt, velocity.z * dt);
    }

    // RAYCASTING (Budowanie)
    public void tryBreakBlock(World world) {
        Vector3f pos = raycast(world, 5.0f, false); // false = szukamy bloku zajętego
        if (pos != null) {
            world.setBlock((int)pos.x, (int)pos.y, (int)pos.z, BlockType.AIR);
        }
    }

    public void tryPlaceBlock(World world) {
        Vector3f pos = raycast(world, 5.0f, true); // true = szukamy miejsca przed blokiem
        if (pos != null) {
            // Unikaj postawienia bloku w głowie gracza (proste sprawdzenie)
            if (Math.abs(pos.x - camera.position.x) > 0.5 || Math.abs(pos.z - camera.position.z) > 0.5)
                world.setBlock((int)pos.x, (int)pos.y, (int)pos.z, BlockType.STONE);
        }
    }

    private Vector3f raycast(World world, float distance, boolean placeMode) {
        Vector3f rayStart = new Vector3f(camera.position);
        Vector3f rayDir = new Vector3f(camera.direction).normalize();
        Vector3f step = new Vector3f(rayDir).mul(0.1f); // Krok 0.1 jednostki

        Vector3f currentPos = new Vector3f(rayStart);
        Vector3f lastPos = new Vector3f(rayStart);

        for (float d = 0; d < distance; d += 0.1f) {
            if (world.getBlock((int)currentPos.x, (int)currentPos.y, (int)currentPos.z) != BlockType.AIR) {
                // Znaleziono blok
                return placeMode ? lastPos : currentPos; // Zwróć poprzednią pozycję dla stawiania, obecną dla niszczenia
            }
            lastPos.set(currentPos);
            currentPos.add(step);
        }
        return null;
    }

    public Camera getCamera() { return camera; }
}