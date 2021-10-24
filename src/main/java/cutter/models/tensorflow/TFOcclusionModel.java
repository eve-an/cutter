package cutter.models.tensorflow;

import cutter.config.ConfigReader;
import cutter.models.OcclusionModel;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.ConcreteFunction;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Tensor;
import org.tensorflow.ndarray.FloatNdArray;
import org.tensorflow.ndarray.NdArrays;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.types.TFloat32;

import java.util.HashMap;
import java.util.Map;

public enum TFOcclusionModel implements OcclusionModel {
    INSTANCE;

    private final String MODEL_DIR = ConfigReader.getConfig().getOcclusionModel().getPath();
    private final double IMAGE_SIZE = ConfigReader.getConfig().getOcclusionModel().getImageSize();

    private final SavedModelBundle model;
    private final ConcreteFunction func;
    private final String input_layer;

    TFOcclusionModel() {
        this.model = SavedModelBundle.load(MODEL_DIR, "serve");
        this.func = model.function("serving_default");
        this.input_layer = "input_1";
    }

    Tensor matToTensor(Mat mat) {
        FloatNdArray array = NdArrays.ofFloats(Shape.of(mat.height(), mat.width(), 3)); // rgb

        for (int h = 0; h < mat.height(); h++) {
            for (int w = 0; w < mat.width(); w++) {
                double[] rgb = mat.get(h, w);
                for (int i = 0; i < 3; i++) {
                    double color = rgb[i];
                    //                if (norm) {
                    //                    color = color / 255.0;
                    //                }

                    array.setFloat((float) color, h, w, i);
                }
            }
        }

        FloatNdArray expanded = NdArrays.ofFloats(Shape.of(1, mat.height(), mat.width(), 3));
        expanded.set(array, 0);

        return TFloat32.tensorOf(expanded);
    }

    Map<String, Tensor> wrapInputTensor(Tensor tensor) {
        Map<String, Tensor> feed_dict = new HashMap<>();
        feed_dict.put(this.input_layer, tensor);

        return feed_dict;
    }

    @Override
    public double predict(Mat mat) {

        Imgproc.resize(mat, mat, new Size(this.IMAGE_SIZE, this.IMAGE_SIZE));
        Tensor matTensor = matToTensor(mat);
        Map<String, Tensor> prediction = this.func.call(wrapInputTensor(matTensor));
        Tensor resultTensor = prediction.values().iterator().next();

        return ((TFloat32) resultTensor).get(0).getFloat(0);
    }

    @Override
    public void close() throws Exception {
        func.close();
        model.close();
    }
}
