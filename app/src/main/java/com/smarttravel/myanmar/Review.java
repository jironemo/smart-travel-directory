package com.smarttravel.myanmar;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

public class Review {
    private String comment;
    private Timestamp created_at;
    private DocumentReference destination_id;
    private double rating;
    private Timestamp updated_at;
    private DocumentReference user_id;

    public Review() {}

    public String getComment() { return comment; }
    public Timestamp getCreated_at() { return created_at; }
    public DocumentReference getDestination_id() { return destination_id; }
    public double getRating() { return rating; }
    public Timestamp getUpdated_at() { return updated_at; }
    public DocumentReference getUser_id() { return user_id; }
}

