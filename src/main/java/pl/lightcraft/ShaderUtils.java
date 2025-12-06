package pl.lightcraft;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import static org.lwjgl.opengl.GL20.*;

public class ShaderUtils {
    public static int load(String vertPath, String fragPath) throws IOException {
        String vertCode = readSource(vertPath);
        String fragCode = readSource(fragPath);

        int vertID = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertID, vertCode);
        glCompileShader(vertID);
        checkError(vertID);

        int fragID = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragID, fragCode);
        glCompileShader(fragID);
        checkError(fragID);

        int programID = glCreateProgram();
        glAttachShader(programID, vertID);
        glAttachShader(programID, fragID);
        glLinkProgram(programID);

        glDeleteShader(vertID);
        glDeleteShader(fragID);

        return programID;
    }

    private static void checkError(int shaderID) {
        if (glGetShaderi(shaderID, GL_COMPILE_STATUS) == 0) {
            throw new RuntimeException("Shader Error: " + glGetShaderInfoLog(shaderID));
        }
    }

    private static String readSource(String path) throws IOException {
        try (InputStream in = ShaderUtils.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) throw new IOException("Nie znaleziono shadera: " + path);
            Scanner scanner = new Scanner(in, StandardCharsets.UTF_8.name()).useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }
}