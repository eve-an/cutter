import com.google.gson.Gson;
import cutter.Video;
import cutter.frame.FrameProducer;
import cutter.frame.FrameWriter;
import cutter.models.onnx.OnnxBallModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        System.load("/usr/lib/libopencv_java454.so");


        List<Video> videos = List.of(
                new Video(new File("/run/media/ivan/Shared/BACKUP2/rUNSWift/converted/MAH00791.MP4.mp4"), 0, 51549, false),
                new Video(new File("/run/media/ivan/Shared/BACKUP2/rUNSWift/converted/2019_0503_152615_004.MP4.mp4"), 2, 51381, false),
                new Video(new File("/run/media/ivan/Shared/BACKUP2/rUNSWift/converted/MAH00790.MP4.mp4"), 1, 4673, false),
                new Video(new File("/run/media/ivan/Shared/BACKUP2/rUNSWift/converted/2019_0503_150006_003.MP4.mp4"), 3, 4299, false),
                new Video(new File("/run/media/ivan/Shared/BACKUP2/rUNSWift/converted/14570001.MOV.mp4"), 4, 0, true)
        );

        FrameProducer.saveFrameScores(videos, "yolov5m_640", new OnnxBallModel());


//        VideoCutter cutter = new VideoCutter(60, 3);

        //        List<VideoCut> cuts = cutter.cut(videos);
//
//        CutWriter.writeCuts(cuts, videos);
    }

    public static void execute_R_plot() throws IOException, InterruptedException {
        var pb = new ProcessBuilder("Rscript", "viz/plot_cuts.R");
        pb.directory(new File("scripts"));
        pb.redirectOutput(new File("out.txt"));
        pb.redirectError(new File("err.txt"));

        pb.start().waitFor();

        pb.command("gwenview", "plots/first.png");
        pb.start();
    }

    public static void updateFrames(List<Video> videos) {
        FrameWriter.saveFrameScores(videos, new File("updated"));
    }

    public static void generateScores(List<Video> videos, String outDir) {
        for (Video video : videos) {

        }
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
