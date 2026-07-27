package com.emranhss.myapplication.model;

import lombok.Data;

@Data
public class PoliceStation {

    private Long id;
    private String name;
    private String nameBn;
    private String postalCode;
    private Boolean active;
    private Long districtId;
    private String districtName;
    private Long divisionId;
    private String divisionName;
    private Long countryId;
    private String countryName;

}
