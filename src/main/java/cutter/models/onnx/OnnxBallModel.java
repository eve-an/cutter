package cutter.models.onnx;

import cutter.config.ConfigReader;
import cutter.geometry.MyBoundingBox;
import cutter.models.BallModel;
import cutter.models.NNModel;
import cutter.scores.BallScore;
import org.opencv.core.*;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OnnxBallModel extends NNModel implements BallModel {

    private final Net net;

    public OnnxBallModel() {
        super(ConfigReader.getConfig().getBallmodel().getPath(),
                (int) ConfigReader.getConfig().getBallmodel().getImageSize(),
                ConfigReader.getConfig().getBallmodel().getThreshold());

        this.net = Dnn.readNet(getPath());
    }

    public OnnxBallModel(String modelPath) {
        super(modelPath, 640, 0.5);
        net = Dnn.readNet(modelPath);
    }

    public void loadImageToNet(Mat image) {
        Mat img_mat = preprocessImage(image);
        net.setInput(img_mat);
    }

    private Mat preprocessImage(Mat image) {
        double multiplyBy = 1.0 / 255.0;
        boolean toRgb = true;
        var resize = new Size(getImageSize(), getImageSize());
        var subtractRgbBy = new Scalar(0, 0, 0);

        return Dnn.blobFromImage(image, multiplyBy, resize, subtractRgbBy, toRgb);
    }

    @Override
    public BallScore predict(Mat image) {
        loadImageToNet(image);

        var detections = net.forward();
        var optScore = processDetections(detections);

        return optScore.orElseGet(BallScore::NoBall);
    }

    public Optional<BallScore> processDetections(Mat detections) {
        // Transform [1, det_num, 6] to [det_num, 6]
        var detection = detections.reshape(0, detections.size(1));
        Rect2d[] rects = new Rect2d[detection.rows()];
        float[] scoresArray = new float[detection.rows()];

        var start = System.currentTimeMillis();
        for (int i = 0; i < detection.rows(); i++) {
            double centerX = detection.get(i, 0)[0];
            double centerY = detection.get(i, 1)[0];
            double width = detection.get(i, 2)[0];
            double height = detection.get(i, 3)[0];

            double x1 = centerX - width / 2;
            double y1 = centerY - height / 2;

            double conf = detection.get(i, 4)[0];

            rects[i] = new Rect2d(x1, y1, width, height);
            scoresArray[i] = (float) conf;
        }

        var bestIndices = nms(rects, scoresArray);

        if (bestIndices.isEmpty()) {
            return Optional.empty();
        }

        var bestIndex = bestIndices.stream().max(Integer::compare).get();
        var bestRects = rects[bestIndex];

        var bbox = new MyBoundingBox((int) bestRects.x, (int) bestRects.y, (int) (bestRects.x + bestRects.width), (int) (bestRects.y + bestRects.height));
        return Optional.of(new BallScore(scoresArray[bestIndex], bbox));
    }

    private List<Integer> nms(Rect2d[] rects, float[] scoresArray) {
        List<Integer> bestIndices = new ArrayList<>();

        MatOfRect2d bboxes = new MatOfRect2d(rects);
        MatOfFloat scores = new MatOfFloat(scoresArray);
        MatOfInt indices = new MatOfInt();

        Dnn.NMSBoxes(bboxes, scores, (float) this.getThreshold(), 0.45f, indices);

        if (indices.elemSize() == 0) {
            return bestIndices;
        }

        for (int i = 0; i < indices.rows(); i++) {
            int maxIndex = (int) indices.get(i, 0)[0];
            bestIndices.add(maxIndex);
        }

        return bestIndices;
    }


    @Override
    public void close() {
        // Do nothing
    }
}
