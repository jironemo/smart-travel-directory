package com.smarttravel.myanmar;

public class ReviewDisplay {
    private final Review review;
    private final String username;
    private final String destinationName;
    private boolean show;
    private final String reviewDocId;

    public ReviewDisplay(Review review, String username, String destinationName, boolean show, String reviewDocId) {
        this.review = review;
        this.username = username;
        this.destinationName = destinationName;
        this.show = show;
        this.reviewDocId = reviewDocId;
    }

    public Review getReview() { return review; }
    public String getUsername() { return username; }
    public String getDestinationName() { return destinationName; }
    public boolean isShow() { return show; }
    public void setShow(boolean show) { this.show = show; }
    public String getReviewDocId() { return reviewDocId; }
}
