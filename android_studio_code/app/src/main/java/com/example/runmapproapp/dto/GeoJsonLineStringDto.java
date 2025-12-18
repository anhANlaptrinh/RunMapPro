package com.example.runmapproapp.dto;

import java.util.List;

public class GeoJsonLineStringDto {
    private String type = "LineString";
    private List<List<Double>> coordinates;

    // Constructor không tham số
    public GeoJsonLineStringDto() {
    }

    // Constructor với tham số
    public GeoJsonLineStringDto(List<List<Double>> coordinates) {
        this.coordinates = coordinates;
    }

    // Getters and Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public List<List<Double>> getCoordinates() { return coordinates; }
    public void setCoordinates(List<List<Double>> coordinates) { this.coordinates = coordinates; }
}
