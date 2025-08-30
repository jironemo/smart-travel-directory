package com.smarttravel.myanmar;

public class Location implements java.io.Serializable {
    private String id;
    private String name;
    private String division;
    private String description;

    public Location() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDivision() { return division; }
    public void setDivision(String division) { this.division = division; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
