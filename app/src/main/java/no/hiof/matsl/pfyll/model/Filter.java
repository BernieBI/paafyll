package no.hiof.matsl.pfyll.model;

public class Filter {
    public enum Type {
        DOUBLE,
        BOOLEAN,
        STRING
    }
    private String key;
    private Object lower;
    private Object upper;
    private Type type;

    private boolean hasUpperBound;
    private boolean hasLowerBound;


    private Filter (String key, Object lower, Object upper, Type type) {
        this.key = key;
        this.lower = lower;
        this.upper = upper;
        this.type = type;

        hasLowerBound = lower != null;
        hasUpperBound = upper != null;
    }

    public Filter(String key, double lower, double upper) {
        this(key, lower, upper, Type.DOUBLE);
    }

    public Filter(String key, boolean lower, boolean upper) {
        this(key, lower, upper, Type.BOOLEAN);
    }

    public Filter(String key, String lower, String upper) {
        this(key, lower, upper, Type.STRING);
    }

    public Type getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    public boolean isLowerBounded() {
        return hasLowerBound;
    }

    public boolean isUpperBounded() {
        return hasUpperBound;
    }

    public double getLowerAsDouble() {
        if (type != Type.DOUBLE) {
            throw new RuntimeException("Filter was not double");
        }

        return (Double) lower;
    }

    public double getUpperAsDouble() {
        if (type != Type.DOUBLE) {
            throw new RuntimeException("Filter was not double");
        }

        return (Double) upper;
    }

    public boolean getLowerAsBoolean() {
        if (type != Type.BOOLEAN) {
            throw new RuntimeException("Filter was not boolean");
        }

        return (Boolean) lower;
    }

    public boolean getUpperAsBoolean() {
        if (type != Type.BOOLEAN) {
            throw new RuntimeException("Filter was not boolean");
        }

        return (Boolean) upper;
    }

    public String getLowerAsString() {
        if (type != Type.STRING) {
            throw new RuntimeException("Filter was not string");
        }

        return (String) lower;
    }

    public String getUpperAsString() {
        if (type != Type.STRING) {
            throw new RuntimeException("Filter was not string");
        }

        return (String) upper;
    }
}
