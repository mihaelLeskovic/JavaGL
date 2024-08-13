package simulator.shaders;

public class ShaderModule {
    private int type;
    private String code;

    public ShaderModule(int type, String code) {
        this.type = type;
        this.code = code;
    }

    public int getType() {
        return type;
    }

    public String getCode() {
        return code;
    }
}