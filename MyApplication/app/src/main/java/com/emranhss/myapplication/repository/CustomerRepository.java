package com.emranhss.myapplication.repository;

import android.content.Context;

import com.emranhss.myapplication.api.ApiClient;
import com.emranhss.myapplication.api.ApiService;
import com.emranhss.myapplication.model.response.CustomerResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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

    /**
     * @param imagePart nullable — pass null when the user didn't pick a photo.
     */
    public void registerCustomer(RequestBody customerJson,
                                 MultipartBody.Part imagePart,
                                 Callback<CustomerResponse> callback) {

        Call<CustomerResponse> call =
                apiService.registerCustomer(customerJson, imagePart);

        call.enqueue(callback);

    }

}
