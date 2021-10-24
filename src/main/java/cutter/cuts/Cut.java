package cutter.cuts;

import java.util.Objects;
import java.util.Optional;

public class Cut {

    private long startFrame;
    private long endFrame;

    public Cut(long startFrame, long endFrame) {
        this.startFrame = startFrame;
        this.endFrame = endFrame;
    }

    public Optional<Cut> intersect(Cut other) {
        if (this.startFrame > other.endFrame || this.endFrame < other.getStartFrame()) {
            return Optional.empty();
        } else {
            return Optional.of(new Cut(Math.max(getStartFrame(), other.getStartFrame()), Math.min(getEndFrame(), other.getEndFrame())));
        }
    }

    public Optional<Cut> merge(Cut other) {
        if (this.startFrame > other.endFrame || this.endFrame < other.getStartFrame()) {
            return Optional.empty();
        } else {
            return Optional.of(new Cut(Math.min(getStartFrame(), other.getStartFrame()), Math.max(getEndFrame(), other.getEndFrame())));
        }
    }


    public boolean toTheRightOf(Cut otherCut) {
        return this.startFrame >= otherCut.startFrame && this.endFrame >= otherCut.endFrame;
    }

    public boolean toTheLeftOf(Cut otherCut) {
        return this.startFrame <= otherCut.startFrame && this.endFrame <= otherCut.endFrame;
    }

    public void incrementEndFrame() {
        endFrame++;
    }

    public long getStartFrame() {
        return startFrame;
    }

    public void setStartFrame(long startFrame) {
        this.startFrame = startFrame;
    }

    public long getEndFrame() {
        return endFrame;
    }

    public void setEndFrame(long endFrame) {
        this.endFrame = endFrame;
    }

    public long getLength() {
        return Math.abs(getEndFrame() - getStartFrame());
    }

    public boolean inRange(long pos) {
        return pos >= getStartFrame() && pos <= getEndFrame();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cut cut = (Cut) o;
        return startFrame == cut.startFrame && endFrame == cut.endFrame;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startFrame, endFrame);
    }

    @Override
    public String toString() {
        return "AbstractCut{" +
                "start=" + startFrame +
                ", end=" + endFrame +
                '}';
    }

}
