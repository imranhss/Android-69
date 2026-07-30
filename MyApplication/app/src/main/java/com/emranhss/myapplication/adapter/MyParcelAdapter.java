package com.emranhss.myapplication.adapter;

import android.graphics.drawable.GradientDrawable;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.emranhss.myapplication.R;
import com.emranhss.myapplication.model.Parcel;
import com.emranhss.myapplication.model.response.ParcelResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Same visual treatment as {@link ParcelAdapter}, but bound to the full
 * {@link ParcelResponse} returned by the API so the row click can open a
 * details dialog without a second network round trip for basic fields.
 */
public class MyParcelAdapter extends RecyclerView.Adapter<MyParcelAdapter.ParcelViewHolder> {

    public interface OnParcelClickListener {
        void onParcelClick(ParcelResponse parcel);
    }

    private final List<ParcelResponse> parcels;
    private final OnParcelClickListener listener;

    public MyParcelAdapter(List<ParcelResponse> parcels, OnParcelClickListener listener) {
        this.parcels = parcels;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ParcelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_parcel, parent, false);
        return new ParcelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParcelViewHolder holder, int position) {
        ParcelResponse parcel = parcels.get(position);

        holder.txtTrackingCode.setText(parcel.getTrackingCode());
        holder.txtDate.setText(formatDate(parcel.getCreatedAt()));
        holder.txtStatus.setText(badgeLabel(parcel.getStatus()));

        GradientDrawable bg = (GradientDrawable) holder.txtStatus.getBackground().mutate();
        bg.setColor(badgeColor(holder.itemView, parcel.getStatus()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onParcelClick(parcel);
        });
    }

    @Override
    public int getItemCount() {
        return parcels.size();
    }

    private CharSequence formatDate(String raw) {
        if (raw == null) return "";
        try {
            SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = apiFormat.parse(raw);
            return DateFormat.format("MMM d, yyyy", date);
        } catch (ParseException e) {
            return raw;
        }
    }

    private String badgeLabel(String status) {
        if (status == null) return "Unknown";
        switch (status) {
            case Parcel.STATUS_PENDING:
                return "Pending";
            case Parcel.STATUS_IN_TRANSIT:
                return "In Transit";
            case Parcel.STATUS_DELIVERED:
                return "Delivered";
            case Parcel.STATUS_CANCELLED:
                return "Cancelled";
            default:
                return status;
        }
    }

    @ColorInt
    private int badgeColor(View context, String status) {
        int colorRes;
        if (status == null) {
            colorRes = R.color.colorMuted;
        } else {
            switch (status) {
                case Parcel.STATUS_PENDING:
                    colorRes = R.color.colorDanger;
                    break;
                case Parcel.STATUS_IN_TRANSIT:
                    colorRes = R.color.colorPrimary;
                    break;
                case Parcel.STATUS_DELIVERED:
                    colorRes = R.color.colorSuccess;
                    break;
                default:
                    colorRes = R.color.colorMuted;
            }
        }
        return context.getResources().getColor(colorRes, context.getContext().getTheme());
    }

    static class ParcelViewHolder extends RecyclerView.ViewHolder {
        TextView txtTrackingCode;
        TextView txtStatus;
        TextView txtDate;

        ParcelViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTrackingCode = itemView.findViewById(R.id.txtTrackingCode);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtDate = itemView.findViewById(R.id.txtDate);
        }
    }
}
