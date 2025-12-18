package com.example.runningapp.run.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * GeoJSON LineString representation for storing running paths.
 * Each coordinate is [longitude, latitude, altitude (optional)]
 * 
 * Format follows GeoJSON RFC 7946:
 * {
 *   "type": "LineString",
 *   "coordinates": [
 *     [longitude, latitude],
 *     [longitude, latitude],
 *     ...
 *   ]
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeoJsonLineString {
    
    private String type = "LineString";
    
    /**
     * List of coordinates in [longitude, latitude] format.
     * Each inner list should have 2 elements: [lng, lat]
     * Optional 3rd element for altitude: [lng, lat, alt]
     */
    private List<List<Double>> coordinates;

    public GeoJsonLineString(List<List<Double>> coordinates) {
        this.type = "LineString";
        this.coordinates = coordinates;
    }
}
