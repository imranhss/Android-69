package com.emranhss.myapplication;


import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.emranhss.myapplication.api.ApiClient;
import com.emranhss.myapplication.api.ApiService;
import com.emranhss.myapplication.model.Country;
import com.emranhss.myapplication.model.District;
import com.emranhss.myapplication.model.Division;
import com.emranhss.myapplication.model.PoliceStation;
import com.emranhss.myapplication.model.request.ParcelRequest;
import com.emranhss.myapplication.model.response.CustomerResponse;
import com.emranhss.myapplication.model.response.ParcelResponse;
import com.emranhss.myapplication.session.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerBookActivity extends AppCompatActivity {

    ApiService api;

    // Selected display names, used to build the full address string
    private String senderCountryName = "";
    private String senderDivisionName = "";
    private String senderDistrictName = "";
    private String senderPoliceName = "";

    private String receiverCountryName = "";
    private String receiverDivisionName = "";
    private String receiverDistrictName = "";
    private String receiverPoliceName = "";

    // Sender
    private TextInputEditText etSenderName;
    private TextInputEditText etSenderPhone;
    private TextInputEditText etSenderAddress;

    // Receiver
    private TextInputEditText etReceiverName;
    private TextInputEditText etReceiverPhone;
    private TextInputEditText etReceiverAddress;

    // Parcel
    private TextInputEditText etWeight;
    private TextInputEditText etCodAmount;

    private TextView txtDeliveryCharge;

    private double deliveryCharge = 0;

    private Long customerId;

    private Long selectedOriginPoliceId;

    private Long selectedDestinationPoliceId;

    // Dropdowns
    private AutoCompleteTextView spCountry;
    private AutoCompleteTextView spDivision;
    private AutoCompleteTextView spDistrict;

    private AutoCompleteTextView rpCountry;
    private AutoCompleteTextView rpDivision;
    private AutoCompleteTextView rpDistrict;

    private AutoCompleteTextView spOriginPolice;
    private AutoCompleteTextView spDestinationPolice;

    private AutoCompleteTextView spParcelType;
    private AutoCompleteTextView spServiceType;
    private AutoCompleteTextView spPriority;
    private AutoCompleteTextView spPaymentMethod;

    private MaterialButton btnBookParcel;

    // Data Lists
    // Split lists so sender and receiver never clobber each other
    private List<Division> senderDivisions = new ArrayList<>();
    private List<District> senderDistricts = new ArrayList<>();
    private List<PoliceStation> senderPoliceStations = new ArrayList<>();
    private List<Country> countries = new ArrayList<>();
    private List<Division> receiverDivisions = new ArrayList<>();
    private List<District> receiverDistricts = new ArrayList<>();
    private List<PoliceStation> receiverPoliceStations = new ArrayList<>();

    // Selected IDs
    private long countryId;
    private long divisionId;
    private long districtId;
    private long originPoliceId;
    private long destinationCountryId;
    private long destinationDivisionId;
    private long destinationDistrictId;
    private long destinationPoliceId;

    private final android.os.Handler handler =
            new android.os.Handler();

    private Runnable chargeRunnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_book);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> finish());

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        api = ApiClient.getClient(getApplicationContext());

        initViews();

        loadCountries();
        loadDestinationCountries();

        loadStaticDropdown();

        SessionManager sessionManager = new SessionManager(this);

        CustomerResponse customer = sessionManager.getCustomer();

        if (customer == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        customerId = customer.getId();
        selectedOriginPoliceId = customer.getPoliceStationId();

        spCountry.setOnItemClickListener((parent, view, position, id) -> {
            Country c = countries.get(position);
            countryId = c.getId();
            senderCountryName = c.getName();
            loadDivision(countryId);
        });

        rpCountry.setOnItemClickListener((parent, view, position, id) -> {
            Country c = countries.get(position);
            destinationCountryId = c.getId();
            receiverCountryName = c.getName();
            loadDestinationDivision(destinationCountryId);
        });

        spDivision.setOnItemClickListener((parent, view, position, id) -> {
            Division d = senderDivisions.get(position);
            divisionId = d.getId();
            senderDivisionName = d.getName();
            loadDistrict(divisionId);
        });

        rpDivision.setOnItemClickListener((parent, view, position, id) -> {
            Division d = receiverDivisions.get(position);
            destinationDivisionId = d.getId();
            receiverDivisionName = d.getName();
            loadDestinationDistrict(destinationDivisionId);
        });

        spDistrict.setOnItemClickListener((parent, view, position, id) -> {
            District d = senderDistricts.get(position);
            districtId = d.getId();
            senderDistrictName = d.getName();
            loadPoliceStation(districtId);
        });

        rpDistrict.setOnItemClickListener((parent, view, position, id) -> {
            District d = receiverDistricts.get(position);
            destinationDistrictId = d.getId();
            receiverDistrictName = d.getName();
            loadDestinationPoliceStation(destinationDistrictId);
        });

        spOriginPolice.setOnItemClickListener((parent, view, position, id) -> {
            PoliceStation p = senderPoliceStations.get(position);
            selectedOriginPoliceId = p.getId();   // <-- fixed: was writing to unused originPoliceId
            senderPoliceName = p.getName();
        });

        spDestinationPolice.setOnItemClickListener((parent, view, position, id) -> {
            PoliceStation p = receiverPoliceStations.get(position);
            selectedDestinationPoliceId = p.getId();
            receiverPoliceName = p.getName();
        });

        spServiceType.setOnItemClickListener((parent,view,pos,id)->{

            calculateCharge();

        });

        etWeight.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s,
                                          int start,
                                          int count,
                                          int after) {

            }

            @Override
            public void onTextChanged(CharSequence s,
                                      int start,
                                      int before,
                                      int count) {

                if(chargeRunnable!=null){

                    handler.removeCallbacks(chargeRunnable);

                }

                chargeRunnable = () -> calculateCharge();

                handler.postDelayed(chargeRunnable,500);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }

        });

        btnBookParcel.setOnClickListener(v -> bookParcel());


    }

    private void initViews(){

        etSenderName=findViewById(R.id.etSenderName);
        etSenderPhone=findViewById(R.id.etSenderPhone);
        etSenderAddress=findViewById(R.id.etSenderAddress);

        etReceiverName=findViewById(R.id.etReceiverName);
        etReceiverPhone=findViewById(R.id.etReceiverPhone);
        etReceiverAddress=findViewById(R.id.etReceiverAddress);

        etWeight=findViewById(R.id.etWeight);
        etCodAmount=findViewById(R.id.etCodAmount);

        spCountry=findViewById(R.id.spCountry);
        spDivision=findViewById(R.id.spDivision);
        spDistrict=findViewById(R.id.spDistrict);
        spOriginPolice=findViewById(R.id.spOriginPolice);



//        Receiver
        rpCountry=findViewById(R.id.rpCountry);
        rpDivision=findViewById(R.id.rpDivision);
        rpDistrict=findViewById(R.id.rpDistrict);
        spDestinationPolice=findViewById(R.id.spDestinationPolice);



        spParcelType=findViewById(R.id.spParcelType);
        spServiceType=findViewById(R.id.spServiceType);
        spPriority=findViewById(R.id.spPriority);
        spPaymentMethod=findViewById(R.id.spPaymentMethod);

        btnBookParcel=findViewById(R.id.btnBookParcel);

        txtDeliveryCharge = findViewById(R.id.txtDeliveryCharge);

    }

    private void loadStaticDropdown(){

        ArrayAdapter<String> parcelAdapter=new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                new String[]{
                        "DOCUMENT",
                        "PRODUCT",
                        "FRAGILE",
                        "HEAVY",
                        "PERISHABLE"
                });

        spParcelType.setAdapter(parcelAdapter);


        ArrayAdapter<String> serviceAdapter=new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                new String[]{
                        "STANDARD",
                        "EXPRESS",
                        "SAME_DAY",
                        "OVERNIGHT",
                });

        spServiceType.setAdapter(serviceAdapter);


        ArrayAdapter<String> priorityAdapter=new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                new String[]{
                        "URGENT",
                        "HIGH",
                        "NORMAL"
                });

        spPriority.setAdapter(priorityAdapter);


        ArrayAdapter<String> paymentAdapter=new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                new String[]{
                        "CASH",
                        "BKASH",
                        "COD",
                        "NAGAD",
                        "SSLCOMMERZ",
                        "PREPAID"
                });

        spPaymentMethod.setAdapter(paymentAdapter);

    }

    private void loadCountries(){

        api.getCountries().enqueue(new Callback<List<Country>>() {

            @Override
            public void onResponse(Call<List<Country>> call,
                                   Response<List<Country>> response) {

                if(response.isSuccessful() && response.body()!=null){

                    countries=response.body();

                    List<String> names=new ArrayList<>();

                    for(Country c:countries){
                        names.add(c.getName());
                    }

                    ArrayAdapter<String> adapter=new ArrayAdapter<>(
                            CustomerBookActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            names);

                    spCountry.setAdapter(adapter);

                }

            }

            @Override
            public void onFailure(Call<List<Country>> call,
                                  Throwable t) {

            }

        });

    }

    private void loadDestinationCountries(){

        api.getCountries().enqueue(new Callback<List<Country>>() {

            @Override
            public void onResponse(Call<List<Country>> call,
                                   Response<List<Country>> response) {

                if(response.isSuccessful() && response.body()!=null){

                    countries=response.body();

                    List<String> names=new ArrayList<>();

                    for(Country c:countries){
                        names.add(c.getName());
                    }

                    ArrayAdapter<String> adapter=new ArrayAdapter<>(
                            CustomerBookActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            names);

                    rpCountry.setAdapter(adapter);

                }

            }

            @Override
            public void onFailure(Call<List<Country>> call,
                                  Throwable t) {

            }

        });

    }


    private void loadDivision(long countryId){
        api.getDivisionByCountry(countryId).enqueue(new Callback<List<Division>>() {
            @Override
            public void onResponse(Call<List<Division>> call, Response<List<Division>> response) {
                if(response.isSuccessful() && response.body()!=null){
                    senderDivisions = response.body();
                    List<String> names = new ArrayList<>();
                    for(Division d : senderDivisions) names.add(d.getName());
                    spDivision.setAdapter(new ArrayAdapter<>(CustomerBookActivity.this,
                            android.R.layout.simple_dropdown_item_1line, names));
                }
            }
            @Override
            public void onFailure(Call<List<Division>> call, Throwable t) {}
        });
    }

    private void loadDestinationDivision(long destinationCountryId){
        api.getDivisionByCountry(destinationCountryId).enqueue(new Callback<List<Division>>() {
            @Override
            public void onResponse(Call<List<Division>> call, Response<List<Division>> response) {
                if(response.isSuccessful() && response.body()!=null){
                    receiverDivisions = response.body();
                    List<String> names = new ArrayList<>();
                    for(Division d : receiverDivisions) names.add(d.getName());
                    rpDivision.setAdapter(new ArrayAdapter<>(CustomerBookActivity.this,
                            android.R.layout.simple_dropdown_item_1line, names));
                }
            }
            @Override
            public void onFailure(Call<List<Division>> call, Throwable t) {}
        });
    }

    private void loadDistrict(long divisionId){
        api.getDistrictByDivision(divisionId).enqueue(new Callback<List<District>>() {
            @Override
            public void onResponse(Call<List<District>> call, Response<List<District>> response) {
                if(response.isSuccessful() && response.body()!=null){
                    senderDistricts = response.body();
                    List<String> names = new ArrayList<>();
                    for(District d : senderDistricts) names.add(d.getName());
                    spDistrict.setAdapter(new ArrayAdapter<>(CustomerBookActivity.this,
                            android.R.layout.simple_dropdown_item_1line, names));
                }
            }
            @Override
            public void onFailure(Call<List<District>> call, Throwable t) {}
        });
    }

    private void loadDestinationDistrict(long destinationDivisionId){
        api.getDistrictByDivision(destinationDivisionId).enqueue(new Callback<List<District>>() {
            @Override
            public void onResponse(Call<List<District>> call, Response<List<District>> response) {
                if(response.isSuccessful() && response.body()!=null){
                    receiverDistricts = response.body();
                    List<String> names = new ArrayList<>();
                    for(District d : receiverDistricts) names.add(d.getName());
                    rpDistrict.setAdapter(new ArrayAdapter<>(CustomerBookActivity.this,
                            android.R.layout.simple_dropdown_item_1line, names));
                }
            }
            @Override
            public void onFailure(Call<List<District>> call, Throwable t) {}
        });
    }

    private void loadPoliceStation(long districtId){
        api.getPoliceStationByDistrict(districtId).enqueue(new Callback<List<PoliceStation>>() {
            @Override
            public void onResponse(Call<List<PoliceStation>> call, Response<List<PoliceStation>> response) {
                if(response.isSuccessful() && response.body()!=null){
                    senderPoliceStations = response.body();
                    List<String> names = new ArrayList<>();
                    for(PoliceStation p : senderPoliceStations) names.add(p.getName());
                    spOriginPolice.setAdapter(new ArrayAdapter<>(CustomerBookActivity.this,
                            android.R.layout.simple_dropdown_item_1line, names)); // origin only
                }
            }
            @Override
            public void onFailure(Call<List<PoliceStation>> call, Throwable t) {}
        });
    }

    private void loadDestinationPoliceStation(long destinationDistrictId){
        api.getPoliceStationByDistrict(destinationDistrictId).enqueue(new Callback<List<PoliceStation>>() {
            @Override
            public void onResponse(Call<List<PoliceStation>> call, Response<List<PoliceStation>> response) {
                if(response.isSuccessful() && response.body()!=null){
                    receiverPoliceStations = response.body();
                    List<String> names = new ArrayList<>();
                    for(PoliceStation p : receiverPoliceStations) names.add(p.getName());
                    spDestinationPolice.setAdapter(new ArrayAdapter<>(CustomerBookActivity.this,
                            android.R.layout.simple_dropdown_item_1line, names)); // destination only
                }
            }
            @Override
            public void onFailure(Call<List<PoliceStation>> call, Throwable t) {}
        });
    }

    private void calculateCharge(){

        String weightText = etWeight.getText().toString().trim();

        if(weightText.isEmpty())
            return;

        if(spServiceType.getText().toString().isEmpty())
            return;

        double weight = Double.parseDouble(weightText);

        double cod = 0;

        if(!etCodAmount.getText().toString().trim().isEmpty()){
            cod = Double.parseDouble(etCodAmount.getText().toString().trim());
        }

        String service = spServiceType.getText().toString();

        api.calculateCharge(weight,service,cod)
                .enqueue(new Callback<Double>() {

                    @Override
                    public void onResponse(Call<Double> call,
                                           Response<Double> response) {

                        if(response.isSuccessful()
                                && response.body()!=null){

                            deliveryCharge = response.body();

                            txtDeliveryCharge.setText(
                                    "Delivery Charge : ৳ "
                                            + String.format("%.2f",
                                            deliveryCharge));

                        }

                    }

                    @Override
                    public void onFailure(Call<Double> call,
                                          Throwable t) {

                        txtDeliveryCharge.setText(
                                "Charge unavailable");

                    }

                });

    }


    private String buildFullAddress(String addressLine, String policeName,
                                    String districtName, String divisionName,
                                    String countryName) {

        StringBuilder sb = new StringBuilder();

        if (!addressLine.isEmpty()) sb.append(addressLine);

        for (String part : new String[]{policeName, districtName, divisionName, countryName}) {
            if (part != null && !part.trim().isEmpty()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(part.trim());
            }
        }

        return sb.toString();
    }

    private void bookParcel() {

        ParcelRequest request = new ParcelRequest();

        request.setCustomerId(customerId);

        request.setSenderName(etSenderName.getText().toString().trim());
        request.setSenderPhone(etSenderPhone.getText().toString().trim());


        request.setOriginPoliceStationId(selectedOriginPoliceId);

        request.setReceiverName(etReceiverName.getText().toString().trim());
        request.setReceiverPhone(etReceiverPhone.getText().toString().trim());


        request.setDestinationPoliceStationId(selectedDestinationPoliceId);

        request.setParcelType(spParcelType.getText().toString());

        request.setWeight(
                Double.parseDouble(etWeight.getText().toString())
        );

        String fullSenderAddress = buildFullAddress(
                etSenderAddress.getText().toString().trim(),
                senderPoliceName, senderDistrictName, senderDivisionName, senderCountryName);

        String fullReceiverAddress = buildFullAddress(
                etReceiverAddress.getText().toString().trim(),
                receiverPoliceName, receiverDistrictName, receiverDivisionName, receiverCountryName);

        request.setSenderAddress(fullSenderAddress);
        request.setReceiverAddress(fullReceiverAddress);

        request.setDescription("");

        request.setSpecialInstructions("");

        request.setServiceType(
                spServiceType.getText().toString()
        );

        request.setPriority(
                spPriority.getText().toString()
        );

        request.setPaymentMethod(
                spPaymentMethod.getText().toString()
        );

        if(etCodAmount.getText().toString().isEmpty()){
            request.setCodAmount(0.0);
        }else{
            request.setCodAmount(
                    Double.parseDouble(etCodAmount.getText().toString())
            );
        }

        api.bookParcel(request)
                .enqueue(new Callback<ParcelResponse>() {

                    @Override
                    public void onResponse(Call<ParcelResponse> call,
                                           Response<ParcelResponse> response) {

                        if(response.isSuccessful()){

                            Toast.makeText(
                                    CustomerBookActivity.this,
                                    "Parcel Booked Successfully",
                                    Toast.LENGTH_LONG
                            ).show();

                            finish();

                        }else{

                            Toast.makeText(
                                    CustomerBookActivity.this,
                                    "Booking Failed",
                                    Toast.LENGTH_SHORT
                            ).show();

                        }

                    }

                    @Override
                    public void onFailure(Call<ParcelResponse> call,
                                          Throwable t) {

                        Toast.makeText(
                                CustomerBookActivity.this,
                                t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();

                    }

                });

    }

}