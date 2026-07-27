package com.emranhss.myapplication.api;

import com.emranhss.myapplication.model.Country;
import com.emranhss.myapplication.model.District;
import com.emranhss.myapplication.model.Division;
import com.emranhss.myapplication.model.PoliceStation;
import com.emranhss.myapplication.model.request.LoginRequest;
import com.emranhss.myapplication.model.request.ParcelRequest;
import com.emranhss.myapplication.model.response.CustomerResponse;
import com.emranhss.myapplication.model.response.LoginResponse;
import com.emranhss.myapplication.model.response.ParcelResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("api/customer/user/{id}")
    Call<CustomerResponse> getCustomerByUserId(@Path("id") Long id);


    @GET("api/country/")
    Call<List<Country>> getCountries();

    @GET("api/division/country/{id}")
    Call<List<Division>> getDivisions(
            @Path("id") Long countryId
    );

    @GET("api/district/{id}")
    Call<List<District>> getDistricts(
            @Path("id") Long divisionId
    );

    @GET("api/policeStation/district/{id}")
    Call<List<PoliceStation>> getPoliceStations(
            @Path("id") Long districtId
    );

    @GET("api/division/country/{id}")
    Call<List<Division>> getDivisionByCountry(
            @Path("id") long id);

    @GET("api/district/{id}")
    Call<List<District>> getDistrictByDivision(
            @Path("id") long id);

    @GET("api/policeStation/district/{id}")
    Call<List<PoliceStation>> getPoliceStationByDistrict(
            @Path("id") long id);


    @POST("api/parcels/book")
    Call<ParcelResponse> bookParcel(
            @Body ParcelRequest request
    );

    // Customer Parcels
    @GET("api/parcels/customer/{customerId}")
    Call<List<ParcelResponse>> getCustomerParcels(
            @Path("customerId") Long customerId
    );

    // Track Parcel
    @GET("api/parcels/track/{trackingCode}")
    Call<ParcelResponse> trackParcel(
            @Path("trackingCode") String trackingCode
    );

    // Cancel Parcel
    @PATCH("api/parcels/{id}/cancel")
    Call<ParcelResponse> cancelParcel(
            @Path("id") Long parcelId,
            @Query("customerId") Long customerId
    );

    // Calculate Charge
    @GET("api/parcels/calculate")
    Call<Double> calculateCharge(
            @Query("weight") double weight,
            @Query("serviceType") String serviceType,
            @Query("codAmount") double codAmount
    );

    // Parcel Details
    @GET("api/parcels/{id}")
    Call<ParcelResponse> getParcel(
            @Path("id") Long id
    );

}
