package com.emranhss.myapplication.model;

import lombok.Data;

@Data
public class District {

    private Long id;
    private String name;
    private String nameBn;
    private String districtCode;
    private Boolean active;
    private Long divisionId;
    private String divisionName;
    private Long countryId;
    private String countryName;
    private String countryCode;
    private int totalPoliceStations;
}
