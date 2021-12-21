package cutter.frame;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cutter.Video;
import cutter.models.BallModel;
import cutter.models.OcclusionModel;
import cutter.models.tensorflow.TFOcclusionModel;
import cutter.scores.Frame;
import cutter.util.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.List;

public class FrameProducer {

    private static final Logger log = LoggerFactory.getLogger(FrameProducer.class);

    public static void saveFrameScores(List<Video> videos, String out, BallModel ballModel, OcclusionModel occlusionModel) throws IOException {
        File outDir = new File(out);

        if (!outDir.exists()) {
            Files.createDirectory(outDir.toPath());
        } else {
            throw new FileAlreadyExistsException(outDir.getAbsolutePath());
        }

        for (Video video : videos) {
            FrameIterator frameIterator = new FrameIterator(ballModel, TFOcclusionModel.INSTANCE, video);
            File outFile = new File(outDir, video.getVideoFile().getName() + ".json");

            Metrics metrics = new Metrics();
            while (frameIterator.hasNext()) {
                Frame frame = frameIterator.next();
                metrics.updateTime();
                video.addFrame(frame);
                logProgress(video, outFile, frame, metrics);
            }

            saveFrames(video, outFile);
        }
    }

    private static void logProgress(Video video, File outFile, Frame frame, Metrics metrics) {
        if (frame.getFramePos() % 100 == 0) {
            saveFrames(video, outFile);

            log.info("{} - Progress: {}.2f% | Processed {}/{} frames | FPS={}.2f",
                    video.getVideoFile().getName(),
                    frame.getFramePos() / (double) video.getLengthInFrames(),
                    frame.getFramePos(),
                    video.getLengthInFrames(),
                    metrics.getFps());
        }
    }

    private static void saveFrames(Video video, File output) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Writer writer = Files.newBufferedWriter(output.toPath());
            gson.toJson(video, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
