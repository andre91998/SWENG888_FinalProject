package com.sweng.scopehud.util;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Date;

public class ScopeZero implements Parcelable {
    private int distance; // distance at which the scope was zeroed
    private float windage; // windage adjustment in MIL
    private float elevation; // elevation adjustment in MIL
    private Date date; // Date on which the scope was zeroed

    public int getDistance() {
        return distance;
    }

    public float getWindage() {
        return windage;
    }

    public float getElevation() {
        return elevation;
    }

    public Date getDate() {
        return date;
    }

    public ScopeZero(int distance, float windage, float elevation, Date date) {
        this.distance = distance;
        this.windage = windage;
        this.elevation = elevation;
        this.date = date;
    }

    protected ScopeZero(Parcel in) {
        distance = in.readInt();
        windage = in.readInt();
        elevation = in.readInt();
    }

    public static final Creator<ScopeZero> CREATOR = new Creator<ScopeZero>() {
        @Override
        public ScopeZero createFromParcel(Parcel in) {
            return new ScopeZero(in);
        }

        @Override
        public ScopeZero[] newArray(int size) {
            return new ScopeZero[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeInt(distance);
        parcel.writeFloat(windage);
        parcel.writeFloat(elevation);
    }
}
