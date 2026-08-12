package com.example.apartmanyonetim.fragments;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.apartmanyonetim.DatabaseHelper;
import com.example.apartmanyonetim.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ReportsFragment extends Fragment {

    private BarChart chartYearly, chartMonthly;
    private Spinner spinnerMonth, spinnerYear;
    private TextView tvYearlyTitle, tvYearlyIncome, tvYearlyExpense, tvYearlyProfit;
    private TextView tvMonthlyIncome, tvMonthlyExpense, tvMonthlyProfit;
    private TextView tvPrediction;
    private TextInputEditText etInflationRate;
    private ToggleButton toggleApplyInflation;
    
    private DatabaseHelper dbHelper;
    private float appliedInflationRate = 0; // Opslaan welke rate is toegepast

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);
        
        dbHelper = new DatabaseHelper(requireContext());
        
        // UI Binding
        chartYearly = view.findViewById(R.id.chartYearly);
        chartMonthly = view.findViewById(R.id.chartMonthly);
        spinnerMonth = view.findViewById(R.id.spinnerMonth);
        spinnerYear = view.findViewById(R.id.spinnerYear);
        
        tvYearlyTitle = view.findViewById(R.id.tvYearlyTitle);
        tvYearlyIncome = view.findViewById(R.id.tvYearlyIncome);
        tvYearlyExpense = view.findViewById(R.id.tvYearlyExpense);
        tvYearlyProfit = view.findViewById(R.id.tvYearlyProfit);
        
        tvMonthlyIncome = view.findViewById(R.id.tvMonthlyIncome);
        tvMonthlyExpense = view.findViewById(R.id.tvMonthlyExpense);
        tvMonthlyProfit = view.findViewById(R.id.tvMonthlyProfit);
        
        tvPrediction = view.findViewById(R.id.tvPrediction);
        etInflationRate = view.findViewById(R.id.etInflationRate);
        toggleApplyInflation = view.findViewById(R.id.toggleApplyInflation);

        setupCharts();
        setupSpinners();
        setupRentSimulator();
        
        return view;
    }

    private void setupCharts() {
        // Init Yearly Chart
        chartYearly.getDescription().setEnabled(false);
        chartYearly.setPinchZoom(false);
        chartYearly.setDrawGridBackground(false);
        XAxis xAxis = chartYearly.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"", "Oca", "Şub", "Mar", "Nis", "May", "Haz", "Tem", "Ağu", "Eyl", "Eki", "Kas", "Ara"}));

        // Init Monthly Chart
        chartMonthly.getDescription().setEnabled(false);
        chartMonthly.setPinchZoom(false);
        chartMonthly.setDrawGridBackground(false);
        XAxis xAxisM = chartMonthly.getXAxis();
        xAxisM.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisM.setGranularity(1f); // Dagen
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
                loadReportData();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        };
        
        spinnerMonth.setOnItemSelectedListener(listener);
        spinnerYear.setOnItemSelectedListener(listener);
    }
    
    private void setupRentSimulator() {
        etInflationRate.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculatePrediction();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        
        toggleApplyInflation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Apply Inflation
                String rateStr = etInflationRate.getText().toString();
                if (!rateStr.isEmpty()) {
                    float rate = Float.parseFloat(rateStr);
                    applyInflation(rate);
                    etInflationRate.setEnabled(false);
                }
            } else {
                // Revert
                revertInflation();
                etInflationRate.setEnabled(true);
            }
            // Update UI
            calculatePrediction();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadReportData();
        calculatePrediction();
    }

    private void loadReportData() {
        String yearStr = spinnerYear.getSelectedItem().toString();
        int monthIndex = spinnerMonth.getSelectedItemPosition() + 1;
        
        tvYearlyTitle.setText("Finansal Özet " + yearStr);
        loadYearlyData(yearStr);
        loadMonthlyData(monthIndex, yearStr);
    }

    // Yıllık Veri ve Grafik
    private void loadYearlyData(String year) {
        float[] monthlyIncome = new float[13]; // 1-12
        float[] monthlyExpense = new float[13];
        double totalInc = 0, totalExp = 0;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABEL_TRANSACTIES, null, 
                DatabaseHelper.KOLOM_DATUM + " LIKE ?", new String[]{"%-" + year + "%"}, null, null, null);

        if (cursor.moveToFirst()) {
            int dateIndex = cursor.getColumnIndex(DatabaseHelper.KOLOM_DATUM);
            int typeIndex = cursor.getColumnIndex(DatabaseHelper.KOLOM_TYPE);
            int amountIndex = cursor.getColumnIndex(DatabaseHelper.KOLOM_BEDRAG);

            do {
                String date = cursor.getString(dateIndex); // dd-MM-yyyy HH:mm:ss of dd-MM-yyyy
                double amount = cursor.getDouble(amountIndex);
                String type = cursor.getString(typeIndex);
                
                // Maand extraheren
                try {
                    // Check formaat "-MM-yyyy"
                    int mStart = date.indexOf("-") + 1;
                    int mEnd = date.indexOf("-", mStart);
                    if (mEnd > mStart) {
                        int month = Integer.parseInt(date.substring(mStart, mEnd));
                        if (month >= 1 && month <= 12) {
                            if ("INKOMSTEN".equals(type)) {
                                monthlyIncome[month] += amount;
                                totalInc += amount;
                            } else {
                                monthlyExpense[month] += amount;
                                totalExp += amount;
                            }
                        }
                    }
                } catch (Exception e) {}
            } while (cursor.moveToNext());
        }
        cursor.close();

        // Update Text
        tvYearlyIncome.setText(String.format("Gelir:\n%.2f TL", totalInc));
        tvYearlyExpense.setText(String.format("Gider:\n%.2f TL", totalExp));
        tvYearlyProfit.setText(String.format("Net Kar:\n%.2f TL", totalInc - totalExp));

        // Update Chart
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            // Gestapelde bar? Of gegroepeerd? Simpele 'Profit' bar of Income vs Expense?
            // De gebruiker wil 'Gelir - Gider - Kar'.
            // Laten we 2 datasets doen: Income (Groen) en Expense (Rood) per maand
            // MPAndroidChart ondersteunt Grouped Bars, maar dat is complexer te positioneren met x-index
            // We doen Stacked Bars: Income (pos) en Expense (neg)? Of gewoon naast elkaar?
            // Voor eenvoud: We tonen de Net Profit (Income - Expense) per maand in de chart?
            // Nee, gevraagd = "Gelir Gider Grafiği".
            // We doen Stacked Bar: [Expense | Profit] = Total Income? Nee, Expense en Profit zijn delen van Income (idealiter).
            // Laten we gewoon Netto Resultaat tonen: Groen als positief, Rood als negatief.
            // Dit is het duidelijkst voor "Kar" (Profit). "Gelir - Gider"
            
            // CORRECTIE op plan: Gebruiker wil 'Gelir - Gider - Net Kar' zien in tekst.
            // Grafiek: 'Aylik ve Yillik Grafik'.
            // Ik doe een Stacked Bar: Onderste deel Expense (Rood), Bovenste deel Net Profit (Groen)?
            // Als Expense > Income, dan Rood.
            
            // Beter: 2 waarden per X: Income en Expense. (Grouped BarChart is standard).
            // Ik zal eenvoudige Grouped Bar implementeren door 2 entries per X maar dat vereist float x manipulatie.
            // Simpelste: BarChart met "Net Kar" (Profit) per maand. Is beste indicator.
            // Maar gebruiker vroeg specifiek "Gelir Gider Grafigi".
            // Ok, we doen Stacked Bar met 2 waarden: Income en Expense.
            
            entries.add(new BarEntry(i, new float[]{monthlyIncome[i], -monthlyExpense[i]})); // Income pos, Expense neg
        }

        BarDataSet set = new BarDataSet(entries, "Gelir (Pozitif) / Gider (Negatif)");
        set.setColors(Color.parseColor("#4CAF50"), Color.parseColor("#F44336"));
        set.setStackLabels(new String[]{"Gelir", "Gider"});
        
        BarData data = new BarData(set);
        data.setBarWidth(0.8f);
        chartYearly.setData(data);
        chartYearly.invalidate();
    }

    // Maandelijkse Veri (Inkomen vs Uitgaven vergelijking)
    private void loadMonthlyData(int month, String year) {
        double totalInc = 0, totalExp = 0;
        
        String filter = String.format(Locale.getDefault(), "-%02d-%s", month, year);

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABEL_TRANSACTIES, null, 
                DatabaseHelper.KOLOM_DATUM + " LIKE ?", new String[]{"%" + filter + "%"}, null, null, null);

        if (cursor.moveToFirst()) {
            int typeIndex = cursor.getColumnIndex(DatabaseHelper.KOLOM_TYPE);
            int amountIndex = cursor.getColumnIndex(DatabaseHelper.KOLOM_BEDRAG);

            do {
                String type = cursor.getString(typeIndex);
                double amount = cursor.getDouble(amountIndex);
                
                if ("INKOMSTEN".equals(type)) {
                    totalInc += amount;
                } else {
                    totalExp += amount;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        tvMonthlyIncome.setText(String.format("Gelir:\n%.2f TL", totalInc));
        tvMonthlyExpense.setText(String.format("Gider:\n%.2f TL", totalExp));
        tvMonthlyProfit.setText(String.format("Net Kar:\n%.2f TL", totalInc - totalExp));
        
        // Chart: Simple Income vs Expense
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, (float) totalInc));
        entries.add(new BarEntry(1, (float) totalExp));
        
        BarDataSet set = new BarDataSet(entries, "Aylık Durum");
        set.setColors(Color.parseColor("#4CAF50"), Color.parseColor("#F44336"));
        
        BarData data = new BarData(set);
        data.setBarWidth(0.5f);
        chartMonthly.setData(data);
        
        // X-As labels updaten
        chartMonthly.getXAxis().setValueFormatter(new IndexAxisValueFormatter(new String[]{"Gelir", "Gider"}));
        chartMonthly.getXAxis().setGranularity(1f);
        chartMonthly.getXAxis().setLabelCount(2);
        
        chartMonthly.invalidate();
    }

    private void calculatePrediction() {
        String rateStr = etInflationRate.getText().toString();
        float rate = 0;
        if (!rateStr.isEmpty()) {
            try { rate = Float.parseFloat(rateStr); } catch (Exception e) {}
        }
        
        // Huidige totale inkomsten (o.b.v. huur) ophalen
        double currentTotalRent = 0;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.TABEL_APPARTEMENTEN, new String[]{DatabaseHelper.KOLOM_HUUR_BEDRAG}, null, null, null, null, null);
        if (c.moveToFirst()) {
            do {
                currentTotalRent += c.getDouble(0);
            } while (c.moveToNext());
        }
        c.close();
        
        double predictedNextYearMonthly = currentTotalRent;
        // Als toggle UIT staat, tonen we wat het ZOU zijn. Als AAN staat, is currentTotalRent al verhoogd.
        if (!toggleApplyInflation.isChecked()) {
             predictedNextYearMonthly = currentTotalRent * (1 + rate / 100);
        }
        
        tvPrediction.setText(String.format(Locale.getDefault(), 
            "Gelecek yıl aylık tahmini gelir: %.2f TL (%%%.1f Artış)", 
            predictedNextYearMonthly, rate));
    }

    private void applyInflation(float rate) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        appliedInflationRate = rate;
        
        // We lezen en updaten in transactie? Nee, één voor één
        Cursor c = db.query(DatabaseHelper.TABEL_APPARTEMENTEN, 
                new String[]{DatabaseHelper.KOLOM_APPARTEMENT_ID, DatabaseHelper.KOLOM_HUUR_BEDRAG}, 
                null, null, null, null, null);
                
        if (c.moveToFirst()) {
            do {
                int id = c.getInt(0);
                double currentRent = c.getDouble(1);
                double newRent = currentRent * (1 + rate / 100);
                
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.KOLOM_HUUR_BEDRAG, Math.round(newRent)); // Afronden op heel getal? Nee, double.
                
                db.update(DatabaseHelper.TABEL_APPARTEMENTEN, values, DatabaseHelper.KOLOM_APPARTEMENT_ID + " = ?", new String[]{String.valueOf(id)});
            } while (c.moveToNext());
        }
        c.close();
        Toast.makeText(getContext(), "%" + rate + " zam tüm dairelere uygulandı.", Toast.LENGTH_SHORT).show();
    }
    
    private void revertInflation() {
        if (appliedInflationRate == 0) return;
        
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        Cursor c = db.query(DatabaseHelper.TABEL_APPARTEMENTEN, 
                new String[]{DatabaseHelper.KOLOM_APPARTEMENT_ID, DatabaseHelper.KOLOM_HUUR_BEDRAG}, 
                null, null, null, null, null);
                
        if (c.moveToFirst()) {
            do {
                int id = c.getInt(0);
                double currentRent = c.getDouble(1);
                double originalRent = currentRent / (1 + appliedInflationRate / 100);
                
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.KOLOM_HUUR_BEDRAG, Math.round(originalRent)); // Terug naar origineel (ongeveer)
                
                db.update(DatabaseHelper.TABEL_APPARTEMENTEN, values, DatabaseHelper.KOLOM_APPARTEMENT_ID + " = ?", new String[]{String.valueOf(id)});
            } while (c.moveToNext());
        }
        c.close();
        Toast.makeText(getContext(), "Zam geri alındı.", Toast.LENGTH_SHORT).show();
        appliedInflationRate = 0;
    }
}
