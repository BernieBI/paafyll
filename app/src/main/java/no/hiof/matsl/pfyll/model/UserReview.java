package no.hiof.matsl.pfyll.model;

import java.util.ArrayList;

public class UserReview {

    private String product;
    private Review review;
    // private ArrayList<Review> reviews = new ArrayList<Review>();

    public UserReview(String product, Review review){
        this.product = product;
        this.review = review;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public Review getReview() {
        return review;
    }

    public void setReview(Review review) {
        this.review = review;
    }
}
