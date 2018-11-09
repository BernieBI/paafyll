package no.hiof.matsl.pfyll.model;

public class BooleanFilter extends Filter<Boolean> {
    private boolean value;

    public BooleanFilter(boolean value, String fieldName, ComparisonType comparisonType) {
        super(fieldName, comparisonType);
        this.value = value;
    }

    @Override
    public Boolean getValue() {
        return value;
    }
}
