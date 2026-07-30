package com.emranhss.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.emranhss.myapplication.api.ApiClient;
import com.emranhss.myapplication.api.ApiService;
import com.emranhss.myapplication.model.response.CustomerResponse;
import com.emranhss.myapplication.model.response.ParcelResponse;
import com.emranhss.myapplication.session.SessionManager;
import com.google.android.material.card.MaterialCardView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.emranhss.myapplication.adapter.ParcelAdapter;

import com.emranhss.myapplication.model.Parcel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerDashboard extends AppCompatActivity {

    // Base URL used to resolve the customer's profile image, same role as
    // `imageUrl + customer?.image` in the Angular template.
//    private static final String IMAGE_BASE_URL = "https://your-api.example.com/uploads/";

    private TextView txtToolbarUserName;
    private TextView txtUserName, txtUserEmail, txtUserPhone, txtUserRole, txtAddPhoto;
    private ImageView imgAvatar;

    private TextView txtStatTotalValue, txtStatTotalLabel;
    private TextView txtStatInTransitValue, txtStatInTransitLabel;
    private TextView txtStatDeliveredValue, txtStatDeliveredLabel;
    private TextView txtStatPendingValue, txtStatPendingLabel;

    private MaterialCardView cardSendParcel, cardTrackParcel;
    private TextView txtViewAll;

    private RecyclerView recyclerParcels;
    private View layoutEmptyState;
    private ParcelAdapter adapter;
    private final List<Parcel> recentParcels = new ArrayList<>();

    int recentPaercelNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_dashboard);

        bindViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();

        loadUserData();
        loadStats();
        loadRecentParcels();
    }

    private void bindViews() {

        txtToolbarUserName = findViewById(R.id.txtToolbarUserName);

        imgAvatar = findViewById(R.id.imgAvatar);
        txtUserName = findViewById(R.id.txtUserName);
        txtUserEmail = findViewById(R.id.txtUserEmail);
        txtUserPhone = findViewById(R.id.txtUserPhone);
        txtUserRole = findViewById(R.id.txtUserRole);
        txtAddPhoto = findViewById(R.id.txtAddPhoto);

        // Stat cards were included via <include>, so grab their inner views by container id.
        View statTotal = findViewById(R.id.statTotal);
        txtStatTotalValue = statTotal.findViewById(R.id.txtStatValue);
        txtStatTotalLabel = statTotal.findViewById(R.id.txtStatLabel);

        View statInTransit = findViewById(R.id.statInTransit);
        txtStatInTransitValue = statInTransit.findViewById(R.id.txtStatValue);
        txtStatInTransitLabel = statInTransit.findViewById(R.id.txtStatLabel);

        View statDelivered = findViewById(R.id.statDelivered);
        txtStatDeliveredValue = statDelivered.findViewById(R.id.txtStatValue);
        txtStatDeliveredLabel = statDelivered.findViewById(R.id.txtStatLabel);

        View statPending = findViewById(R.id.statPending);
        txtStatPendingValue = statPending.findViewById(R.id.txtStatValue);
        txtStatPendingLabel = statPending.findViewById(R.id.txtStatLabel);

        cardSendParcel = findViewById(R.id.cardSendParcel);
        cardTrackParcel = findViewById(R.id.cardTrackParcel);
        txtViewAll = findViewById(R.id.txtViewAll);

        recyclerParcels = findViewById(R.id.recyclerParcels);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        View btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> logout());
    }

    private void setupRecyclerView() {
        recyclerParcels.setLayoutManager(new LinearLayoutManager(this));
        recyclerParcels.setHasFixedSize(false);
        adapter = new ParcelAdapter(recentParcels, parcel -> {
            // Equivalent of routerLink to the parcel details / tracking screen.
            Intent intent = new Intent(this, MyParcelActivity.class);
            intent.putExtra("trackingCode", parcel.getTrackingCode());
            startActivity(intent);
        });
        recyclerParcels.setAdapter(adapter);
    }

    private void setupClickListeners() {
        cardSendParcel.setOnClickListener(v ->
                startActivity(new Intent(this, CustomerBookActivity.class)));

        cardTrackParcel.setOnClickListener(v ->
                startActivity(new Intent(this, MyParcelActivity.class)));

        txtViewAll.setOnClickListener(v ->
                startActivity(new Intent(this, MyParcelActivity.class)));
    }

    /** Replace with your real user/session source (SharedPreferences, ViewModel, API, etc). */
    private void loadUserData() {

        String imageUrl = ApiClient.IMAGE_URL + "/customer";

        SessionManager sessionManager = new SessionManager(this);

        CustomerResponse customer = sessionManager.getCustomer();

        if (customer == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String name = customer.getName();
        String email = customer.getEmail();
        String phone = customer.getPhone();
        String role = "CUSTOMER";
        String customerImage = imageUrl + customer.getImage(); // e.g. "avatars/jane.jpg", or null if not set

        txtToolbarUserName.setText(name);
        txtUserName.setText(name);
        txtUserEmail.setText(email);
        txtUserPhone.setText(phone);
        txtUserRole.setText(role);

        if (!TextUtils.isEmpty(customerImage)) {
            Glide.with(this)
                    .load(customerImage)
                    .placeholder(android.R.drawable.sym_def_app_icon)
                    .error(android.R.drawable.sym_def_app_icon)
                    .circleCrop()
                    .into(imgAvatar);
            txtAddPhoto.setVisibility(View.GONE);
        } else {
            imgAvatar.setImageResource(android.R.drawable.sym_def_app_icon);
            txtAddPhoto.setVisibility(View.VISIBLE);
        }
    }

    /** Replace with real counts from your parcels API/repository. */
    private void loadStats() {

        txtStatTotalLabel.setText("Total Parcels");

        txtStatInTransitLabel.setText("In Transit");

        txtStatDeliveredLabel.setText("Delivered");

        txtStatPendingLabel.setText("Pending");

    }



    /** Replace with real data loaded from your API/repository. */
    private void loadRecentParcels() {

        SessionManager sessionManager = new SessionManager(this);
        CustomerResponse customer = sessionManager.getCustomer();

        if (customer == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }

        Long customerId = customer.getId();

        ApiService apiService = ApiClient.getClient(getApplicationContext());

        apiService.getCustomerParcels(customerId)
                .enqueue(new Callback<List<ParcelResponse>>() {
                    @Override
                    public void onResponse(Call<List<ParcelResponse>> call,
                                           Response<List<ParcelResponse>> response) {

                        int pendingCount = 0;
                        int inTransitCount = 0;
                        int deliveredCount = 0;

                        if (response.isSuccessful() && response.body() != null) {

                            recentParcels.clear();

                            for (ParcelResponse item : response.body()) {

                                SimpleDateFormat apiFormat =
                                        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

                                Date date;

                                try {
                                    date = apiFormat.parse(item.getCreatedAt());
                                } catch (ParseException e) {
                                    date = new Date();
                                }

                                Parcel parcel = new Parcel(
                                        item.getTrackingCode(),
                                        item.getStatus(),
                                        date   // convert if necessary
                                );

                                recentParcels.add(parcel);

                                // Count status
                                if ("PENDING".equalsIgnoreCase(item.getStatus())) {
                                    pendingCount++;
                                } else if ("IN_TRANSIT".equalsIgnoreCase(item.getStatus())) {
                                    inTransitCount++;
                                } else if ("DELIVERED".equalsIgnoreCase(item.getStatus())) {
                                    deliveredCount++;
                                }
                            }

                            recentPaercelNumber = recentParcels.size();
                            txtStatTotalValue.setText(String.valueOf(recentPaercelNumber));

                            txtStatDeliveredValue.setText(String.valueOf(deliveredCount));
                            txtStatPendingValue.setText(String.valueOf(pendingCount));
                            txtStatInTransitValue.setText(String.valueOf(inTransitCount));

                            adapter.notifyDataSetChanged();
                            updateEmptyState();

                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "No parcels found",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ParcelResponse>> call, Throwable t) {

                        Toast.makeText(getApplicationContext(),
                                t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

//    private int recentParcelsPlaceholderTotal() {
//        System.out.println(recentPaercelNumber);
//        return 15;
//    }

    private void updateEmptyState() {
        boolean hasParcels = !recentParcels.isEmpty();
        recyclerParcels.setVisibility(hasParcels ? View.VISIBLE : View.GONE);
        layoutEmptyState.setVisibility(hasParcels ? View.GONE : View.VISIBLE);
        txtViewAll.setVisibility(hasParcels ? View.VISIBLE : View.GONE);
    }

    private void logout() {
        // TODO: clear session / tokens here.
        Intent intent = new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}