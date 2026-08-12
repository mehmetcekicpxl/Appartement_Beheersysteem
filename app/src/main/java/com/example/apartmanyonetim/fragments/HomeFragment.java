package com.example.apartmanyonetim.fragments;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apartmanyonetim.DatabaseHelper;
import com.example.apartmanyonetim.R;
import com.example.apartmanyonetim.adapters.PaymentAdapter;
import com.example.apartmanyonetim.models.Apartment;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

// Fragment voor het startscherm (Ödemeler)
public class HomeFragment extends Fragment {

    private RecyclerView rvPaymentStatus;
    private TextView tvMonthlyCollection;
    private TextView tvPaidCount;
    private TextView tvUnpaidCount;
    private DatabaseHelper dbHelper;
    private List<Apartment> apartmentList;
    private Set<Integer> paidApartmentIds;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        dbHelper = new DatabaseHelper(requireContext());
        rvPaymentStatus = view.findViewById(R.id.rvPaymentStatus);
        tvMonthlyCollection = view.findViewById(R.id.tvMonthlyCollection);
        tvPaidCount = view.findViewById(R.id.tvPaidCount);
        tvUnpaidCount = view.findViewById(R.id.tvUnpaidCount);
        
        rvPaymentStatus.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Initialiseer data indien nodig (voor eerste start)
        initialiseerDataIndienLeeg();
        
        // Test data voor januari toevoegen (zoals gevraagd)
        addJanuaryDummyData();

        // Datum header instellen
        TextView tvDateHeader = view.findViewById(R.id.tvDateHeader);
        Calendar cal = Calendar.getInstance();
        String currentMonthYear = new SimpleDateFormat("MMMM yyyy", new Locale("tr", "TR")).format(cal.getTime());
        // Eerste letter hoofdletter maken
        currentMonthYear = currentMonthYear.substring(0, 1).toUpperCase(new Locale("tr", "TR")) + currentMonthYear.substring(1);
        tvDateHeader.setText(currentMonthYear);
        
        return view;
    }

    // Dummy data voor Januari toevoegen (voor testdoeleinden)
    private void addJanuaryDummyData() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        // Check of er al data is voor jan 2026
        Cursor cursor = db.query(DatabaseHelper.TABEL_TRANSACTIES, null, 
                DatabaseHelper.KOLOM_DATUM + " LIKE ?", new String[]{"%01-2026%"}, null, null, null);
        
        boolean hasJanData = cursor.getCount() > 0;
        cursor.close();
        
        if (!hasJanData) {
            // Voeg inkomsten toe (Aidat) voor 5 appartementen
            for (int i = 1; i <= 5; i++) {
                 ContentValues values = new ContentValues();
                 values.put(DatabaseHelper.KOLOM_TRANSACTIE_APP_ID, i); // Koppel aan app ID i
                 values.put(DatabaseHelper.KOLOM_TYPE, "INKOMSTEN");
                 values.put(DatabaseHelper.KOLOM_CATEGORIE, "Aidat");
                 values.put(DatabaseHelper.KOLOM_BEDRAG, 1500.0);
                 values.put(DatabaseHelper.KOLOM_DATUM, "15-01-2026 10:00:00");
                 values.put(DatabaseHelper.KOLOM_IS_BETAALD, 1);
                 values.put(DatabaseHelper.KOLOM_OMSCHRIJVING, "Aidat Ödemesi - Ocak 2026");
                 db.insert(DatabaseHelper.TABEL_TRANSACTIES, null, values);
            }

            // Voeg een uitgave toe (Elektrik Faturası)
            ContentValues expValues = new ContentValues();
            expValues.put(DatabaseHelper.KOLOM_TRANSACTIE_APP_ID, -1); // Algemeen
            expValues.put(DatabaseHelper.KOLOM_TYPE, "UITGAVEN");
            expValues.put(DatabaseHelper.KOLOM_CATEGORIE, "Fatura");
            expValues.put(DatabaseHelper.KOLOM_BEDRAG, 3500.0);
            expValues.put(DatabaseHelper.KOLOM_DATUM, "20-01-2026 14:30:00");
            expValues.put(DatabaseHelper.KOLOM_IS_BETAALD, 1); // Betaald
            expValues.put(DatabaseHelper.KOLOM_OMSCHRIJVING, "Ocak Elektrik Faturası");
            db.insert(DatabaseHelper.TABEL_TRANSACTIES, null, expValues);
            
            // Voeg nog een uitgave toe (Temizlik)
             ContentValues expValues2 = new ContentValues();
            expValues2.put(DatabaseHelper.KOLOM_TRANSACTIE_APP_ID, -1);
            expValues2.put(DatabaseHelper.KOLOM_TYPE, "UITGAVEN");
            expValues2.put(DatabaseHelper.KOLOM_CATEGORIE, "Temizlik");
            expValues2.put(DatabaseHelper.KOLOM_BEDRAG, 2000.0);
            expValues2.put(DatabaseHelper.KOLOM_DATUM, "25-01-2026 09:00:00");
            expValues2.put(DatabaseHelper.KOLOM_IS_BETAALD, 1);
            expValues2.put(DatabaseHelper.KOLOM_OMSCHRIJVING, "Ocak Temizlik Gideri");
            db.insert(DatabaseHelper.TABEL_TRANSACTIES, null, expValues2);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPaymentStatus();
    }

    // ... (rest van de methoden blijven hetzelfde)
    
    // Laad appartementen en hun betaalstatus voor deze maand
    private void loadPaymentStatus() {
        apartmentList = new ArrayList<>();
        paidApartmentIds = new HashSet<>();
        double totalCollected = 0;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        // 1. Alle appartementen ophalen
        Cursor cursor = db.query(DatabaseHelper.TABEL_APPARTEMENTEN, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                int idIndex = cursor.getColumnIndex(DatabaseHelper.KOLOM_APPARTEMENT_ID);
                int doorIndex = cursor.getColumnIndex(DatabaseHelper.KOLOM_DEURNUMMER);
                int floorIndex = cursor.getColumnIndex(DatabaseHelper.KOLOM_VERDIEPING);
                int balanceIndex = cursor.getColumnIndex(DatabaseHelper.KOLOM_HUIDIG_SALDO);
                int tenantIndex = cursor.getColumnIndex(DatabaseHelper.KOLOM_HUURDER_NAAM);
                int rentIndex = cursor.getColumnIndex(DatabaseHelper.KOLOM_HUUR_BEDRAG);

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
        
        // Rest van loadPaymentStatus... (wordt niet vervangen door deze range)


        // 2. Betaalstatus controleren (Transacties van type INKOMSTEN deze maand)
        Calendar cal = Calendar.getInstance();
        String currentMonthYear = new SimpleDateFormat("MM-yyyy", Locale.getDefault()).format(cal.getTime());
        
        // Query voor alle inkomsten van deze maand
        // Omdat datums als TEXT zijn opgeslagen (dd-MM-yyyy of timestamp), moeten we filteren
        // Hier nemen we aan dat we transacties ophalen en datum parsen/checken
        // Voor efficiëntie: SELECT * FROM transacties WHERE type = 'INKOMSTEN'
        
        Cursor tCursor = db.query(DatabaseHelper.TABEL_TRANSACTIES, null, 
                DatabaseHelper.KOLOM_TYPE + " = ?", new String[]{"INKOMSTEN"}, null, null, null);

        if (tCursor.moveToFirst()) {
            int appIdIndex = tCursor.getColumnIndex(DatabaseHelper.KOLOM_TRANSACTIE_APP_ID);
            int dateIndex = tCursor.getColumnIndex(DatabaseHelper.KOLOM_DATUM);
            int amountIndex = tCursor.getColumnIndex(DatabaseHelper.KOLOM_BEDRAG);
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()); // Formaat uit DatabaseHelper/insert

            do {
                String dateStr = tCursor.getString(dateIndex);
                // Check of datum in huidige maand is
                if (isDateInCurrentMonth(dateStr)) {
                    int appId = tCursor.getInt(appIdIndex);
                    paidApartmentIds.add(appId);
                    totalCollected += tCursor.getDouble(amountIndex);
                }
            } while (tCursor.moveToNext());
        }
        tCursor.close();

        // Update UI
        tvMonthlyCollection.setText(String.format("₺%.0f", totalCollected)); // Integer format for cleaner look

        int totalCount = apartmentList.size();
        int paidCount = paidApartmentIds.size();
        int unpaidCount = totalCount - paidCount;

        if (tvPaidCount != null) tvPaidCount.setText(paidCount + " Ödendi");
        if (tvUnpaidCount != null) tvUnpaidCount.setText(unpaidCount + " Ödenmedi");
        
        PaymentAdapter adapter = new PaymentAdapter(apartmentList, paidApartmentIds, (apartment, isPaid) -> {
            togglePayment(apartment, isPaid);
        });
        rvPaymentStatus.setAdapter(adapter);
    }

    // Helper om te checken of datum in huidige maand is
    private boolean isDateInCurrentMonth(String dateStr) {
        try {
            // Probeer standaard formaat of timestamp
            // Eenvoudige check: bevat huidige maand string?
            Calendar cal = Calendar.getInstance();
            String currentMonth = new SimpleDateFormat("-MM-yyyy", Locale.getDefault()).format(cal.getTime());
            return dateStr.contains(currentMonth); // Simpele check voor dd-MM-yyyy
        } catch (Exception e) {
            return false;
        }
    }

    // Betaling status wijzigen
    private void togglePayment(Apartment apartment, boolean isPaid) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Calendar cal = Calendar.getInstance();
        String currentMonthName = new SimpleDateFormat("MMMM yyyy", new Locale("tr", "TR")).format(cal.getTime());
        String currentDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(cal.getTime());

        if (isPaid) {
            // Transactie toevoegen
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.KOLOM_TRANSACTIE_APP_ID, apartment.getId());
            values.put(DatabaseHelper.KOLOM_TYPE, "INKOMSTEN");
            values.put(DatabaseHelper.KOLOM_CATEGORIE, "Aidat");
            values.put(DatabaseHelper.KOLOM_BEDRAG, apartment.getRentAmount() > 0 ? apartment.getRentAmount() : 0); // Gebruik rent_amount
            values.put(DatabaseHelper.KOLOM_DATUM, currentDate);
            values.put(DatabaseHelper.KOLOM_IS_BETAALD, 1);
            values.put(DatabaseHelper.KOLOM_OMSCHRIJVING, "Aidat Ödemesi - " + currentMonthName);
            
            db.insert(DatabaseHelper.TABEL_TRANSACTIES, null, values);
            
            // Saldo updaten
            updateApartmentBalance(apartment.getId(), apartment.getRentAmount());
            
        } else {
            // Transactie verwijderen (laatste aidat van deze maand)
            // Dit is complexer, we zoeken de laatste INKOMSTEN van deze maand voor dit appartement
            // Voor nu: verwijder alle INKOMSTEN van deze maand voor dit app ID (gevaarlijk als er meerdere zijn, maar 'toggle' suggereert aan/uit)
            
            // We doen ons best om de specifieke transactie te vinden
            String delWhere = DatabaseHelper.KOLOM_TRANSACTIE_APP_ID + " = ? AND " + 
                              DatabaseHelper.KOLOM_TYPE + " = ? AND " +
                              DatabaseHelper.KOLOM_OMSCHRIJVING + " LIKE ?";
            String[] delArgs = new String[]{String.valueOf(apartment.getId()), "INKOMSTEN", "%Aidat Ödemesi - " + currentMonthName + "%"};
            
            int deleted = db.delete(DatabaseHelper.TABEL_TRANSACTIES, delWhere, delArgs);
            
            if (deleted > 0) {
                 // Saldo terugdraaien
                 updateApartmentBalance(apartment.getId(), -apartment.getRentAmount());
            }
        }
        
        // UI verversen (totaal bedrag)
        loadPaymentStatus();
    }
    
    // Saldo van appartement bijwerken
    private void updateApartmentBalance(int apartmentId, double amountChange) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Eerst huidig saldo ophalen
        Cursor c = db.query(DatabaseHelper.TABEL_APPARTEMENTEN, new String[]{DatabaseHelper.KOLOM_HUIDIG_SALDO}, 
                DatabaseHelper.KOLOM_APPARTEMENT_ID + " = ?", new String[]{String.valueOf(apartmentId)}, null, null, null);
        
        if (c.moveToFirst()) {
            double currentBalance = c.getDouble(0);
            double newBalance = currentBalance + amountChange;
            
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.KOLOM_HUIDIG_SALDO, newBalance);
            db.update(DatabaseHelper.TABEL_APPARTEMENTEN, values, DatabaseHelper.KOLOM_APPARTEMENT_ID + " = ?", new String[]{String.valueOf(apartmentId)});
        }
        c.close();
    }

    // Dummy data initialiseren als DB leeg is
    private void initialiseerDataIndienLeeg() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT count(*) FROM " + DatabaseHelper.TABEL_APPARTEMENTEN, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();

        if (count == 0) {
            db = dbHelper.getWritableDatabase();
            for (int i = 1; i <= 10; i++) {
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.KOLOM_DEURNUMMER, i);
                values.put(DatabaseHelper.KOLOM_VERDIEPING, (i - 1) / 2 + 1);
                values.put(DatabaseHelper.KOLOM_HUIDIG_SALDO, 0);
                values.put(DatabaseHelper.KOLOM_HUURDER_NAAM, "");
                values.put(DatabaseHelper.KOLOM_HUUR_BEDRAG, 1500.0); // Standaard aidat
                db.insert(DatabaseHelper.TABEL_APPARTEMENTEN, null, values);
            }
        }
    }
}
