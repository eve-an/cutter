package cutter.models.tensorflow;

import cutter.geometry.MyBoundingBox;
import cutter.geometry.MyPoint;
import cutter.models.BallModel;
import cutter.models.Model;
import cutter.scores.BallScore;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.Tensor;
import org.tensorflow.ndarray.ByteNdArray;
import org.tensorflow.ndarray.FloatNdArray;
import org.tensorflow.ndarray.NdArrays;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.types.TFloat32;
import org.tensorflow.types.TUint8;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class TFBallModel extends Model<BallScore> implements BallModel {

    public TFBallModel(String modelDir) throws IOException {
        super(modelDir);
    }

    @Override
    protected Tensor matToTensor(Mat mat) {
        ByteNdArray array = NdArrays.ofBytes(Shape.of(mat.height(), mat.width(), 3)); // rgb

        for (int h = 0; h < mat.height(); h++) {
            for (int w = 0; w < mat.width(); w++) {
                double[] rgb = mat.get(h, w);
                for (int i = 0; i < 3; i++) {
                    double color = rgb[i];
                    array.setByte((byte) color, h, w, i);
                }
            }
        }

        ByteNdArray expanded = NdArrays.ofBytes(Shape.of(1, mat.height(), mat.width(), 3));
        expanded.set(array, 0);

        return TUint8.tensorOf(expanded);
    }

    @Override
    public void close() {
        this.stop();
    }

    @Override
    public BallScore predict(Mat mat) {
        int imageSize = getImageSize();
        int origWidth = mat.width();
        int origHeight = mat.height();

        if (imageSize != -1) {
            Imgproc.resize(mat, mat, new Size(imageSize, imageSize));
        }
        Tensor matTensor = matToTensor(mat);
        Map<String, Tensor> resultMap = predict(matTensor);


        TFloat32 boxesTensor = (TFloat32) resultMap.get("detection_boxes");
        TFloat32 scoresTensor = (TFloat32) resultMap.get("detection_scores");


        FloatNdArray boxesArrays = boxesTensor.get(0);
        long numArrays = boxesArrays.shape().size(0);

        FloatNdArray scoresArray = scoresTensor.get(0);

        List<BallScore> results = new ArrayList<>();
        for (int i = 0; i < numArrays; i++) {
            FloatNdArray bboxArray = boxesArrays.get(i);

            double score = scoresArray.getFloat(i);

            int ymin = (int) (origHeight * bboxArray.getFloat(0));
            int xmin = (int) (origWidth * bboxArray.getFloat(1));
            int ymax = (int) (origHeight * bboxArray.getFloat(2));
            int xmax = (int) (origWidth * bboxArray.getFloat(3));

            MyBoundingBox bbox = new MyBoundingBox(new MyPoint(xmin, ymin), new MyPoint(xmax, ymax));

            BallScore prediction = new BallScore(score, bbox);
            results.add(prediction);
        }

        resultMap.values().forEach(Tensor::close);
        matTensor.close();

        return results.stream()
                .max(Comparator.comparingDouble(BallScore::getScore))
                .orElseThrow(() -> new IllegalArgumentException("Could not find a ball with ball detection model.")); // TODO: create custom exception
    }
}
