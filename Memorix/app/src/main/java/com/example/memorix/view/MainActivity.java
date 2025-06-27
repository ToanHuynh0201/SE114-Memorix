package com.example.memorix.view;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.memorix.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class  MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupNavigation();
    }

    private void setupNavigation() {
        findViewById(R.id.nav_host_fragment).post(() -> {
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

            // Cấu hình để BottomNavigation xử lý navigation đúng cách
            NavigationUI.setupWithNavController(bottomNav, navController);

            // Thêm listener để xử lý việc reselect
            bottomNav.setOnItemReselectedListener(item -> {
                // Khi reselect một item, pop back stack về destination đó
                navController.popBackStack(item.getItemId(), false);
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}