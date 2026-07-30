package com.emranhss.myapplication.model.request;

import lombok.Data;

/**
 * Mirrors the Angular `CustomerModel` used by add-customer.ts / customer.service.ts.
 */
@Data
public class CustomerRegisterRequest {

    private String name;
    private String email;
    private String phone;
    private String password;
    private String address;
    private String gender;
    private String dob;
    private Long policeStationId;
}
