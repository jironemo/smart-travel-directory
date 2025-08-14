package com.smarttravel.myanmar;

public class Destination {
    private String id;
    private String name;
    private String description;
    private String category;
    private String imageUrl;
    private double rating;
    private String location;
    private boolean isPopular;

    public Destination() {}

    public Destination(String name, String description, String division, String category,
                       String imageUrl, double rating, String location, boolean isPopular) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.location = location;
        this.isPopular = isPopular;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }


    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public boolean isPopular() { return isPopular; }
    public void setPopular(boolean popular) { isPopular = popular; }
}