package cutter.config;

public class ModelConfig {

    private String path;
    private double threshold;
    private boolean ignore;
    private double imageSize;

    public double getImageSize() {
        return imageSize;
    }

    public boolean isIgnore() {
        return ignore;
    }

    public String getPath() {
        return path;
    }

    public double getThreshold() {
        return threshold;
    }

}
