package cutter.frame;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cutter.Video;
import cutter.cuts.VideoCut;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FrameWriter {

    private final static Map<Integer, String> ID_TO_FILENAME = Map.of(
            0, "MAH00791.MP4.mp4",
            1, "MAH00790.MP4.mp4",
            2, "2019_0503_152615_004.MP4.mp4",
            3, "2019_0503_150006_003.MP4.mp4",
            4, "14570001.MOV.mp4"
    );


    public static void saveFrameScores(List<Video> videos, File outDirFile) {
        for (Video video : videos) {
            File outFile = new File(outDirFile, video.getVideoFile().getName() + ".json");
            saveFrames(video, outFile);
        }
    }

    public static Map<Integer, Long> getOffsetMap(List<Video> videos) {
        Map<Integer, Long> offsets = new HashMap<>();

        for (Video video : videos) {
            offsets.put(video.getId(), video.getOffset());
        }

        return offsets;
    }

    public static void cutsToCsv(List<Video> videos, List<VideoCut> cuts, String outCsv) throws IOException {
        Map<Integer, Long> offsetMap = getOffsetMap(videos);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outCsv))) {
            bw.write("video_id,offset,start,end");

            for (VideoCut cut : cuts) {
                bw.newLine();
                long offset = offsetMap.get(cut.getVideoId());
                String toWrite = String.format("%s,%d,%d,%d",
                        ID_TO_FILENAME.get(cut.getVideoId()),
                        offset,
                        cut.getStartFrame(),
                        cut.getEndFrame());

                bw.write(toWrite);
            }
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
