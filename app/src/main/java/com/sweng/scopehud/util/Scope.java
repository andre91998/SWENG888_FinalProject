package com.sweng.scopehud.util;

import android.location.Location;

import java.util.Date;

//Scopes are stored in the database
public class Scope {
    private int id; //database row id
    private String name; //user-configurable scope name
    private String brand; //brand of scope
    private float maxMagnification; //maximum magnification of scope (1 for non magnified scopes)
    private boolean variableMagnification; //whether the scope has variable magnification
    private ScopeZero scopeZero; //last scope zero data


    public Scope() {

    }

    public String getName() {
        return name;
    }
}
