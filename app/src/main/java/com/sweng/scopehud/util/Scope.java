package com.sweng.scopehud.util;

import android.location.Location;

import java.util.Date;

//Scopes are stored in the database
public class Scope {
    private int id; //database row id
    private String name; //user-configurable scope name
    private String brand; //brand of scope
    private Date zeroDate; //date on which the scope was last zeroed
    private Location zeroLocation; //location at which the scope was last zeroed
    private float maxMagnification; //maximum magnification of scope (1 for non magnified scopes)
    private boolean variableMagnification; //whether the scope has variable magnification


    public Scope() {

    }

    public String getName() {
        return name;
    }
}
