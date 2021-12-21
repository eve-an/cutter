package cutter.frame;

import cutter.Video;
import cutter.config.ConfigReader;
import cutter.models.BallModel;
import cutter.models.OcclusionModel;
import cutter.scores.BallScore;
import cutter.scores.Frame;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

public class FrameIterator implements Iterator<Frame> {

    private static final Logger log = LoggerFactory.getLogger(FrameIterator.class);

    private final BallModel ballModel;
    private final OcclusionModel occlusionModel;
    private final Video video;
    private final VideoCapture cap;
    private final Mat frameMat;
    private long currentFrame;
    private Frame stepFrame;
    private final int predictionStep = ConfigReader.getConfig().getPredictionStep();


    public FrameIterator(BallModel ballModel, OcclusionModel occlusionModel, Video video) {
        this.ballModel = ballModel;
        this.occlusionModel = occlusionModel;
        this.video = video;
        this.cap = new VideoCapture(video.getVideoFile().getAbsolutePath());
        this.currentFrame = 0;
        this.frameMat = new Mat();
    }

    @Override
    public boolean hasNext() {
        return cap.isOpened() && currentFrame + 1 < video.getLengthInFrames();
    }

    @Override
    public Frame next() {
        Frame nextFrame;

        if (currentFrame % predictionStep == 0) { // Predict every $predictionStep$ steps
            stepFrame = readAndPredict();
            nextFrame = stepFrame;
        } else if (video.getSkipFrames().contains(currentFrame + video.getOffset())) { // Skip and take dummy
            nextFrame = getDummyFrame();
            skipMat();
        } else {    // Skip and take last
            if (stepFrame == null) {
                stepFrame = readAndPredict();
            } else {
                skipMat();
            }

            nextFrame = stepFrame;
        }

        currentFrame++;
        return nextFrame;
    }

    private Frame readAndPredict() {
        readMat();
        BallScore maxPrediction = ballModel.predict(frameMat.clone());
        double occScore = occlusionModel.predict(frameMat);

        return new Frame(video.getId(), currentFrame, maxPrediction, occScore, video.getOffset(), video.isOverview());
    }

    private Frame getDummyFrame() {
        return new Frame(video.getId(), currentFrame, BallScore.GetDefault(), -1, video.getOffset(), video.isOverview());
    }


    private void readMat() {
        if (!cap.read(this.frameMat)) {
            log.error("Could not read Frame!");
            throw new IllegalStateException("Could not read Frame!");
        }

        Imgproc.cvtColor(frameMat, frameMat, Imgproc.COLOR_BGR2RGB);
    }

    private void skipMat() {
        if (!cap.grab()) {
            log.error("Could not read Frame!");
            throw new IllegalStateException("Could not read Frame!");
        }
    }
}
