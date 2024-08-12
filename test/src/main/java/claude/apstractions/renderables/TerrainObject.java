package claude.apstractions.renderables;

import claude.apstractions.UniformManager;
import claude.apstractions.shaders.Shader;
import claude.apstractions.transforms.Camera;
import claude.apstractions.transforms.Light;
import claude.apstractions.transforms.Renderable;
import claude.apstractions.transforms.Transform;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL20.glUseProgram;

public class TerrainObject extends Transform implements Renderable {
    TerrainMesh terrainMesh;
    Shader shader;
    UniformManager uniformManager;
    float span;
    float divisionSpan;
    float maxHeight;
    float[][] heightMap;

    public float getSpan() {
        return span;
    }

    //returns bottom-left point (i=0, j=0)
    public Vector3f getOrigin() {
        return getPosition().sub(span/2, 0, span/2, new Vector3f());
    }

    public float getDivisionSpan() {
        return divisionSpan;
    }

    public float getMaxHeight() {
        return maxHeight;
    }

    public float[][] getHeightMap() {
        return heightMap;
    }

    public TerrainObject(Shader shader, UniformManager uniformManager) {
        this.shader = shader;
        this.uniformManager = uniformManager;
    }

    @Override
    public void render(Camera camera, Light light) {
        glUseProgram(shader.getShader());

        uniformManager.setUniformMatrix4f(shader.getShader(), "projection", camera.getProjectionMatrix());
        uniformManager.setUniformMatrix4f(shader.getShader(), "model", this.getModelMatrix());
        uniformManager.setUniformMatrix4f(shader.getShader(), "view", camera.getViewMatrix());

        float time = (float) glfwGetTime();
        uniformManager.setUniformFloat(shader.getShader(), "time", time);

        uniformManager.setUniformVector3f(shader.getShader(), "viewPos", camera.getPosition());
        uniformManager.setUniformVector3f(shader.getShader(), "lightColor", light.getColor());
        uniformManager.setUniformVector3f(shader.getShader(), "lightDirection", light.getLightDirection());
        uniformManager.setUniformFloat(shader.getShader(), "ambientIntensity", light.getAmbientIntensity());

        uniformManager.setUniformFloat(shader.getShader(), "height", maxHeight);

        terrainMesh.draw();
    }

    public float getHeightAt(Vector3f point) {
        float center = this.getSpan() / 2;
        float divisionSpan = this.getDivisionSpan();

        // Adjust the point coordinates to match the terrain's coordinate system
        float adjustedX = point.x + center;
        float adjustedZ = point.z + center;

        int i = (int) Math.floor(adjustedX / divisionSpan);
        int j = (int) Math.floor(adjustedZ / divisionSpan);

        if (i < 0 || i >= this.getHeightMap().length - 1 || j < 0 || j >= this.getHeightMap()[0].length - 1) {
            return -10;
        }

        float xLocal = (adjustedX / divisionSpan) - i;
        float zLocal = (adjustedZ / divisionSpan) - j;

        boolean isLowerTriangle = (xLocal + zLocal <= 1);

        float y1, y2, y3;
        float weight1, weight2, weight3;

        if (isLowerTriangle) {
            y1 = this.getHeightMap()[i][j];
            y2 = this.getHeightMap()[i+1][j];
            y3 = this.getHeightMap()[i][j+1];

            weight1 = 1 - xLocal - zLocal;
            weight2 = xLocal;
            weight3 = zLocal;
        } else {
            y1 = this.getHeightMap()[i+1][j+1];
            y2 = this.getHeightMap()[i][j+1];
            y3 = this.getHeightMap()[i+1][j];

            weight1 = xLocal + zLocal - 1;
            weight2 = 1 - xLocal;
            weight3 = 1 - zLocal;
        }

        return y1 * weight1 + y2 * weight2 + y3 * weight3;
    }

    public boolean isAbovePoint(Vector3f point) {
        float center = this.getSpan() / 2;
        float divisionSpan = this.getDivisionSpan();

        float adjustedX = point.x + center;
        float adjustedZ = point.z + center;

        int i = (int) Math.floor(adjustedX / divisionSpan);
        int j = (int) Math.floor(adjustedZ / divisionSpan);

        if (i < 0 || i >= this.getHeightMap().length - 1 || j < 0 || j >= this.getHeightMap()[0].length - 1) {
            return false;
        }

        if (!pointIsCloseEnough(point)) return false;
        if (isAbovePointByAlot(i, j, point)) return true;
        if (isUnderPointByAlot(i, j, point)) return false;

        float heightAtPoint = getHeightAt(point);
        return heightAtPoint > point.y;
    }

    private boolean isAbovePointByAlot(int i, int j, Vector3f point) {
        float min = Float.MAX_VALUE;
        min = Math.min(this.getHeightMap()[i][j], min);
        min = Math.min(this.getHeightMap()[i+1][j], min);
        min = Math.min(this.getHeightMap()[i][j+1], min);
        min = Math.min(this.getHeightMap()[i+1][j+1], min);

        return min > point.y;
    }

    private boolean isUnderPointByAlot(int i, int j, Vector3f point) {
        float max = Float.MIN_VALUE;
        max = Math.max(this.getHeightMap()[i][j], max);
        max = Math.max(this.getHeightMap()[i+1][j], max);
        max = Math.max(this.getHeightMap()[i][j+1], max);
        max = Math.max(this.getHeightMap()[i+1][j+1], max);

        return point.y > max;
    }

    public boolean pointIsCloseEnough(Vector3f point) {
        Vector3f min = new Vector3f(this.getPosition()).sub(this.getSpan()/2, this.getPosition().y, this.getSpan()/2);
        Vector3f max = new Vector3f(this.getPosition()).add(this.getSpan()/2, this.getMaxHeight(), this.getSpan()/2);
        return min.x < point.x && min.z < point.z && min.y < point.y && point.y < max.y && point.x < max.x && point.z < max.z;
    }
}
