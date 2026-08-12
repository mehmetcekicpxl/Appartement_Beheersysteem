package com.example.apartmanyonetim.fragments;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import com.example.apartmanyonetim.ApartmentDetailActivity;
import com.example.apartmanyonetim.DatabaseHelper;
import com.example.apartmanyonetim.R;
import com.example.apartmanyonetim.adapters.ApartmentAdapter;
import com.example.apartmanyonetim.models.Apartment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

// Fragment voor beheer van appartementen (Daireler)
public class ApartmentsFragment extends Fragment {

    private RecyclerView rvApartments;
    private ApartmentAdapter adapter;
    private List<Apartment> apartmentList;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_apartments, container, false);
        
        dbHelper = new DatabaseHelper(requireContext());
        rvApartments = view.findViewById(R.id.rvApartments);
        rvApartments.setLayoutManager(new LinearLayoutManager(getContext()));
        
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAddApartment);
        fabAdd.setOnClickListener(v -> showAddApartmentDialog());

        // Swipe-to-delete setup
        setupSwipeToDelete();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadApartments();
    }

    // Appartementen laden uit DB
    private void loadApartments() {
        apartmentList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABEL_APPARTEMENTEN, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                int idIndex = cursor.getColumnIndex(DatabaseHelper.KOLOM_APPARTEMENT_ID);
                int doorIndex = cursor.getColumnIndex(DatabaseHelper.KOLOM_DEURNUMMER);
                int floorIndex = cursor.getColumnIndex(DatabaseHelper.KOLOM_VERDIEPING);
                int balanceIndex = cursor.getColumnIndex(DatabaseHelper.KOLOM_HUIDIG_SALDO);
                int tenantIndex = cursor.getColumnIndex(DatabaseHelper.KOLOM_HUURDER_NAAM);
                int rentIndex = cursor.getColumnIndex(DatabaseHelper.KOLOM_HUUR_BEDRAG); // V3

                if (idIndex != -1) {
                    String tenantName = (tenantIndex != -1) ? cursor.getString(tenantIndex) : "";
                    double rentAmount = (rentIndex != -1) ? cursor.getDouble(rentIndex) : 0;

                    Apartment apt = new Apartment(
                            cursor.getInt(idIndex),
                            cursor.getInt(doorIndex),
                            cursor.getInt(floorIndex),
                            cursor.getDouble(balanceIndex),
                            tenantName,
                            rentAmount
                    );
                    apartmentList.add(apt);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        adapter = new ApartmentAdapter(apartmentList, apartment -> {
            Intent intent = new Intent(getContext(), ApartmentDetailActivity.class);
            intent.putExtra("apartment_id", apartment.getId());
            startActivity(intent);
        });
        rvApartments.setAdapter(adapter);
    }

    // Swipe-to-delete logica
    private void setupSwipeToDelete() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Apartment apt = apartmentList.get(position);
                showDeleteConfirmationDialog(apt, position);
            }
        }).attachToRecyclerView(rvApartments);
    }

    // Bevestigingsdialoog voor verwijderen
    private void showDeleteConfirmationDialog(Apartment apt, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Daireyi Sil")
                .setMessage("Daire " + apt.getDoorNumber() + " silinsin mi? Bu işlem geri alınamaz.")
                .setPositiveButton("Evet", (dialog, which) -> deleteApartment(apt.getId()))
                .setNegativeButton("Hayır", (dialog, which) -> adapter.notifyItemChanged(position))
                .setCancelable(false)
                .show();
    }

    // Verwijder appartement
    private void deleteApartment(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABEL_APPARTEMENTEN, DatabaseHelper.KOLOM_APPARTEMENT_ID + " = ?", new String[]{String.valueOf(id)});
        loadApartments();
        Toast.makeText(getContext(), "Daire silindi", Toast.LENGTH_SHORT).show();
    }

    // Dialoog voor toevoegen appartement
    private void showAddApartmentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_add_apartment, null);
        builder.setView(view);

        android.widget.EditText etDoor = view.findViewById(R.id.etDoorNumber);
        // Kat (Floor) input verwijderd op verzoek
        android.widget.EditText etTenantName = view.findViewById(R.id.etTenantName);
        Button btnAdd = view.findViewById(R.id.btnAdd);
        
        AlertDialog dialog = builder.create();
        
        btnAdd.setOnClickListener(v -> {
            String doorStr = etDoor.getText().toString();
            String tenantName = etTenantName.getText().toString();
            
            if (!doorStr.isEmpty()) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.KOLOM_DEURNUMMER, Integer.parseInt(doorStr));
                values.put(DatabaseHelper.KOLOM_VERDIEPING, 0); // Default waarde, niet meer gevraagd
                values.put(DatabaseHelper.KOLOM_HUIDIG_SALDO, 0);
                values.put(DatabaseHelper.KOLOM_HUURDER_NAAM, tenantName);
                values.put(DatabaseHelper.KOLOM_HUUR_BEDRAG, 0); // Standaard 0
                
                db.insert(DatabaseHelper.TABEL_APPARTEMENTEN, null, values);
                loadApartments();
                dialog.dismiss();
                Toast.makeText(getContext(), "Daire eklendi", Toast.LENGTH_SHORT).show();
            }
        });
        
        dialog.show();
    }
}
