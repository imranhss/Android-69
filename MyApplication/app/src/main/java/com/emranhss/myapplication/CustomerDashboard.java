package com.emranhss.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.emranhss.myapplication.adapter.ParcelAdapter;

import com.emranhss.myapplication.model.Parcel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CustomerDashboard extends AppCompatActivity {

    // Base URL used to resolve the customer's profile image, same role as
    // `imageUrl + customer?.image` in the Angular template.
    private static final String IMAGE_BASE_URL = "https://your-api.example.com/uploads/";

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
        String name = "Jane Doe";
        String email = "jane.doe@example.com";
        String phone = "+1 555 123 4567";
        String role = "CUSTOMER";
        String customerImage = null; // e.g. "avatars/jane.jpg", or null if not set

        txtToolbarUserName.setText(name);
        txtUserName.setText(name);
        txtUserEmail.setText(email);
        txtUserPhone.setText(phone);
        txtUserRole.setText(role);

        if (!TextUtils.isEmpty(customerImage)) {
            Glide.with(this)
                    .load(IMAGE_BASE_URL + customerImage)
                    .placeholder(android.R.drawable.sym_def_app_icon)
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
        int total = recentParcelsPlaceholderTotal();
        int inTransit = 4;
        int delivered = 9;
        int pending = 2;

        txtStatTotalValue.setText(String.valueOf(total));
        txtStatTotalLabel.setText("Total Parcels");

        txtStatInTransitValue.setText(String.valueOf(inTransit));
        txtStatInTransitLabel.setText("In Transit");

        txtStatDeliveredValue.setText(String.valueOf(delivered));
        txtStatDeliveredLabel.setText("Delivered");

        txtStatPendingValue.setText(String.valueOf(pending));
        txtStatPendingLabel.setText("Pending");
    }

    private int recentParcelsPlaceholderTotal() {
        return 15;
    }

    /** Replace with real data loaded from your API/repository. */
    private void loadRecentParcels() {
        recentParcels.clear();
        recentParcels.add(new Parcel("PKG-000123", Parcel.STATUS_IN_TRANSIT, new Date()));
        recentParcels.add(new Parcel("PKG-000119", Parcel.STATUS_DELIVERED, new Date()));
        recentParcels.add(new Parcel("PKG-000108", Parcel.STATUS_PENDING, new Date()));

        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

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