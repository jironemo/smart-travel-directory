package com.smarttravel.myanmar;

import java.util.List;

public class Destination {
    private String id;
    private String name;
    private String description;
    private String category;
    private List<String> imageUrl;
    private double rating;
    private boolean isPopular;
    private boolean selected;
    private String address;
    private String contact;
    private String additionalInformation;
    private String division;
    private String locationName;

    public Destination() {}

    public Destination(String name, String description, String division, String category,
                       List<String> imageUrl, double rating, boolean isPopular) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.isPopular = isPopular;
        this.id = id;
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

    public List<String> getImageUrl() { return imageUrl; }
    public void setImageUrl(List<String> imageUrl) { this.imageUrl = imageUrl; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }


    public boolean isPopular() { return isPopular; }
    public void setPopular(boolean popular) { isPopular = popular; }

    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public String getAdditionalInformation() { return additionalInformation; }
    public void setAdditionalInformation(String additionalInformation) { this.additionalInformation = additionalInformation; }

    public String getDivision() { return division; }
    public void setDivision(String division) { this.division = division; }
    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }
}