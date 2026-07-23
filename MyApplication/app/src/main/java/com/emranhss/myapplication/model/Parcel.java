package com.emranhss.myapplication.model;

import java.util.Date;

public class Parcel {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_IN_TRANSIT = "IN_TRANSIT";
    public static final String STATUS_DELIVERED = "DELIVERED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    private String trackingCode;
    private String status;
    private Date createdAt;

    public Parcel(String trackingCode, String status, Date createdAt) {
        this.trackingCode = trackingCode;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getTrackingCode() {
        return trackingCode;
    }

    public String getStatus() {
        return status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
}

