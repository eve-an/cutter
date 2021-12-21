package cutter.cuts;

import cutter.Video;
import cutter.frame.FrameWriter;

import java.io.IOException;
import java.util.List;

public class CutWriter {

    public static void writeCuts(List<VideoCut> cleaned, List<Video> videos) {
        try {
            FrameWriter.cutsToCsv(videos, cleaned, "scripts/cuts/cuts.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
