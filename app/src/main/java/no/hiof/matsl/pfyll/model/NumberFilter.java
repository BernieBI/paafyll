package no.hiof.matsl.pfyll.model;

public class NumberFilter extends Filter<Double> {
    private double value;

    public NumberFilter(double value, String fieldName, ComparisonType comparisonType) {
        super(fieldName, comparisonType);
        this.value = value;
    }

    @Override
    public Double getValue() {
        return value;
    }
}
