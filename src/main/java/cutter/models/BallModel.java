package cutter.models;

import cutter.scores.BallScore;
import org.opencv.core.Mat;

public interface BallModel {

    BallScore predict(Mat image);

    void close();
}
