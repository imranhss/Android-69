package com.emranhss.myapplication.model;

import lombok.Data;

@Data
public class Division {

    private Long id;
    private String name;
    private String nameBn;
    private Boolean active;
    private Long countryId;
    private String countryName;
    private int totalDistricts;

}
