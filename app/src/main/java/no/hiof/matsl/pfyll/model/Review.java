package no.hiof.matsl.pfyll.model;

public class Review {

    private String reviewText;
    private float reviewValue;
    private String user;
    private String Id;
    private String productId;

    public Review(){}

    public Review(String reviewText, float reviewValue, String user) {
        this.reviewText = reviewText;
        this.reviewValue = reviewValue;
        this.user = user;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
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
