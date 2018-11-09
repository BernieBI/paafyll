package no.hiof.matsl.pfyll.model;

public class StringFilter extends Filter<String> {
    private String value;
    public StringFilter(String fieldName, ComparisonType comparisonType, String value) {
        super(fieldName, comparisonType);
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
}
