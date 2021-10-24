import com.google.gson.Gson;
import cutter.FrameWriter;
import cutter.Video;
import cutter.VideoCutter;
import cutter.cuts.Cut;
import cutter.cuts.VideoCut;
import cutter.scores.Frame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    private static final Logger log = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        System.load("/home/ivan/Documents/opencv/build/lib/libopencv_java454.so");


        // List<Video> videos = List.of(
        //         new Video(new File("/run/media/Shared/BACKUP2/rUNSWift/converted/MAH00791.MP4.mp4"), 0, 51549, false),
        //         new Video(new File("/run/media/Shared/BACKUP2/rUNSWift/converted/MAH00790.MP4.mp4"), 1, 4673, false),
        //         new Video(new File("/run/media/Shared/BACKUP2/rUNSWift/converted/2019_0503_152615_004.MP4.mp4"), 2, 51381, false),
        //         new Video(new File("/run/media/Shared/BACKUP2/rUNSWift/converted/2019_0503_150006_003.MP4.mp4"), 3, 4299, false),
        //         new Video(new File("/run/media/Shared/BACKUP2/rUNSWift/converted/14570001.MOV.mp4"), 4,0,true)
        // );


        List<Video> videos = loadVideos("scores");

        VideoCutter cutter = new VideoCutter(videos, List.of(new Cut(4305, 999999)));
        // cutter.generateScores("scores");
        List<VideoCut> cuts = cutter.cut();
        cuts = cuts.stream().distinct().collect(Collectors.toList());
        System.out.println("oasd");


    }

    public static void updateFrames(List<Video> videos) {
        FrameWriter.saveFrameScores(videos, new File("updated"));
    }


    public static List<Video> loadVideos(String scoreDir) throws IOException {
        Gson gson = new Gson();
        return Files.list(Paths.get(scoreDir))
                .filter(path -> path.getFileName().toString().endsWith("json"))
                .map(scoreFile -> {
                    try (Reader reader = Files.newBufferedReader(scoreFile)) {
                        return gson.fromJson(reader, Video.class);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }
}
