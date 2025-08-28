package com.smarttravel.myanmar;

public class Favourite {
    private String placeName;
    private String dateAdded;

    public Favourite() {}

    public Favourite(String placeName, String dateAdded) {
        this.placeName = placeName;
        this.dateAdded = dateAdded;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }
}

