package com.smarttravel.myanmar;

public class ReviewDisplay {
    private final Review review;
    private final String username;

    public ReviewDisplay(Review review, String username) {
        this.review = review;
        this.username = username;
    }

    public Review getReview() { return review; }
    public String getUsername() { return username; }
}

