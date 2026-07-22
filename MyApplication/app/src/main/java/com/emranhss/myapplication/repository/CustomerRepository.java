package com.emranhss.myapplication.repository;

import android.content.Context;

import com.emranhss.myapplication.api.ApiClient;
import com.emranhss.myapplication.api.ApiService;
import com.emranhss.myapplication.model.response.CustomerResponse;

import retrofit2.Call;
import retrofit2.Callback;

public class CustomerRepository {

    private final ApiService apiService;

    public CustomerRepository(Context context) {
        apiService = ApiClient.getClient(context);
    }

    public void getCustomerByUserId(Long userId,
                                    Callback<CustomerResponse> callback) {

        Call<CustomerResponse> call =
                apiService.getCustomerByUserId(userId);

        call.enqueue(callback);

    }

}
