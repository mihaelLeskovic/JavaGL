package javagl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Util {
    public static String readFile(String filePath) {
        String str;
        try {
            str = new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException excp) {
            throw new RuntimeException("Error reading file [" + filePath + "]", excp);
        }
        return str;
    }

    public static String resourceString(String path) {
//        return new String(Util.class.getClassLoader().getResource(path).getPath());
        try {
            return new String(new Util().getClass().getClassLoader().getResourceAsStream(path).readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//                .getClass().getClassLoader().getResourceAsStream("shaders/shader.vert").readAllBytes()
    }
}