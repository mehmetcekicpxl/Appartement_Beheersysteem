package com.example.apartmanyonetim.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apartmanyonetim.R;
import com.example.apartmanyonetim.models.Apartment;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.util.List;
import java.util.Set;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.ViewHolder> {

    private List<Apartment> apartmentList;
    private Set<Integer> paidApartmentIds;
    private OnPaymentToggleListener listener;

    public interface OnPaymentToggleListener {
        void onToggle(Apartment apartment, boolean isPaid);
    }

    public PaymentAdapter(List<Apartment> apartmentList, Set<Integer> paidApartmentIds, OnPaymentToggleListener listener) {
        this.apartmentList = apartmentList;
        this.paidApartmentIds = paidApartmentIds;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payment_apartment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Apartment apartment = apartmentList.get(position);
        boolean isPaid = paidApartmentIds.contains(apartment.getId());

        // Gegevens instellen
        holder.tvDoorNumber.setText("Daire " + apartment.getDoorNumber());
        
        if (apartment.getTenantName() != null && !apartment.getTenantName().isEmpty()) {
            holder.tvTenantName.setText(apartment.getTenantName());
        } else {
            holder.tvTenantName.setText("Boş");
        }
        
        holder.tvAmount.setText(String.format("%.0f TL", apartment.getRentAmount())); // Geen decimalen voor schoner beeld

        // Switch status instellen zonder listener te triggeren
        holder.switchStatus.setOnCheckedChangeListener(null);
        holder.switchStatus.setChecked(isPaid);
        
        // UI updates op basis van status (Iconen en Kleuren)
        int greenColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.colorPaid);
        int redColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.colorUnpaid);
        int greenBg = Color.parseColor("#E8F5E9"); // Light Green
        int redBg = Color.parseColor("#FFEBEE"); // Light Red

        if (isPaid) {
            holder.ivStatusIcon.setImageResource(R.drawable.ic_check_circle);
            holder.ivStatusIcon.setImageTintList(ColorStateList.valueOf(greenColor));
            holder.ivStatusIcon.setBackgroundTintList(ColorStateList.valueOf(greenBg));
            holder.tvAmount.setTextColor(greenColor);
        } else {
            holder.ivStatusIcon.setImageResource(R.drawable.ic_close_circle);
            holder.ivStatusIcon.setImageTintList(ColorStateList.valueOf(redColor));
            holder.ivStatusIcon.setBackgroundTintList(ColorStateList.valueOf(redBg));
            holder.tvAmount.setTextColor(redColor);
        }

        holder.switchStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            listener.onToggle(apartment, isChecked);
            // Update lokale set voor snelle UI respons
            if (isChecked) paidApartmentIds.add(apartment.getId());
            else paidApartmentIds.remove(apartment.getId());
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return apartmentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDoorNumber;
        TextView tvTenantName;
        TextView tvAmount;
        SwitchMaterial switchStatus;
        ImageView ivStatusIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDoorNumber = itemView.findViewById(R.id.tvPaymentDoorNumber);
            tvTenantName = itemView.findViewById(R.id.tvPaymentTenantName);
            tvAmount = itemView.findViewById(R.id.tvPaymentAmount);
            switchStatus = itemView.findViewById(R.id.switchPaymentStatus);
            ivStatusIcon = itemView.findViewById(R.id.ivStatusIcon);
        }
    }
}
