package com.emranhss.myapplication.model.response;

import lombok.Data;

@Data
public class LoginResponse {

    private String  token;
    private String  tokenType = "Bearer";

    private Long    userId;
    private String  name;
    private String  email;
    private String  phone;
    private String  role;

    // Hub info — only set if role = AGENT
    private Long    hubId;
    private String  hubName;
}
