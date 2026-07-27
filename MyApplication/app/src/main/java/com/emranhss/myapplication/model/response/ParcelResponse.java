package com.emranhss.myapplication.model.response;

import com.emranhss.myapplication.model.HistoryEntry;

import java.util.List;

import lombok.Data;

@Data
public class ParcelResponse {
    private Long id;
    private String trackingCode;

    private String senderName;
    private String senderPhone;
    private String senderAddress;

    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;

    private String originPoliceStation;
    private String destinationPoliceStation;

    private String parcelType;
    private Double weight;

    private String description;
    private String specialInstructions;

    private String serviceType;
    private String priority;

    private Double deliveryCharge;
    private Double codAmount;

    private String paymentMethod;
    private String paymentStatus;

    private String status;

    private String estimatedDelivery;
    private String createdAt;
    private String updatedAt;

    private Long customerId;
    private String customerName;
    private String customerPhone;

    private Long riderId;
    private String riderName;

    private List<HistoryEntry> history;

}
