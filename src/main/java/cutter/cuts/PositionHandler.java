package cutter.cuts;

import cutter.config.Halftime;
import cutter.config.SetState;

import java.util.List;

public class PositionHandler {

    private final Halftime halftime;
    private final List<SetState> setStates;

    public PositionHandler(Halftime halftime, List<SetState> setStates) {
        this.halftime = halftime;
        this.setStates = setStates;
    }

    public GameState getCurrentGameState(long position, long margin) {
        if (nearHalftime(position, margin)) {
            return GameState.FIRST_HALFTIME;
        } else if (isInSetState(position, margin)) {
            return GameState.SET_STATE;
        } else {
            return GameState.PLAYING;
        }
    }


    public boolean nearHalftime(long pos, long margin) {
        boolean nearFirst = Math.abs(pos - halftime.getFirstStart()) <= margin;
        boolean nearSecond = Math.abs(pos - halftime.getSecondStart()) <= margin;

        return nearFirst || nearSecond;
    }

    public boolean isInHalftimePause(long pos) {
        return pos >= halftime.getFirstEnd() && pos <= halftime.getSecondStart();
    }

    public boolean isInSetState(long pos, long margin) {
        return setStates.stream()
                .anyMatch(setState -> frameInSetState(pos, margin, setState));
    }

    private boolean frameInSetState(long pos, long margin, SetState setState) {
        return pos - margin >= setState.getStart() && pos + margin <= setState.getEnd();
    }

    public static boolean between(long value, long start, long end) {
        return value >= start && value <= end;
    }

    public enum GameState {
        FIRST_HALFTIME,
        SECOND_HALFTIME,
        SET_STATE,
        PLAYING
    }
}
