package no.hiof.matsl.pfyll.model;

public class Review {
    private String reviewText;
    private float reviewValue;

    public Review(String reviewText, float reviewValue) {
        this.reviewText = reviewText;
        this.reviewValue = reviewValue;
    }


    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public float getReviewValue() {
        return reviewValue;
    }

    public void setReviewValue(float reviewValue) {
        this.reviewValue = reviewValue;
    }
}
