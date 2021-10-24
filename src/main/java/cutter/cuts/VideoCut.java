package cutter.cuts;

import java.util.Objects;
import java.util.Optional;

public class VideoCut extends Cut {

    private int videoId;

    public VideoCut(int videoId, long start, long end) {
        super(start, end);
        this.videoId = videoId;
    }

    public static VideoCut CreateDefault() {
        return new VideoCut(-1, -1, -1);
    }

    public int getVideoId() {
        return videoId;
    }

    public void setVideoId(int videoId) {
        this.videoId = videoId;
    }

    public boolean sameRange(VideoCut other) {
        return super.equals(other);
    }

    public Optional<VideoCut> merge(VideoCut other) {
        if (getStartFrame() > getEndFrame() || getEndFrame() < other.getStartFrame()) {
            return Optional.empty();
        } else {
            return Optional.of(new VideoCut(this.videoId, Math.min(getStartFrame(), other.getStartFrame()), Math.max(getEndFrame(), other.getEndFrame())));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        VideoCut videoCut = (VideoCut) o;
        return videoId == videoCut.videoId;
    }

    public Optional<VideoCut> intersect(VideoCut other) {
        Optional<Cut> optIntersection = super.intersect(other);

        if (optIntersection.isPresent()) {
            Cut intersection = optIntersection.get();
            VideoCut videoCut = new VideoCut(this.videoId, intersection.getStartFrame(), intersection.getEndFrame());
            return Optional.of(videoCut);
        } else {
            return Optional.empty();
        }

    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), videoId);
    }

    @Override
    public String toString() {
        return "VideoCut{" +
                "videoId=" + videoId + ", " + super.toString() +
                '}';
    }
}
