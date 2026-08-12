package com.example.apartmanyonetim;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.apartmanyonetim.fragments.ApartmentsFragment;
import com.example.apartmanyonetim.fragments.ExpensesFragment;
import com.example.apartmanyonetim.fragments.HomeFragment;
import com.example.apartmanyonetim.fragments.ReportsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

// Hoofdactiviteit met Bottom Navigation
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            
            // Navigatie logica
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_expenses) {
                selectedFragment = new ExpensesFragment();
            } else if (itemId == R.id.nav_apartments) {
                selectedFragment = new ApartmentsFragment();
            } else if (itemId == R.id.nav_reports) {
                selectedFragment = new ReportsFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        // Standaard fragment instellen (Home)
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }
    }
}
