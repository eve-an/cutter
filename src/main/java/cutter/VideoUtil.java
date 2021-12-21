package cutter;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class VideoUtil {

    public static void fillSkipFrames(List<Video> videos) {
        long maxFramePos = videos.stream()
                .map(video -> video.getLengthInFrames() + video.getOffset())
                .max(Comparator.comparingLong(Long::longValue))
                .get();

        for (int i = 0; i < maxFramePos; i++) {
            List<Video> videosAtCurrentPos = numCutsAtFramePos(videos, i);

            if (videosAtCurrentPos.size() == 0) {
                throw new IllegalArgumentException("Videos must not contain any gaps!");
            } else if (videosAtCurrentPos.size() == 1) {
                Video onlyVideo = videosAtCurrentPos.get(0);
                for (Video video : videos) {
                    if (video.getId() == onlyVideo.getId()) {
                        video.addSkipFrame(i);
                    }
                }
            }
        }
    }

    private static List<Video> numCutsAtFramePos(List<Video> videos, long framePos) {
        return videos.stream()
                .filter(video -> framePos >= video.getOffset())
                .filter(video -> framePos <= video.getOffset() + video.getLengthInFrames())
                .collect(Collectors.toList());
    }
}
