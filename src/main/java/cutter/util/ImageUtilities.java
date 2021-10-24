package cutter.util;


import org.opencv.core.Mat;

public class ImageUtilities {


    public static float[][][][] Mat2PyTorchArray(Mat mat) {
        float[][][][] array = new float[1][mat.channels()][mat.height()][mat.width()];
        for (int h = 0; h < mat.height(); h++) {
            for (int w = 0; w < mat.width(); w++) {
                double[] rgb = mat.get(h, w);
                for (int i = 0, rgbLength = rgb.length; i < rgbLength; i++) {
                    float color = (float) (rgb[i] / 255.0);
                    array[0][i][h][w] = color;
                }
            }
        }

        return array;
    }
}
