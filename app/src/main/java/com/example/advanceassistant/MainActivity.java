package com.example.advanceassistant;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.advanceassistant.ForegroundAppService;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        if (!UsageAccessHelper.hasUsageAccess(this)) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        }

        ForegroundAppMonitor foregroundAppMonitor = new ForegroundAppMonitor(this);
        String packageName = foregroundAppMonitor.getForegroundApp();
        Log.d("ForegroundApp", "Current app: " + packageName);




        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void startForegroundAppMonitoring() {
        Intent serviceIntent = new Intent(this, ForegroundAppService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!UsageAccessHelper.hasUsageAccess(this)) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        }
        startForegroundAppMonitoring();
    }
}