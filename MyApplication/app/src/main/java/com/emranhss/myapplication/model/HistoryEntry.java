package com.emranhss.myapplication.model;

import lombok.Data;

@Data
public class HistoryEntry {

    private Long id;
    private String status;
    private String note;
    private String location;
    private String timestamp;
    private String performedBy;
    private Long riderId;
}
