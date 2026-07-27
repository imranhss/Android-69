package com.emranhss.myapplication.model.request;

import lombok.Data;

@Data
public class ParcelRequest {

    private Long customerId;

    private String senderName;
    private String senderPhone;
    private String senderAddress;
    private Long originPoliceStationId;

    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private Long destinationPoliceStationId;

    private String parcelType;
    private Double weight;
    private String description;
    private String specialInstructions;

    private String serviceType;
    private String priority;

    private String paymentMethod;
    private Double codAmount;
}
