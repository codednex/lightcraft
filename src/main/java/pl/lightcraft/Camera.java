package pl.lightcraft;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
    public Vector3f position;
    public Vector3f direction;
    public float yaw = -90.0f;
    public float pitch = 0.0f;

    public Camera(Vector3f startPos) {
        this.position = startPos;
        this.direction = new Vector3f();
        updateVectors();
    }

    public void rotate(float dx, float dy) {
        yaw += dx;
        pitch += dy;
        if (pitch > 89.0f) pitch = 89.0f;
        if (pitch < -89.0f) pitch = -89.0f;
        updateVectors();
    }

    private void updateVectors() {
        // Obliczanie wektora kierunku
        direction.x = (float) Math.cos(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch));
        direction.y = (float) Math.sin(Math.toRadians(pitch));
        direction.z = (float) Math.sin(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch));
        direction.normalize();
    }

    public Matrix4f getViewMatrix() {
        Vector3f target = new Vector3f(position).add(direction);
        return new Matrix4f().lookAt(position, target, new Vector3f(0, 1, 0));
    }
}