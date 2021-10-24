package cutter.models;

import org.opencv.core.Mat;

public interface OcclusionModel extends AutoCloseable {

    double predict(Mat image);
}
