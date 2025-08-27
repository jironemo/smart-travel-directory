package com.smarttravel.myanmar;

import com.google.firebase.firestore.DocumentReference;
import java.io.Serializable;
import java.util.List;

public class TripAdvice implements Serializable {
    private String id;
    private String location;
    private List<DocumentReference> destinations;
    private String estimatedCost;

    private String food;
    private Double rating;

    public TripAdvice(String id, String location, List<DocumentReference> destinations, String estimatedCost, Double rating) {
        this.id = id;
        this.location = location;
        this.destinations = destinations;
        this.estimatedCost = estimatedCost;
        this.rating = rating;
    }

    public TripAdvice() {
        // Required for Firestore deserialization
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public List<DocumentReference> getDestinations() {
        return destinations;
    }

    public String getEstimatedCost() {
        return estimatedCost;
    }

    public Double getRating() {
        return rating;
    }

    public String getFood() {
        return food;
    }

    public void setFood(String food) {
        this.food = food;
    }
}
