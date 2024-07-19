package javagl;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class TesterClass {
    public static void main(String[] args) {
        Vector4f position = new Vector4f(0, 0, 0, 1);
        Vector4f position2 = new Vector4f(position);
        position2.add(1, 1, 1, 0);

        Vector4f pos3;
        position.add(position2);

        System.out.println(position2);

        Matrix4f matrix = new Matrix4f();
        System.out.println(matrix);
        matrix.identity().translate(1, 1, 1);
        System.out.println(matrix);
        System.out.println(new Matrix4f().scale(2, 2, 2).mul(matrix));
        matrix.mul(new Matrix4f().scale(2, 2, 2));
        System.out.println(matrix);

        Vector3f vec = new Vector3f(1, 0, 0);
        Vector3f vec2 = vec.cross(new Vector3f(0, 1, 0), new Vector3f());
        System.out.println(vec2);
    }
}
