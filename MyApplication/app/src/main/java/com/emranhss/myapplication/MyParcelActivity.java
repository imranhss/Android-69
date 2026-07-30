package com.emranhss.myapplication;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.emranhss.myapplication.adapter.MyParcelAdapter;
import com.emranhss.myapplication.api.ApiClient;
import com.emranhss.myapplication.api.ApiService;
import com.emranhss.myapplication.model.HistoryEntry;
import com.emranhss.myapplication.model.Parcel;
import com.emranhss.myapplication.model.response.CustomerResponse;
import com.emranhss.myapplication.model.response.ParcelResponse;
import com.emranhss.myapplication.session.SessionManager;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyParcelActivity extends AppCompatActivity {

    private RecyclerView recyclerParcels;
    private View layoutEmptyState;
    private View progressLoading;
    private android.widget.EditText edtSearch;
    private ChipGroup chipGroupStatus;

    private MyParcelAdapter adapter;

    /** Full unfiltered list fetched from the API. */
    private final List<ParcelResponse> allParcels = new ArrayList<>();
    /** Currently displayed (search + status filtered) list — backs the adapter. */
    private final List<ParcelResponse> visibleParcels = new ArrayList<>();

    private String currentStatusFilter = null; // null = All
    private String currentSearchTerm = "";

    private ApiService apiService;
    private Long customerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_parcel);

        SessionManager sessionManager = new SessionManager(this);
        CustomerResponse customer = sessionManager.getCustomer();
        if (customer == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        customerId = customer.getId();
        apiService = ApiClient.getClient(getApplicationContext());

        bindViews();
        setupToolbar();
        setupRecyclerView();
        setupSearch();
        setupFilters();

        loadParcels();
    }

    private void bindViews() {
        recyclerParcels = findViewById(R.id.recyclerParcels);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        progressLoading = findViewById(R.id.progressLoading);
        edtSearch = findViewById(R.id.edtSearch);
        chipGroupStatus = findViewById(R.id.chipGroupStatus);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnRefresh).setOnClickListener(v -> loadParcels());
    }

    private void setupRecyclerView() {
        recyclerParcels.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyParcelAdapter(visibleParcels, this::showParcelDetails);
        recyclerParcels.setAdapter(adapter);
    }

    private void setupSearch() {
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchTerm = s.toString().trim();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilters() {
        chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentStatusFilter = null;
            } else {
                int id = checkedIds.get(0);
                if (id == R.id.chipPending) {
                    currentStatusFilter = Parcel.STATUS_PENDING;
                } else if (id == R.id.chipInTransit) {
                    currentStatusFilter = Parcel.STATUS_IN_TRANSIT;
                } else if (id == R.id.chipDelivered) {
                    currentStatusFilter = Parcel.STATUS_DELIVERED;
                } else if (id == R.id.chipCancelled) {
                    currentStatusFilter = Parcel.STATUS_CANCELLED;
                } else {
                    currentStatusFilter = null; // "All" chip
                }
            }
            applyFilters();
        });
    }

    private void loadParcels() {
        showLoading(true);

        apiService.getCustomerParcels(customerId).enqueue(new Callback<List<ParcelResponse>>() {
            @Override
            public void onResponse(Call<List<ParcelResponse>> call, Response<List<ParcelResponse>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    allParcels.clear();
                    allParcels.addAll(response.body());
                    applyFilters();
                } else {
                    allParcels.clear();
                    applyFilters();
                    Toast.makeText(MyParcelActivity.this, "Could not load parcels", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ParcelResponse>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(MyParcelActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /** Applies the current search term + status chip to allParcels and refreshes the list. */
    private void applyFilters() {
        visibleParcels.clear();

        for (ParcelResponse parcel : allParcels) {
            boolean matchesStatus = currentStatusFilter == null
                    || currentStatusFilter.equalsIgnoreCase(parcel.getStatus());

            boolean matchesSearch = currentSearchTerm.isEmpty()
                    || (parcel.getTrackingCode() != null
                        && parcel.getTrackingCode().toUpperCase()
                                .contains(currentSearchTerm.toUpperCase()));

            if (matchesStatus && matchesSearch) {
                visibleParcels.add(parcel);
            }
        }

        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void showLoading(boolean loading) {
        progressLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (loading) {
            recyclerParcels.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.GONE);
        }
    }

    private void updateEmptyState() {
        boolean hasParcels = !visibleParcels.isEmpty();
        recyclerParcels.setVisibility(hasParcels ? View.VISIBLE : View.GONE);
        layoutEmptyState.setVisibility(hasParcels ? View.GONE : View.VISIBLE);
    }

    // ===== Parcel details dialog =====

    private void showParcelDetails(ParcelResponse summary) {
        // Fetch the full record so we're sure to have history + every field,
        // even if the list endpoint returns a trimmed payload.
        apiService.getParcel(summary.getId()).enqueue(new Callback<ParcelResponse>() {
            @Override
            public void onResponse(Call<ParcelResponse> call, Response<ParcelResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    renderParcelDialog(response.body());
                } else {
                    renderParcelDialog(summary); // fall back to what we already have
                }
            }

            @Override
            public void onFailure(Call<ParcelResponse> call, Throwable t) {
                renderParcelDialog(summary);
            }
        });
    }

    private void renderParcelDialog(ParcelResponse parcel) {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_parcel_details, null, false);

        TextView txtTrackingCode = dialogView.findViewById(R.id.txtDialogTrackingCode);
        TextView txtStatus = dialogView.findViewById(R.id.txtDialogStatus);
        TextView txtSender = dialogView.findViewById(R.id.txtDialogSender);
        TextView txtReceiver = dialogView.findViewById(R.id.txtDialogReceiver);
        TextView txtParcelInfo = dialogView.findViewById(R.id.txtDialogParcelInfo);
        TextView txtHistoryLabel = dialogView.findViewById(R.id.txtDialogHistoryLabel);
        LinearLayout layoutHistory = dialogView.findViewById(R.id.layoutDialogHistory);
        Button btnCancelParcel = dialogView.findViewById(R.id.btnCancelParcel);

        txtTrackingCode.setText(parcel.getTrackingCode());
        txtStatus.setText(statusLabel(parcel.getStatus()));
        ((android.graphics.drawable.GradientDrawable) txtStatus.getBackground().mutate())
                .setColor(getResources().getColor(statusColorRes(parcel.getStatus()), getTheme()));

        txtSender.setText(String.format("%s · %s\n%s",
                nullToDash(parcel.getSenderName()),
                nullToDash(parcel.getSenderPhone()),
                nullToDash(parcel.getSenderAddress())));

        txtReceiver.setText(String.format("%s · %s\n%s",
                nullToDash(parcel.getReceiverName()),
                nullToDash(parcel.getReceiverPhone()),
                nullToDash(parcel.getReceiverAddress())));

        txtParcelInfo.setText(String.format(
                "Type: %s · Weight: %s kg\nService: %s · Priority: %s\nCharge: %s · COD: %s\nPayment: %s (%s)",
                nullToDash(parcel.getParcelType()),
                parcel.getWeight() != null ? parcel.getWeight() : "-",
                nullToDash(parcel.getServiceType()),
                nullToDash(parcel.getPriority()),
                parcel.getDeliveryCharge() != null ? parcel.getDeliveryCharge() : "-",
                parcel.getCodAmount() != null ? parcel.getCodAmount() : "0",
                nullToDash(parcel.getPaymentMethod()),
                nullToDash(parcel.getPaymentStatus())));

        List<HistoryEntry> history = parcel.getHistory();
        if (history == null || history.isEmpty()) {
            txtHistoryLabel.setVisibility(View.GONE);
        } else {
            txtHistoryLabel.setVisibility(View.VISIBLE);
            layoutHistory.removeAllViews();
            for (HistoryEntry entry : history) {
                TextView row = new TextView(this);
                row.setTextColor(getResources().getColor(R.color.colorDark, getTheme()));
                row.setTextSize(13f);
                row.setPadding(0, 8, 0, 8);
                row.setText(String.format("• %s — %s%s",
                        statusLabel(entry.getStatus()),
                        nullToDash(entry.getTimestamp()),
                        entry.getNote() != null ? "\n   " + entry.getNote() : ""));
                layoutHistory.addView(row);
            }
        }

        boolean canCancel = Parcel.STATUS_PENDING.equalsIgnoreCase(parcel.getStatus());
        btnCancelParcel.setVisibility(canCancel ? View.VISIBLE : View.GONE);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setNegativeButton("Close", null)
                .create();

        btnCancelParcel.setOnClickListener(v -> {
            dialog.dismiss();
            confirmCancelParcel(parcel);
        });

        dialog.show();
    }

    private void confirmCancelParcel(ParcelResponse parcel) {
        new AlertDialog.Builder(this)
                .setTitle("Cancel parcel?")
                .setMessage("This will cancel " + parcel.getTrackingCode() + ". This can't be undone.")
                .setPositiveButton("Yes, cancel it", (dialogInterface, which) -> cancelParcel(parcel))
                .setNegativeButton("Keep it", null)
                .show();
    }

    private void cancelParcel(ParcelResponse parcel) {
        apiService.cancelParcel(parcel.getId(), customerId).enqueue(new Callback<ParcelResponse>() {
            @Override
            public void onResponse(Call<ParcelResponse> call, Response<ParcelResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MyParcelActivity.this, "Parcel cancelled", Toast.LENGTH_SHORT).show();
                    loadParcels();
                } else {
                    Toast.makeText(MyParcelActivity.this, "Could not cancel parcel", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ParcelResponse> call, Throwable t) {
                Toast.makeText(MyParcelActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private String statusLabel(String status) {
        if (status == null) return "Unknown";
        switch (status) {
            case Parcel.STATUS_PENDING: return "Pending";
            case Parcel.STATUS_IN_TRANSIT: return "In Transit";
            case Parcel.STATUS_DELIVERED: return "Delivered";
            case Parcel.STATUS_CANCELLED: return "Cancelled";
            default: return status;
        }
    }

    private int statusColorRes(String status) {
        if (status == null) return R.color.colorMuted;
        switch (status) {
            case Parcel.STATUS_PENDING: return R.color.colorDanger;
            case Parcel.STATUS_IN_TRANSIT: return R.color.colorPrimary;
            case Parcel.STATUS_DELIVERED: return R.color.colorSuccess;
            default: return R.color.colorMuted;
        }
    }

    private String nullToDash(String value) {
        return value == null || value.isEmpty() ? "-" : value;
    }
}
