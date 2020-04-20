package com.example.hellomap;
//Name: yunyi zheng
//Student number:s1923021
//description: this block is used to construct the data that will show on the screen, reconstruct the string into better layout
public class LocationList {

    //Location name Lat and Lnt
    private String locationName;
    private double locationLat;
    private double locationLnt;

    @Override
    public String toString() {
        return "Address{" +
                "Location=" + locationName +
                ", Longitude='" + locationLat + '\'' +
                ", =" + locationLnt +
                '}';
    }
    public String getLocationName() {
        return locationName;
    }
    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }
    public double getLocationLat() {
        return locationLat;
    }
    public void setLocationLat(double locationLat) {
        this.locationLat = locationLat;
    }
    public double getLocationLnt() {
        return locationLnt;
    }
    public void setLocationLnt(double locationLnt) {
        this.locationLnt = locationLnt;
    }
}
