package cutter.config;

public class Config {

    private ModelConfig ballModel;
    private ModelConfig occlusionModel;
    private int predictionStep;

    public int getPredictionStep() {
        return predictionStep;
    }

    public ModelConfig getBallmodel() {
        return ballModel;
    }

    public ModelConfig getOcclusionModel() {
        return occlusionModel;
    }
}
