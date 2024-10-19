package com.sweng.scopehud.util;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Date;

public class ScopeZero implements Parcelable {
    private int distance;
    private float windage;
    private float elevation;
    private Date date;

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
