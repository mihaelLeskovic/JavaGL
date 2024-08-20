package simulator.transforms;

import simulator.drawables.TerrainMesh;
import simulator.utility.Cleanable;
import simulator.physics.hitboxes.HitboxVisitor;
import simulator.physics.hitboxes.VisitableHitbox;
import simulator.shaders.UniformManager;
import simulator.shaders.Shader;
import simulator.transforms.Camera;
import simulator.transforms.Light;
import simulator.transforms.Renderable;
import simulator.transforms.Transform;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL20.glUseProgram;

public class TerrainObject extends Transform implements Renderable, Cleanable, VisitableHitbox {
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

    public void setMaxHeight(float maxHeight) {
        this.maxHeight = maxHeight;
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

        uniformManager.setUniformVector3f(shader.getShader(), "viewPos", camera.getPosition());
        uniformManager.setUniformVector3f(shader.getShader(), "lightColor", light.getColor());
        uniformManager.setUniformVector3f(shader.getShader(), "lightDirection", light.getLightDirection());
        uniformManager.setUniformFloat(shader.getShader(), "ambientIntensity", light.getAmbientIntensity());

        uniformManager.setUniformFloat(shader.getShader(), "maxHeight", maxHeight);

        terrainMesh.draw();
    }

    public float getHeightAt(Vector3f point) {
        Vector3f localPoint = new Vector3f(point).sub(this.getPosition());
        if (!pointIsInXZArea(localPoint)) return 0; // Return ground level instead of Float.MIN_VALUE

        float halfSpan = this.getSpan() / 2;
        float divisionSpan = this.getDivisionSpan();

        float adjustedX = localPoint.x + halfSpan;
        float adjustedZ = localPoint.z + halfSpan;

        int i = Math.max(0, Math.min((int) (adjustedX / divisionSpan), this.getHeightMap().length - 2));
        int j = Math.max(0, Math.min((int) (adjustedZ / divisionSpan), this.getHeightMap()[0].length - 2));

        float xLocal = (adjustedX / divisionSpan) - i;
        float zLocal = (adjustedZ / divisionSpan) - j;

        float h00 = this.getHeightMap()[i][j];
        float h10 = this.getHeightMap()[i+1][j];
        float h01 = this.getHeightMap()[i][j+1];
        float h11 = this.getHeightMap()[i+1][j+1];

        float h0 = h00 * (1 - xLocal) + h10 * xLocal;
        float h1 = h01 * (1 - xLocal) + h11 * xLocal;

        return h0 * (1 - zLocal) + h1 * zLocal;
    }

    public boolean isAbovePoint(Vector3f point) {
        if(!pointIsInXZArea(point.sub(this.getPosition()))) return false;

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
        return min.x < point.x && min.z < point.z && point.y < max.y && point.x < max.x && point.z < max.z;
    }

    //local coords
    public boolean pointIsInXZArea(Vector3f point) {
        float halfSpan = this.getSpan() / 2;
        return point.x >= -halfSpan && point.x <= halfSpan &&
                point.z >= -halfSpan && point.z <= halfSpan;
    }

    @Override
    public void cleanup() {
        terrainMesh.cleanup();
        shader.cleanup();
    }

    @Override
    public boolean isCleaned() {
        return false;
    }

    @Override
    public void accept(HitboxVisitor visitor) {
        visitor.visitTerrain(this);
    }
}
