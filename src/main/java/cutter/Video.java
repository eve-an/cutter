package cutter;

import cutter.scores.Frame;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Video {

    private final int id;
    private final long offset;
    private final long lengthInFrames;
    private final File videoFile;
    private final boolean isOverview;
    private final List<Frame> frames;
    private List<Long> skipFrames; // These frames can be skipped for prediction because they are the only ones at these positions

    public Video(File videoFile, int id, long offset, long lengthInFrames, boolean isOverview) {
        this.videoFile = videoFile;
        this.id = id;
        this.offset = offset;
        this.lengthInFrames = lengthInFrames;
        this.isOverview = isOverview;
        this.frames = new ArrayList<>();
        this.skipFrames = new ArrayList<>();
    }


    public Video(File videoFile, int id, long offset, boolean isOverview) {
        this(videoFile, id, offset, readVideoLength(videoFile.getAbsolutePath()), isOverview);
    }

    public void addFrame(Frame frame) {
        this.frames.add(frame);
    }

    public void addSkipFrame(long skipFrame) {
        if (skipFrames == null) {
            skipFrames = new ArrayList<>();
        }
        this.skipFrames.add(skipFrame);
    }

    public static long readVideoLength(String videoPath) {
        VideoCapture cap = new VideoCapture(videoPath);
        long frameCount = (long) cap.get(Videoio.CAP_PROP_FRAME_COUNT);
        cap.release();
        return frameCount;
    }

    public File getVideoFile() {
        return videoFile;
    }

    public int getId() {
        return id;
    }

    public long getOffset() {
        return offset;
    }

    public long getLengthInFrames() {
        return lengthInFrames;
    }

    public List<Frame> getFrames() {
        return frames;
    }

    public List<Long> getSkipFrames() {
        return skipFrames;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Video video = (Video) o;
        return id == video.id && offset == video.offset && lengthInFrames == video.lengthInFrames && videoFile.equals(video.videoFile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(videoFile, id, offset, lengthInFrames);
    }

    @Override
    public String toString() {
        return "Video{" +
                "videoFile=" + videoFile +
                ", id=" + id +
                ", offset=" + offset +
                ", lengthInFrames=" + lengthInFrames +
                '}';
    }

    public boolean isOverview() {
        return isOverview;
    }
}
