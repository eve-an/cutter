package cutter.cuts;

import cutter.scores.BallScore;
import cutter.scores.Frame;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FrameFilter {

    public static List<Frame> withoutOcclusion(List<Frame> frames) {
        return frames.stream()
                .filter(frame -> !frame.containsOcclusion())
                .collect(Collectors.toList());
    }

    public static List<Frame> overviewOnly(List<Frame> frames) {
        return frames.stream()
                .filter(Frame::isOverview)
                .collect(Collectors.toList());
    }

    public static List<Frame> actionOnly(List<Frame> frames) {
        return frames.stream()
                .filter(frame -> !frame.isOverview())
                .collect(Collectors.toList());
    }

    public static Frame bestBallView(List<Frame> frames) {
        Frame bestFrameByScore = frames.stream()
                .max(Comparator.comparingDouble(frame -> frame.getBallScore().getScore()))
                .orElseThrow(() -> new IllegalStateException("Could not find maximum ball score: " + frames));

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

    public static boolean allOcclusion(List<Frame> frames) {
        return actionOnly(frames).stream() // Overview videos dont contain occlusion
                .allMatch(Frame::containsOcclusion);
    }

}
