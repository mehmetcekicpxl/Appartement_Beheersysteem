package com.example.apartmanyonetim.fragments;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apartmanyonetim.DatabaseHelper;
import com.example.apartmanyonetim.R;
import com.example.apartmanyonetim.adapters.ExpenseAdapter;
import com.example.apartmanyonetim.models.Transaction;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// Fragment voor uitgavenbeheer (Giderler)
public class ExpensesFragment extends Fragment {

    private RecyclerView rvExpenses;
    private DatabaseHelper dbHelper;
    private List<Transaction> expenseList;
    private ExpenseAdapter adapter;
    private Spinner spinnerMonth, spinnerYear;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expenses, container, false);
        
        dbHelper = new DatabaseHelper(requireContext());
        rvExpenses = view.findViewById(R.id.rvExpenses);
        rvExpenses.setLayoutManager(new LinearLayoutManager(getContext()));
        
        spinnerMonth = view.findViewById(R.id.spinnerMonth);
        spinnerYear = view.findViewById(R.id.spinnerYear);
        
        setupSpinners();
        
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAddExpense);
        fabAdd.setOnClickListener(v -> showAddExpenseDialog());

        return view;
    }

    private void setupSpinners() {
        // Maanden
        String[] months = {"Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran", 
                          "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"};
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, months);
        spinnerMonth.setAdapter(monthAdapter);
        
        // Jaren
        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR);
        List<String> years = new ArrayList<>();
        for (int i = currentYear - 2; i <= currentYear + 2; i++) {
            years.add(String.valueOf(i));
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, years);
        spinnerYear.setAdapter(yearAdapter);
        
        // Defaults
        spinnerMonth.setSelection(cal.get(Calendar.MONTH));
        spinnerYear.setSelection(2); // Index van currentYear

        // Listeners
        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadExpenses();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        };
        
        spinnerMonth.setOnItemSelectedListener(listener);
        spinnerYear.setOnItemSelectedListener(listener);
    }

    @Override
    public void onResume() {
        super.onResume();
        checkAndGenerateRecurringExpenses();
        loadExpenses();
    }
    
    // Check en genereer periodieke uitgaven
    private void checkAndGenerateRecurringExpenses() {
        Calendar cal = Calendar.getInstance();
        int currentMonth = cal.get(Calendar.MONTH) + 1; // 1-12
        int currentYear = cal.get(Calendar.YEAR);
        
        // Vorige maand berekenen
        cal.add(Calendar.MONTH, -1);
        int prevMonth = cal.get(Calendar.MONTH) + 1;
        int prevYear = cal.get(Calendar.YEAR);
        
        String prevFilter = String.format(Locale.getDefault(), "-%02d-%s", prevMonth, prevYear);
        String currentFilter = String.format(Locale.getDefault(), "-%02d-%s", currentMonth, currentYear);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        // Haal recurring expenses van vorige maand
        // Let op v5 kolom
        try {
            Cursor cursor = db.query(DatabaseHelper.TABEL_TRANSACTIES, null, 
                    DatabaseHelper.KOLOM_DATUM + " LIKE ? AND " + DatabaseHelper.KOLOM_IS_RECURRING + " = 1", 
                    new String[]{"%" + prevFilter + "%"}, null, null, null);

            if (cursor.moveToFirst()) {
                int descIndex = cursor.getColumnIndex(DatabaseHelper.KOLOM_OMSCHRIJVING);
                int amountIndex = cursor.getColumnIndex(DatabaseHelper.KOLOM_BEDRAG);
                int catIndex = cursor.getColumnIndex(DatabaseHelper.KOLOM_CATEGORIE);
                int typeIndex = cursor.getColumnIndex(DatabaseHelper.KOLOM_TYPE);
                int dateIndex = cursor.getColumnIndex(DatabaseHelper.KOLOM_DATUM);

                do {
                    String desc = cursor.getString(descIndex);
                    double amount = cursor.getDouble(amountIndex);
                    String cat = cursor.getString(catIndex);
                    String type = cursor.getString(typeIndex);
                    String prevDate = cursor.getString(dateIndex);
                    
                    // Check of deze al bestaat in huidige maand
                    Cursor check = db.query(DatabaseHelper.TABEL_TRANSACTIES, null,
                            DatabaseHelper.KOLOM_DATUM + " LIKE ? AND " + DatabaseHelper.KOLOM_OMSCHRIJVING + " = ? AND " + DatabaseHelper.KOLOM_BEDRAG + " = ?",
                            new String[]{"%" + currentFilter + "%", desc, String.valueOf(amount)}, null, null, null);
                    
                    if (check.getCount() == 0) {
                        // Bestaat niet, kopieer
                        ContentValues values = new ContentValues();
                        values.put(DatabaseHelper.KOLOM_TYPE, type);
                        values.put(DatabaseHelper.KOLOM_CATEGORIE, cat);
                        values.put(DatabaseHelper.KOLOM_BEDRAG, amount);
                        
                        // Datum aanpassen naar huidige maand
                        // We proberen dag te behouden
                        try {
                            String day = prevDate.substring(0, prevDate.indexOf("-"));
                            values.put(DatabaseHelper.KOLOM_DATUM, day + currentFilter + " " + prevDate.substring(prevDate.indexOf(" ") + 1));
                        } catch (Exception e) {
                            values.put(DatabaseHelper.KOLOM_DATUM, "01" + currentFilter + " 09:00:00");
                        }
                        
                        values.put(DatabaseHelper.KOLOM_IS_BETAALD, 1);
                        values.put(DatabaseHelper.KOLOM_OMSCHRIJVING, desc);
                        values.put(DatabaseHelper.KOLOM_IS_RECURRING, 1); // Blijft recurring
                        
                        db.insert(DatabaseHelper.TABEL_TRANSACTIES, null, values);
                    }
                    check.close();
                    
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            // Kolom bestaat misschien nog niet als upgrade faalt, negeer
        }
    }

    // Laad uitgaven uit database (Gefilterd op maand)
    private void loadExpenses() {
        expenseList = new ArrayList<>();
        
        int monthIndex = spinnerMonth.getSelectedItemPosition() + 1;
        String yearStr = spinnerYear.getSelectedItem().toString();
        String filter = String.format(Locale.getDefault(), "-%02d-%s", monthIndex, yearStr);
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        // Filter op datum en type=UITGAVEN
        Cursor cursor = db.query(DatabaseHelper.TABEL_TRANSACTIES, null, 
                DatabaseHelper.KOLOM_TYPE + " = ? AND " + DatabaseHelper.KOLOM_DATUM + " LIKE ?", 
                new String[]{"UITGAVEN", "%" + filter + "%"}, 
                null, null, DatabaseHelper.KOLOM_DATUM + " DESC");

        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(DatabaseHelper.KOLOM_TRANSACTIE_ID);
            int descIndex = cursor.getColumnIndex(DatabaseHelper.KOLOM_OMSCHRIJVING);
            int amountIndex = cursor.getColumnIndex(DatabaseHelper.KOLOM_BEDRAG);
            int dateIndex = cursor.getColumnIndex(DatabaseHelper.KOLOM_DATUM);
            int catIndex = cursor.getColumnIndex(DatabaseHelper.KOLOM_CATEGORIE);
            int typeIndex = cursor.getColumnIndex(DatabaseHelper.KOLOM_TYPE);
            int paidIndex = cursor.getColumnIndex(DatabaseHelper.KOLOM_IS_BETAALD);
            int appIdIndex = cursor.getColumnIndex(DatabaseHelper.KOLOM_TRANSACTIE_APP_ID);

            do {
                if (idIndex != -1) {
                    int id = cursor.getInt(idIndex);
                    String desc = cursor.getString(descIndex);
                    double amount = cursor.getDouble(amountIndex);
                    String date = cursor.getString(dateIndex);
                    String cat = cursor.getString(catIndex);
                    
                    Transaction t = new Transaction(id, 0, "UITGAVEN", cat, amount, date, true, desc);
                    expenseList.add(t);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        adapter = new ExpenseAdapter(expenseList, this::showEditDeleteOptions);
        rvExpenses.setAdapter(adapter);
    }

    // ... (showEditDeleteOptions, confirmDeleteExpense, deleteExpense blijven hetzelfde)
    // Opties tonen: Bewerken of Verwijderen
    private void showEditDeleteOptions(Transaction expense) {
        String[] options = {"Düzenle", "Sil"};
        new AlertDialog.Builder(requireContext())
                .setTitle("İşlem Seçin")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) { // Düzenle
                        showEditExpenseDialog(expense);
                    } else { // Sil
                        confirmDeleteExpense(expense);
                    }
                })
                .show();
    }

    // Bevestig verwijderen
    private void confirmDeleteExpense(Transaction expense) {
         new AlertDialog.Builder(requireContext())
                .setTitle("Gideri Sil")
                .setMessage("Bu gider kaydı silinsin mi?")
                .setPositiveButton("Evet", (dialog, which) -> {
                    deleteExpense(expense.getId());
                })
                .setNegativeButton("Hayır", null)
                .show();
    }

    // Verwijder uit database
    private void deleteExpense(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABEL_TRANSACTIES, DatabaseHelper.KOLOM_TRANSACTIE_ID + " = ?", new String[]{String.valueOf(id)});
        loadExpenses();
        Toast.makeText(getContext(), "Gider silindi", Toast.LENGTH_SHORT).show();
    }

    // Dialoog voor bewerken uitgave
    private void showEditExpenseDialog(Transaction expense) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Gider Düzenle");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(requireContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText etDesc = new EditText(requireContext());
        etDesc.setText(expense.getDescription());
        layout.addView(etDesc);

        final EditText etAmount = new EditText(requireContext());
        etAmount.setText(String.valueOf(expense.getAmount()));
        etAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etAmount);

        builder.setView(layout);

        builder.setPositiveButton("Güncelle", (dialog, which) -> {
            String desc = etDesc.getText().toString();
            String amountStr = etAmount.getText().toString();

            if (!desc.isEmpty() && !amountStr.isEmpty()) {
                updateExpense(expense.getId(), desc, Double.parseDouble(amountStr));
            }
        });

        builder.setNegativeButton("İptal", null);
        builder.show();
    }
    
    // Update in database
    private void updateExpense(int id, String desc, double amount) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KOLOM_OMSCHRIJVING, desc);
        values.put(DatabaseHelper.KOLOM_BEDRAG, amount);
        
        db.update(DatabaseHelper.TABEL_TRANSACTIES, values, DatabaseHelper.KOLOM_TRANSACTIE_ID + " = ?", new String[]{String.valueOf(id)});
        loadExpenses();
        Toast.makeText(getContext(), "Gider güncellendi", Toast.LENGTH_SHORT).show();
    }

    // Dialoog voor toevoegen uitgave
    private void showAddExpenseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Gider Ekle");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(requireContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText etDesc = new EditText(requireContext());
        etDesc.setHint("Açıklama (Örn: Asansör)");
        layout.addView(etDesc);

        final EditText etAmount = new EditText(requireContext());
        etAmount.setHint("Tutar (TL)");
        etAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etAmount);

        final Spinner spinnerType = new Spinner(requireContext());
        String[] types = {"Sabit Gider", "Ekstra Gider", "Demirbaş", "Diğer"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, types);
        spinnerType.setAdapter(adapter);
        layout.addView(spinnerType);
        
        // Checkbox voor recurring
        final CheckBox cbRecurring = new CheckBox(requireContext());
        cbRecurring.setText("Bu gider her ay tekrarlansın (Sabit)");
        layout.addView(cbRecurring);
        
        // Auto-check/disable als Sabit Gider gekozen wordt
        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                 if (position == 0) { // Sabit Gider
                     cbRecurring.setChecked(true);
                     cbRecurring.setEnabled(true);
                 } else {
                     cbRecurring.setChecked(false);
                     cbRecurring.setEnabled(false);
                 }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        builder.setView(layout);

        builder.setPositiveButton("Kaydet", (dialog, which) -> {
            String desc = etDesc.getText().toString();
            String amountStr = etAmount.getText().toString();
            String category = spinnerType.getSelectedItem().toString();
            boolean isRecurring = cbRecurring.isChecked();

            if (!desc.isEmpty() && !amountStr.isEmpty()) {
                saveExpense(desc, Double.parseDouble(amountStr), category, isRecurring);
            }
        });

        builder.setNegativeButton("İptal", null);
        builder.show();
    }

    private void saveExpense(String desc, double amount, String category, boolean isRecurring) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        String currentDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());

        values.put(DatabaseHelper.KOLOM_TYPE, "UITGAVEN");
        values.put(DatabaseHelper.KOLOM_CATEGORIE, category);
        values.put(DatabaseHelper.KOLOM_BEDRAG, amount);
        values.put(DatabaseHelper.KOLOM_DATUM, currentDate);
        values.put(DatabaseHelper.KOLOM_IS_BETAALD, 1);
        values.put(DatabaseHelper.KOLOM_OMSCHRIJVING, desc);
        values.put(DatabaseHelper.KOLOM_IS_RECURRING, isRecurring ? 1 : 0); // Opslaan
        
        db.insert(DatabaseHelper.TABEL_TRANSACTIES, null, values);
        
        Toast.makeText(getContext(), "Gider kaydedildi", Toast.LENGTH_SHORT).show();
        loadExpenses();
    }
}
