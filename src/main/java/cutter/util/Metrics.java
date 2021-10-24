package cutter.util;

public class Metrics {

    private double fps;
    private final long startTime;
    private long processedFrames = 0;

    public Metrics() {
        this.startTime = System.nanoTime();
    }

    public void updateTime() {
        processedFrames++;
        long diffTime = System.nanoTime() - startTime;
        fps = processedFrames / (diffTime / Math.pow(10, 9));
    }

    public double getFps() {
        return fps;
    }

    public long getProcessedFrames() {
        return processedFrames;
    }
}
