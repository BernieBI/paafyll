package no.hiof.matsl.pfyll.model;

public class Review {

    private String reviewText;
    private float reviewValue;
    private String user;
    private String Id;
    private String productId;
    private String userReviewId;
    private int index;

    public Review(){}

    public Review(String reviewText, float reviewValue, String user, int index) {
        this.reviewText = reviewText;
        this.reviewValue = reviewValue;
        this.user = user;
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getUserReviewId() {
        return userReviewId;
    }

    public void setUserReviewId(String userReviewId) {
        this.userReviewId = userReviewId;
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
