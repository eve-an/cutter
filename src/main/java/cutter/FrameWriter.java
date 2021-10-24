package cutter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cutter.cuts.Cut;
import cutter.cuts.VideoCut;
import cutter.models.pytorch.PyTorchBallModel;
import cutter.models.tensorflow.TFOcclusionModel;
import cutter.scores.BallScore;
import cutter.scores.Frame;
import cutter.util.Metrics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class FrameWriter {


    public  static void saveFrameScores(List<Video> videos, File outDirFile) {
        for (Video video : videos) {
            File outFile = new File(outDirFile, video.getVideoFile().getName() + ".json");
            saveFrames(video, outFile);
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
