package com.sweng.scopehud;

import java.util.Objects;

public class Range {
    private String name;
    private double latitude;
    private double longitude;

    /**
     * Constructor for creating a Range object.
     *
     * @param name      The name of the range.
     * @param latitude  The latitude coordinate of the range.
     * @param longitude The longitude coordinate of the range.
     */
    public Range(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Gets the name of the range.
     *
     * @return The name of the range.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the latitude of the range.
     *
     * @return The latitude coordinate of the range.
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Gets the longitude of the range.
     *
     * @return The longitude coordinate of the range.
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Checks if two Range objects are equal by comparing their name, latitude, and longitude.
     *
     * @param o The object to compare against.
     * @return True if the objects are equal, otherwise false.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Range)) return false;
        Range range = (Range) o;
        return Double.compare(range.latitude, latitude) == 0 && Double.compare(range.longitude, longitude) == 0 && Objects.equals(name, range.name);
    }

    /**
     * Generates a hash code for the Range object based on its name, latitude, and longitude.
     *
     * @return The hash code of the Range object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, latitude, longitude);
    }
}
