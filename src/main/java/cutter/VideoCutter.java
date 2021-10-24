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
import java.util.*;
import java.util.stream.Collectors;

public class VideoCutter {

    private static final Logger log = LogManager.getLogger(VideoCutter.class);
    private final List<Video> videos;
    private final HashMap<Long, List<Frame>> framesMap;
    private Metrics metrics;
    private long currentPos;

    private List<VideoCut> cuts = new ArrayList<>();
    private VideoCut currentCut;
    private List<Cut> setStates;

    public VideoCutter(List<Video> videos, List<Cut> setStates) {
        this.videos = videos;
        this.setStates = setStates;
        this.framesMap = new HashMap<>();


        for (Video video : videos) {
            for (Frame frame : video.getFrames()) {
                List<Frame> framesAtPos = framesMap.computeIfAbsent(frame.getGlobalPos(), k -> new ArrayList<>());
                framesAtPos.add(frame);
            }
        }
    }


    public void generateScores(String outDir) {
        File outDirFile = createDirectory(outDir);
        saveFrameScores(outDirFile);
    }

    public List<VideoCut> cut() {

        for (long framePos = 0; framePos < framesMap.size(); framePos++) {
            currentPos = framePos;
            List<Frame> frames = framesMap.get(framePos);

            if (currentPos > 4305 && currentPos < 14754) {
                frames = frames.stream().filter(frame -> !frame.isOverview()).collect(Collectors.toList());
            }

            Frame bestFrame = decideForBestFrame(framePos, frames);
            if (currentCut != null) {
                if (bestFrame.getVideoId() == currentCut.getVideoId()) { // Make our cut bigger
                    currentCut.incrementEndFrame();
                } else {

                    if (currentCut.getLength() < 30) {
                        VideoCut lastCut = cuts.get(cuts.size() - 1);
                        lastCut.setEndFrame(currentCut.getEndFrame());
                        currentCut = lastCut;
                    } else {
                        cuts.add(currentCut);
                        currentCut = new VideoCut(bestFrame.getVideoId(), framePos, framePos);
                    }
                }
            } else {
                currentCut = new VideoCut(bestFrame.getVideoId(), 0, 0);
            }
        }

        return cuts;
    }

    private Frame decideForBestFrame(long framePos, List<Frame> frames) {
        if (frames.size() == 1) {
            return frames.get(0);
        }

        if (framePos == 4698) {
            System.out.println("");
        }

        Frame bestFrame;
        if (allVideosObscured((int) framePos)) {
            bestFrame = frameWithBestBallView(frames);
        } else {
            List<Frame> noOcclusion = framesWithoutOcclusion(frames);
            List<Frame> maybeBall = noOcclusion.stream().filter(Frame::ballIsVisible).collect(Collectors.toList());

            if (maybeBall.isEmpty()) {
                if (takeFrameWithCurrentId(frames).isPresent()) {
                    bestFrame = takeFrameWithCurrentId(frames).get();
                } else {
                    return takeMostDominantVideo(frames).orElse(frames.get(0));
                }
            } else {
                bestFrame = frameWithBestBallView(noOcclusion);
            }

        }

        return bestFrame;
    }

    private Optional<Frame> takeMostDominantVideo(List<Frame> frames) {
        int mostRepresentedId = getIdWithMostCuts();
        return frames.stream().filter(frame -> frame.getVideoId() == mostRepresentedId).findAny();
    }

    private Optional<Frame> takeFrameWithCurrentId(List<Frame> frames) {
        return frames.stream()
                .filter(frame -> frame.getVideoId() == currentCut.getVideoId())
                .findAny();
    }


    public int getIdWithMostCuts() {
        Map<Integer, Integer> histogram = new HashMap<>();

        for (VideoCut cut : cuts) {
            histogram.putIfAbsent(cut.getVideoId(), 1);
            histogram.computeIfPresent(cut.getVideoId(), (id, occurrences) -> occurrences + 1);
        }

        int maxId = -1;
        int maxValue = -1;
        for (Map.Entry<Integer, Integer> entry : histogram.entrySet()) {
            if (entry.getValue() > maxValue) {
                maxId = entry.getKey();
            }
        }

        return maxId;
    }

    public List<Frame> framesWithoutOcclusion(List<Frame> frames) {
        return frames.stream()
                .filter(frame -> !frame.containsOcclusion())
                .collect(Collectors.toList());
    }


    public List<Frame> getFramesAt(int framePos) {
        return videos.stream()
                .filter(video -> {
                    long videoStart = video.getOffset();
                    long videoEnd = video.getOffset() + video.getLengthInFrames();

                    return framePos >= videoStart && framePos < videoEnd;
                })
                .map(video -> video.getFrames().get((int) (framePos - video.getOffset())))
                .collect(Collectors.toList());
    }

    public Frame frameWithBestBallView(List<Frame> frames) {
        Frame bestFrameByScore = frames.stream()
                .max(Comparator.comparingDouble(frame -> frame.getBallScore().getScore()))
                .orElseThrow(() -> new IllegalStateException("Could not find maximum ball score: " + frames + " " + currentPos));

        BallScore maxScore = bestFrameByScore.getBallScore();
        for (Frame frame : frames) {
            BallScore otherScore = frame.getBallScore();
            if (maxScore.getScore() - otherScore.getScore() < 0.10 && frame.ballIsVisible()) {
                if (otherScore.getBbox().getArea() > maxScore.getBbox().getArea()) {
                    bestFrameByScore = frame;
                    maxScore = frame.getBallScore();
                }
            }
        }

        return bestFrameByScore;
    }

    public boolean allVideosObscured(int framePos) { // TODO: Übersichtsvideos entfernen, da die keine Verdeckungen haben!
        return getFramesAt(framePos).stream()
                .allMatch(Frame::containsOcclusion);
    }

    private long getMaxFrame() {
        return videos.stream()
                .map(video -> video.getOffset() + video.getLengthInFrames()) // Maximum frame pos of video
                .max(Long::compareTo)
                .orElseThrow(() -> new IllegalStateException("Could not determine max frame of videos"));
    }

    private void saveFrameScores(File outDirFile) {
        for (Video video : videos) {
            FrameIterator frameIterator = new FrameIterator(PyTorchBallModel.INSTANCE, TFOcclusionModel.INSTANCE, video);
            File outFile = new File(outDirFile, video.getVideoFile().getName() + ".json");

            metrics = new Metrics();
            while (frameIterator.hasNext()) {
                Frame frame = frameIterator.next();
                metrics.updateTime();
                video.add(frame);
                logProgress(video, outFile, frame);
            }

            saveFrames(video, outFile);
        }
    }

    private File createDirectory(String outDir) {
        File outDirFile = new File(outDir);
        if (!outDirFile.exists()) {
            createDirectory(outDirFile);
        }
        return outDirFile;
    }

    private void createDirectory(File outDirFile) {
        try {
            Files.createDirectory(outDirFile.toPath());
        } catch (IOException e) {
            log.error("Could not create directory {}", outDirFile);
            throw new RuntimeException(e);
        }
    }

    private void logProgress(Video video, File outFile, Frame frame) {
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


    private void saveFrames(Video video, File output) {
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
