package com.example.weatherapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SplashActivity extends AppCompatActivity {

    private TextView currentTime; // TextView untuk menampilkan jam saat ini
    private Handler handler = new Handler();
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);  // Pastikan layout splash sesuai

        currentTime = findViewById(R.id.currentTime); // Menghubungkan TextView untuk jam
        Button startButton = findViewById(R.id.startButton);

        // Update waktu setiap detik
        runnable = new Runnable() {
            @Override
            public void run() {
                updateTime();
                handler.postDelayed(this, 1000); // Update setiap 1 detik
            }
        };
        handler.post(runnable);

        // Set aksi ketika tombol Start ditekan
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pindah ke MainActivity ketika tombol ditekan
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Menyelesaikan SplashActivity agar tidak bisa kembali ke halaman splash
            }
        });
    }

    private void updateTime() {
        String currentTimeString = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        currentTime.setText(currentTimeString); // Memperbarui TextView dengan waktu saat ini
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable); // Hentikan update saat Activity dihancurkan
    }
}