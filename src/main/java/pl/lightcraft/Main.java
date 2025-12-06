package pl.lightcraft;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main {
    private long window;
    private int width = 1280, height = 720;

    private World world;
    private Renderer renderer;
    private Player player;
    private boolean isMenuOpen = false;

    // Czas klatki
    private float deltaTime = 0.0f;
    private float lastFrame = 0.0f;

    public void run() {
        init();
        loop();
        cleanup();
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) throw new IllegalStateException("Nie udało się zainicjalizować GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        // ZMIANA: Ustawienie tytułu okna
        window = glfwCreateWindow(width, height, "LightCraft RD1", NULL, NULL);
        if (window == NULL) throw new RuntimeException("Błąd tworzenia okna");

        // Obsługa klawiatury
        glfwSetKeyCallback(window, (win, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS) {
                if (key == GLFW_KEY_ESCAPE) {
                    toggleMenu();
                }
                if (key == GLFW_KEY_F5) {
                    // W tej wersji tylko komunikat, ponieważ World.saveWorld jest uproszczone
                    System.out.println("Zapisano świat! (Funkcja zapisu Infinite World jest uproszczona w RD1)");
                }
            }
        });

        // Obsługa myszy (budowanie/niszczenie)
        glfwSetMouseButtonCallback(window, (win, button, action, mods) -> {
            if (action == GLFW_PRESS && !isMenuOpen) {
                if (button == GLFW_MOUSE_BUTTON_LEFT) player.tryBreakBlock(world);
                if (button == GLFW_MOUSE_BUTTON_RIGHT) player.tryPlaceBlock(world);
            }
        });

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1); // V-Sync
        glfwShowWindow(window);
        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);

        // --- Inicjalizacja Gry ---
        renderer = new Renderer();
        world = new World();
        player = new Player(window, world);
        // Ustawienie startowej pozycji gracza na bezpieczną wysokość (Y=50)
        player.getCamera().position.y = 50;

        // Ukryj myszkę na start
        setCursorLocked(true);
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            float currentFrame = (float) glfwGetTime();
            deltaTime = currentFrame - lastFrame;
            lastFrame = currentFrame;

            // Logika i aktualizacja (tylko gdy nie ma menu)
            if (!isMenuOpen) {
                player.update(deltaTime);
                // ZMIANA: Aktualizacja świata (generowanie chunków)
                world.update(player.getCamera().position);
            }

            // Render
            float bg = isMenuOpen ? 0.1f : 0.53f;
            glClearColor(bg, isMenuOpen ? 0.1f : 0.81f, isMenuOpen ? 0.1f : 0.92f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            renderer.render(world, player.getCamera());

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void toggleMenu() {
        isMenuOpen = !isMenuOpen;
        setCursorLocked(!isMenuOpen);
        System.out.println(isMenuOpen ? "--- PAUZA / MENU ---" : "--- GRA ---");
    }

    private void setCursorLocked(boolean locked) {
        if (locked) {
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        } else {
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        }
    }

    private void cleanup() {
        renderer.cleanup();
        world.cleanup();
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    public static void main(String[] args) {
        new Main().run();
    }
}