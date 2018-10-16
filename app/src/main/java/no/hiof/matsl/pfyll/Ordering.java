package no.hiof.matsl.pfyll;

public class Ordering {
    public enum Mode {
        ASCENDING,
        DESCENDING
    }
    private String key;
    private Mode mode;

    public Ordering(String key, Mode mode) {
        this.key = key;
        this.mode = mode;
    }

    public String getKey() {
        return key;
    }

    public Mode getMode() {
        return mode;
    }
}
