package com.example.memorix.ui;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

    private BroadcastReceiver logoutReceiver;

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
        logoutReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("com.example.ACTION_LOGOUT".equals(intent.getAction())) {
                    finish();
                }
            }
        };

        // Đăng ký BroadcastReceiver đúng cách với SDK >= 33
        IntentFilter filter = new IntentFilter("com.example.ACTION_LOGOUT");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                    logoutReceiver,
                    new IntentFilter("com.example.ACTION_LOGOUT"),
                    Context.RECEIVER_NOT_EXPORTED
            );
        } else {
            registerReceiver(
                    logoutReceiver,
                    new IntentFilter("com.example.ACTION_LOGOUT")
            );
        }

    }

    private void setupNavigation() {
        findViewById(R.id.nav_host_fragment).post(() -> {
                NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
                BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
                NavigationUI.setupWithNavController(bottomNav, navController);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (logoutReceiver != null) {
            unregisterReceiver(logoutReceiver);
        }
    }
}