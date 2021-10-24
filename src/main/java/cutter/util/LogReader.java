package cutter.util;

import cutter.cuts.Cut;
import org.tensorflow.op.math.Log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class LogReader {

    private File log;
    private long logOffset;
    private LocalTime startTime;

    public LogReader(File log, long logOffset) {
        this.log = log;
        this.logOffset = logOffset;
    }

    public List<Cut> getSetStates() {
        try {
            List<String> file = Files.readAllLines(log.toPath());
            List<Cut> cuts = new ArrayList<>();
            List<LogEntry> entries = new ArrayList<>();

            Cut currentCut = null;
            for (String line : file) {
                String[] lineArr = line.split(": ");

                String timeStampEntry = lineArr[0];
                String stateEntry = lineArr[1].strip().toLowerCase();

                LogEntry.STATE state;
                if (stateEntry.equals("set")) {
                    state = LogEntry.STATE.SET;
                } else if (stateEntry.equals("playing")) {
                    state = LogEntry.STATE.PLAYING;
                } else {
                    continue;
                }


                String timeString = timeStampEntry.split("-")[1];
                String[] timeArr = timeString.split("\\.");

                int hour = Integer.parseInt(timeArr[0]);
                int minute = Integer.parseInt(timeArr[1]);
                int second = Integer.parseInt(timeArr[2]);

                LocalTime time = LocalTime.of(hour, minute, second);

                LogEntry entry = new LogEntry(time, state);


                if (currentCut == null) {
                    startTime = time;
                    currentCut = new Cut(logOffset, -1);
                } else {
                    if (state == LogEntry.STATE.SET) {
                        currentCut = new Cut(logOffset + (Duration.between(startTime, time).getSeconds() * 30), -1);
                    } else {
                        //currentCut.setEndFrame(currentCut.getStartFrame() + Duration.between());
                    }
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static class LogEntry {

        private LocalTime timestamp;
        private STATE state;

        public LogEntry(LocalTime timestamp, STATE state) {
            this.timestamp = timestamp;
            this.state = state;
        }

        public LocalTime getTimestamp() {
            return timestamp;
        }

        public STATE getState() {
            return state;
        }

        enum STATE {
            SET,
            PLAYING
        }
    }
}
