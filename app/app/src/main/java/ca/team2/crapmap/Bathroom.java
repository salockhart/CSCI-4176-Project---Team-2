package ca.team2.crapmap;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by geoffreycaven on 2017-03-15.
 */

public class Bathroom implements Serializable {
    private String id;
    private String name;
    private Boolean requiresPurchase;
    private transient LatLng location;
    //how to deal with images?
    //TODO deal with images
    private ArrayList<Review> reviews;

    public Bathroom(String id, String name, Boolean requiresPurchase, String lat, String lng, ArrayList<Review> reviews) {
        this.id = id;
        this.name = name;
        this.requiresPurchase = requiresPurchase;
        this.location = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
        this.reviews = reviews;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getRequiresPurchase() { return requiresPurchase; }

    public void setRequiresPurchase(Boolean requiresPurchase) { this.requiresPurchase = requiresPurchase; }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public ArrayList<Review> getReviews() {
        return reviews;
    }

    public void setReviews(ArrayList<Review> reviews) {
        this.reviews = reviews;
    }

    @Override
    public String toString() {
        return "Bathroom{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", requiresPurchase" + requiresPurchase +
                ", location=" + location +
                ", reviews=" + reviews.toString() +
                '}';
    }
}
