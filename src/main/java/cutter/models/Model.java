package cutter.models;

import org.opencv.core.Mat;
import org.tensorflow.ConcreteFunction;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Tensor;
import org.tensorflow.ndarray.FloatNdArray;
import org.tensorflow.ndarray.NdArrays;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.types.TFloat32;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public abstract class Model<T> {

    private final String TF_GRAPH_INPUT_LAYER;
    private final SavedModelBundle model;
    private final ConcreteFunction predictFunction;
    private final boolean norm;
    private final int imageSize;

    public Model(String modelDir) throws IOException {
        File file = new File(modelDir);

        Properties props = new Properties();
        props.load(new FileInputStream(new File(file, "params.properties")));

        this.model = SavedModelBundle.load(modelDir, props.getProperty("graph"));
        this.predictFunction = model.function(props.getProperty("predict"));
        this.TF_GRAPH_INPUT_LAYER = props.getProperty("input");
        this.norm = props.getProperty("normalize").equalsIgnoreCase("true");
        this.imageSize = Integer.parseInt(props.getProperty("image_size"));
    }


    protected Tensor matToTensor(Mat mat) {
        FloatNdArray array = NdArrays.ofFloats(Shape.of(mat.height(), mat.width(), 3)); // rgb

        for (int h = 0; h < mat.height(); h++) {
            for (int w = 0; w < mat.width(); w++) {
                double[] rgb = mat.get(h, w);
                for (int i = 0; i < 3; i++) {
                    double color = rgb[i];
                    if (norm) {
                        color = color / 255.0;
                    }

                    array.setFloat((float) color, h, w, i);
                }
            }
        }

        FloatNdArray expanded = NdArrays.ofFloats(Shape.of(1, mat.height(), mat.width(), 3));
        expanded.set(array, 0);

        return TFloat32.tensorOf(expanded);
    }

    protected Map<String, Tensor> wrapInputTensor(Tensor tensor) {
        Map<String, Tensor> feed_dict = new HashMap<>();
        feed_dict.put(TF_GRAPH_INPUT_LAYER, tensor);

        return feed_dict;
    }

    protected Map<String, Tensor> predict(Tensor input) {
        return predictFunction.call(wrapInputTensor(input));
    }

    abstract public T predict(Mat mat);

    public int getImageSize() {
        return imageSize;
    }

    public void stop() {
        this.model.close();
    }

}
