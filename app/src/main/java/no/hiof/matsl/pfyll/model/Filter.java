package no.hiof.matsl.pfyll.model;

public abstract class Filter <T>{
    public enum ComparisonType {
        EQUALS,
        GREATER_THAN,
        LESS_THAN
    }
    private ComparisonType comparisonType;
    private String fieldName;

    public Filter(String fieldName, ComparisonType comparisonType) {
        this.comparisonType = comparisonType;
        this.fieldName = fieldName;
    }

    public ComparisonType getComparisonType() {
        return comparisonType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public abstract T getValue();
}
