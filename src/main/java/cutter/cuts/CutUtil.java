package cutter.cuts;

import cutter.Video;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CutUtil {




    public void cleanCuts(List<VideoCut> cuts, long minCutLength) {
        mergeSmallCuts(cuts, minCutLength);
        mergeConsecutiveCuts(cuts);
    }

    private void mergeSmallCuts(List<VideoCut> cleaned, long minCutLength) {
        while (hasSmallCuts(cleaned, minCutLength)) {
            VideoCut toRemove = null;
            for (int i = 0; i < cleaned.size(); i++) {
                VideoCut cut = cleaned.get(i);

                if (cut.getLength() < minCutLength) {
                    if (i == cleaned.size() - 1) { // Last cut is too short
                        var left = cleaned.get(i - 1);
                        left.setEndFrame(cut.getEndFrame());
                    } else if (i == 0) { // First cut is too short
                        var right = cleaned.get(i + 1);
                        right.setStartFrame(cut.getStartFrame());
                    } else { // Somewhere in between
                        var right = cleaned.get(i + 1);
                        var left = cleaned.get(i - 1);

                        if (right.getLength() > left.getLength()) {
                            right.setStartFrame(cut.getStartFrame());
                        } else {
                            left.setEndFrame(cut.getEndFrame());
                        }
                    }
                    toRemove = cut;
                    break;
                }
            }

            if (toRemove != null) {
                cleaned.remove(toRemove);
            }
        }
    }

    private boolean hasSmallCuts(List<VideoCut> cleaned, long minCutLength) {
        return cleaned.stream().anyMatch(cut -> cut.getLength() < minCutLength);
    }

    private void mergeConsecutiveCuts(List<VideoCut> cleaned) {
        while (!isMerged(cleaned)) {
            VideoCut toRemove = null;

            for (int i = 0; i < cleaned.size() - 1; i++) {
                VideoCut cut = cleaned.get(i);
                VideoCut nextCut = cleaned.get(i + 1);

                if (cut.getVideoId() == nextCut.getVideoId()) {
                    nextCut.setStartFrame(cut.getStartFrame());
                    toRemove = cut;
                    break;
                }
            }

            if (toRemove != null) {
                cleaned.remove(toRemove);
            }
        }
    }


    private boolean isMerged(List<VideoCut> cleaned) {
        for (int i = 0; i < cleaned.size() - 1; i++) {
            VideoCut cut = cleaned.get(i);
            VideoCut nextCut = cleaned.get(i + 1);

            if (cut.getVideoId() == nextCut.getVideoId()) {
                return false;
            }
        }

        return true;
    }
}
