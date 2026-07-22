package com.emranhss.myapplication.repository;

import android.content.Context;

import com.emranhss.myapplication.api.ApiClient;
import com.emranhss.myapplication.api.ApiService;
import com.emranhss.myapplication.model.request.LoginRequest;
import com.emranhss.myapplication.model.response.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;

public class AuthRepository {

    private final ApiService apiService;

    public AuthRepository(Context context) {
        apiService = ApiClient.getClient(context);
    }

    public void login(LoginRequest request,
                      Callback<LoginResponse> callback) {

        Call<LoginResponse> call = apiService.login(request);

        call.enqueue(callback);
    }

}
