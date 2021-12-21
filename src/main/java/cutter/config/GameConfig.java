package cutter.config;

import java.util.List;

public class GameConfig {

    private Halftime halftime;
    private List<SetState> setStates;

    public Halftime getHalftime() {
        return halftime;
    }

    public void setHalftime(Halftime halftime) {
        this.halftime = halftime;
    }

    public List<SetState> getSetStates() {
        return setStates;
    }

    public void setSetStates(List<SetState> setStates) {
        this.setStates = setStates;
    }
}

