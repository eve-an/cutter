package cutter;

import cutter.models.BallModel;
import cutter.models.OcclusionModel;
import cutter.scores.BallScore;
import cutter.scores.Frame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.util.Iterator;

public class FrameIterator implements Iterator<Frame> {

    private static final Logger log = LogManager.getLogger(FrameIterator.class);

    private final BallModel ballModel;
    private final OcclusionModel occlusionModel;
    private final Video video;
    private final VideoCapture cap;
    private final Mat frameMat;
    private long currentFrame;


    public FrameIterator(BallModel ballModel, OcclusionModel occlusionModel, Video video) {
        this.ballModel = ballModel;
        this.occlusionModel = occlusionModel;
        this.video = video;
        this.cap = new VideoCapture(video.getVideoFile().getAbsolutePath());
        this.currentFrame = -1;
        this.frameMat = new Mat();
    }

    @Override
    public boolean hasNext() {
        return cap.isOpened() && currentFrame + 1 < video.getLengthInFrames();
    }

    @Override
    public Frame next() {
        readMat();

        BallScore maxPrediction = ballModel.predict(frameMat.clone());
        double occScore = occlusionModel.predict(frameMat);

        return new Frame(video.getId(), currentFrame, maxPrediction, occScore, video.getOffset(), video.isOverview());
    }

    private void readMat() {
        if (!cap.read(this.frameMat)) {
            log.error("Could not read Frame!");
            throw new IllegalStateException("Could not read Frame!");
        }

        Imgproc.cvtColor(frameMat, frameMat, Imgproc.COLOR_BGR2RGB);
        currentFrame++;
    }
}
