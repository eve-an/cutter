package cutter;

import com.google.gson.Gson;
import cutter.config.GameConfig;
import cutter.cuts.CutUtil;
import cutter.cuts.FrameFilter;
import cutter.cuts.PositionHandler;
import cutter.cuts.VideoCut;
import cutter.scores.Frame;
import cutter.util.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class VideoCutter {

    private static final Logger log = LoggerFactory.getLogger(VideoCutter.class);
    private Metrics metrics;
    private final long minCutLength;
    private final int frameStep;
    private final PositionHandler positionHandler;

    public VideoCutter(long minCutLength, int frameStep) {
        this.minCutLength = minCutLength;
        this.frameStep = frameStep;
        GameConfig gameConfig = readGameConfig("game_config.json");
        this.positionHandler = new PositionHandler(gameConfig.getHalftime(), gameConfig.getSetStates());
    }

    private GameConfig readGameConfig(String jsonPath) {
        File file = new File(jsonPath);

        Gson gson = new Gson();
        try (Reader reader = Files.newBufferedReader(file.toPath())) {
            return gson.fromJson(reader, GameConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // A Map is easier to handle and faster to access
    private HashMap<Long, List<Frame>> fillFramesMap(List<Video> videos) {
        HashMap<Long, List<Frame>> framesMap = new HashMap<>();

        for (Video video : videos) {
            for (Frame frame : video.getFrames()) {
                List<Frame> framesAtPos = framesMap.computeIfAbsent(frame.getGlobalPos(), k -> new ArrayList<>());
                framesAtPos.add(frame);
            }
        }

        List<Frame> frameList = framesMap.get(0L);
        for (Long frame : framesMap.keySet()) {
            if (frame % frameStep == 0) {
                frameList = framesMap.get(frame);
            } else {
                framesMap.replace(frame, frameList);
            }
        }

        return framesMap;
    }



    public List<VideoCut> cut(List<Video> videos) {
        var cuts = generateCuts(videos);
        // Throw away Cuts before the game start
        List<VideoCut> cleaned = cuts.stream()
                .filter(cut -> cut.getStartFrame() >= 5900)
                .collect(Collectors.toList());

        var cutCleaner = new CutUtil();
        cutCleaner.cleanCuts(cleaned, minCutLength);

        cleaned.removeIf(cut -> positionHandler.isInHalftimePause(cut.getEndFrame() - 500));

        return cleaned;
    }

    private List<VideoCut> generateCuts(List<Video> videos) {
        HashMap<Long, List<Frame>> framesMap = fillFramesMap(videos);
        List<VideoCut> cuts = new ArrayList<>();
        VideoCut currentCut = null;

        for (long framePos = 0; framePos < framesMap.size(); framePos++) {
            List<Frame> frames = framesMap.get(framePos);
            Frame bestFrame = decideForBestFrame(frames);

            if (currentCut == null) {
                currentCut = new VideoCut(bestFrame.getVideoId(), 0, 0);
            } else if (bestFrame.getVideoId() == currentCut.getVideoId()) { // Make our cut bigger
                currentCut.incrementEndFrame();
            } else {
                cuts.add(currentCut);
                currentCut = new VideoCut(bestFrame.getVideoId(), framePos, framePos);
            }
        }

        return cuts;
    }

    private Frame decideForBestFrame(List<Frame> frames) {
        sanityCheck(frames);

        long framePos = frames.get(0).getGlobalPos();
        var currentGameState = positionHandler.getCurrentGameState(framePos, 75);

        boolean nearSetState = currentGameState == PositionHandler.GameState.SET_STATE;
        boolean nearHalftime = currentGameState == PositionHandler.GameState.FIRST_HALFTIME;

        Frame bestFrame;
        if (frames.size() == 1) {   // Trivial Case
            bestFrame = frames.get(0);
        } else if (nearSetState || nearHalftime || FrameFilter.allOcclusion(frames)) { // Prioritize overview cams for set states and halftimes or when every action cam contains occlusion
            bestFrame = bestOverviewFrame(frames);
        } else {
            List<Frame> actionCams = FrameFilter.actionOnly(frames);

            if (actionCams.isEmpty()) {
                log.error("Cannot happen!");
                throw new IllegalStateException("Cannot find suitable frame");
            } else {
                List<Frame> noOcclusion = FrameFilter.withoutOcclusion(actionCams);
                bestFrame = FrameFilter.bestBallView(noOcclusion); // Best Frame = Action Cam + No Occlusion + max bounding box area
            }
        }

        return bestFrame;
    }

    private Frame bestOverviewFrame(List<Frame> frames) {
        List<Frame> overviewOnly = FrameFilter.overviewOnly(frames);
        return FrameFilter.bestBallView(overviewOnly);
    }

    private void sanityCheck(List<Frame> frames) {
        String errorMsg = "";

        if (frames.isEmpty()) {
            errorMsg += "Frames must not be empty!";
        } else if (!sameFramePosition(frames)) {
            errorMsg += "Frames must be at the same global position!";
        } else {
            return; // Ok
        }

        log.error(errorMsg + frames);
        throw new IllegalArgumentException(errorMsg);
    }

    private boolean sameFramePosition(List<Frame> frames) {
        Frame frame = frames.get(0);
        for (Frame other : frames) {
            if (frame.getGlobalPos() != other.getGlobalPos()) {
                return false;
            }
        }

        return true;
    }
}
