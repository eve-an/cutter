package cutter.models;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import cutter.config.ConfigReader;
import cutter.util.ImageUtilities;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.Collections;
import java.util.Map;

public enum OnnxOcclusionModel implements OcclusionModel {
    INSTANCE;

    private OrtSession session;
    private OrtEnvironment env;

    OnnxOcclusionModel() {
        init();
    }

    private void init() {
        this.env = OrtEnvironment.getEnvironment();
        try {
            this.session = env.createSession(ConfigReader.getConfig().getOcclusionModel().getPath(), new OrtSession.SessionOptions());
        } catch (OrtException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public double predict(Mat image) {
        try {
            Imgproc.resize(image, image, new Size(112, 112));
            float[][][][] pyTorchArray = ImageUtilities.Mat2PyTorchArray(image);
            try (OnnxTensor inputTensor = OnnxTensor.createTensor(env, pyTorchArray)) {
                Map<String, OnnxTensor> inputs = Collections.singletonMap("input", inputTensor);

                try (OrtSession.Result results = session.run(inputs)) {
                    float[][] output = (float[][]) results.get("output")
                            .orElseThrow(() -> new RuntimeException("Error while predicting occlusion."))
                            .getValue();

                    float occlusionScore = output[0][0];
                    return occlusionScore;
                }
            }
        } catch (OrtException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        this.session.close();
        this.env.close();
    }
}
