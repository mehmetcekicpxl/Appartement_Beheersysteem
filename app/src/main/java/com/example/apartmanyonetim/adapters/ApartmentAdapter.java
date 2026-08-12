package com.example.apartmanyonetim.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apartmanyonetim.R;
import com.example.apartmanyonetim.models.Apartment;
import java.util.List;

// Adapter voor de lijst met appartementen
public class ApartmentAdapter extends RecyclerView.Adapter<ApartmentAdapter.ViewHolder> {

    private List<Apartment> apartmentList;
    private OnItemClickListener listener;

    // Interface voor klikgebeurtenissen
    public interface OnItemClickListener {
        void onItemClick(Apartment apartment);
    }

    // Constructor
    public ApartmentAdapter(List<Apartment> apartmentList, OnItemClickListener listener) {
        this.apartmentList = apartmentList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Layout "item_apartment" opblazen
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_apartment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Apartment apartment = apartmentList.get(position);
        
        // Gegevens binden aan de views
        holder.tvDoorNumber.setText("Daire " + apartment.getDoorNumber());
        
        // Toon huurdersnaam
        String tenant = apartment.getTenantName();
        if (tenant != null && !tenant.isEmpty()) {
            holder.tvTenantName.setText("Kiracı: " + tenant);
        } else {
            holder.tvTenantName.setText("Kiracı: -");
        } 

        // Klikgebeurtenis instellen
        holder.itemView.setOnClickListener(v -> listener.onItemClick(apartment));
    }

    @Override
    public int getItemCount() {
        return apartmentList.size();
    }

    // ViewHolder klasse
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDoorNumber;
        TextView tvTenantName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDoorNumber = itemView.findViewById(R.id.tvApartmentDoorNumber);
            tvTenantName = itemView.findViewById(R.id.tvApartmentTenant);
        }
    }
}
