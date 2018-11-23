package no.hiof.matsl.pfyll.model;

public class Review {

    private String reviewText;
    private float reviewValue;
    private String Id;
    private String productId;
    private String userIndex; // used to retrieve user reviews in the right order
    private int index;
    public Review(){}

    public Review(String reviewText, float reviewValue, int index) {
        this.reviewText = reviewText;
        this.reviewValue = reviewValue;
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getUserIndex() {
        return userIndex;
    }

    public void setUserIndex(String userIndex) {
        this.userIndex = userIndex;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
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
