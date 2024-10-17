package com.sweng.scopehud.util;

import android.location.Location;

import java.util.Date;

public class ScopeZero {
    private int distance;
    private int windage;
    private int elevation;
    private Date date;
    private Location location;

    public ScopeZero(int distance, int windage, int elevation, Date date, Location location) {
        this.distance = distance;
        this.windage = windage;
        this.elevation = elevation;
        this.date = date;
        this.location = location;
    }
}
