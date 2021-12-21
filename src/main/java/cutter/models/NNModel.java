package cutter.models;

public class NNModel {

    private final String path;
    private final int imageSize;
    private final double threshold;

    public NNModel(String path, int imageSize, double threshold) {
        this.path = path;
        this.imageSize = imageSize;
        this.threshold = threshold;
    }

    public String getPath() {
        return path;
    }

    public int getImageSize() {
        return imageSize;
    }

    public double getThreshold() {
        return threshold;
    }
}
