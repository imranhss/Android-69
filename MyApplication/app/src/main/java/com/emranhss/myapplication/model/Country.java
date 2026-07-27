package com.emranhss.myapplication.model;

import lombok.Data;

@Data
public class Country {

    private Long id;
    private String name;
    private String code;
    private String phoneCode;
    private Boolean active;
    private int totalDivisions;
}
