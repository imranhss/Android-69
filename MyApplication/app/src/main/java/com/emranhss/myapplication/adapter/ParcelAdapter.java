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

import java.util.List;

public class ParcelAdapter extends RecyclerView.Adapter<ParcelAdapter.ParcelViewHolder> {

    public interface OnParcelClickListener {
        void onParcelClick(Parcel parcel);
    }

    private final List<Parcel> parcels;
    private final OnParcelClickListener listener;

    public ParcelAdapter(List<Parcel> parcels, OnParcelClickListener listener) {
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
        Parcel parcel = parcels.get(position);

        holder.txtTrackingCode.setText(parcel.getTrackingCode());
        holder.txtDate.setText(DateFormat.format("MMM d, yyyy", parcel.getCreatedAt()));

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

    /** Mirrors the Angular component's badgeLabel() helper. */
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

    /** Mirrors the Angular component's badgeClass() helper. */
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
