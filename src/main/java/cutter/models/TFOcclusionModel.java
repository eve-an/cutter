package cutter.models;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.Tensor;
import org.tensorflow.types.TFloat32;

import java.io.IOException;
import java.util.Map;

public class TFOcclusionModel extends Model<Double> {


    public TFOcclusionModel(String modelDir) throws IOException {
        super(modelDir);
    }

    @Override
    public Double predict(Mat mat) {
        Imgproc.resize(mat, mat, new Size(getImageSize(), getImageSize()));
        Tensor matTensor = matToTensor(mat);
        Map<String, Tensor> resultMap = predict(matTensor);
        Tensor resultTensor = resultMap.values().iterator().next();

        return (double) ((TFloat32) resultTensor).get(0).getFloat(0);
    }

}
