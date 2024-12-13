package com.example.weatherapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private TextView cityNameText, temperatureText, humidityText, descriptionText, windText, currentTime;
    private ImageView weatherIcon;
    private Button refreshButton;
    private EditText cityNameInput;
    private static final String API_KEY = "27318b909c2d303cbe82a86e231f0fdb";

    private Handler handler = new Handler();
    private Runnable runnable;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityNameText = findViewById(R.id.cityNameText);
        temperatureText = findViewById(R.id.temperatureText);
        humidityText = findViewById(R.id.humidityText);
        windText = findViewById(R.id.windText);
        descriptionText = findViewById(R.id.descriptionText);
        weatherIcon = findViewById(R.id.weatherIcon);
        refreshButton = findViewById(R.id.fetchWeatherButton);
        cityNameInput = findViewById(R.id.cityNameInput);
        currentTime = findViewById(R.id.currentTime); // Inisialisasi TextView untuk jam

        refreshButton.setOnClickListener(view -> {
            String cityName = cityNameInput.getText().toString();
            if (!cityName.isEmpty()) {
                fetchWeatherData(cityName);
            } else {
                cityNameInput.setError("Please Enter a City Name");
            }
        });

        fetchWeatherData("Manado");

        // Memulai pembaruan waktu
        startUpdatingTime();
    }

    private void startUpdatingTime() {
        runnable = new Runnable() {
            @Override
            public void run() {
                updateTime();
                handler.postDelayed(this, 1000); // Memperbarui setiap 1 detik
            }
        };
        handler.post(runnable); // Memulai runnable
    }

    private void updateTime() {
        String currentTimeString = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        currentTime.setText(currentTimeString); // Memperbarui TextView dengan waktu saat ini
    }

    private void fetchWeatherData(String cityName) {
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + cityName + "&appid=" + API_KEY + "&units=metric";
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String result = response.body().string();
                    runOnUiThread(() -> updateUi(result));
                } else {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Failed to fetch weather data", Toast.LENGTH_SHORT).show());
                }
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Network error occurred", Toast.LENGTH_SHORT).show());
                e.printStackTrace();
            }
        });
    }

    private void updateUi(String result) {
        if (result != null) {
            try {
                JSONObject jsonObject = new JSONObject(result);

                if (jsonObject.has("main") && jsonObject.has("wind") && jsonObject.has("weather")) {
                    JSONObject main = jsonObject.getJSONObject("main");
                    double temperature = main.getDouble("temp");
                    double humidity = main.getDouble("humidity");
                    double windSpeed = jsonObject.getJSONObject("wind").getDouble("speed");

                    String description = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");
                    String iconCode = jsonObject.getJSONArray("weather").getJSONObject(0). getString("icon");
                    String resourceName = "ic_" + iconCode;
                    int resId = getResources().getIdentifier(resourceName, "drawable", getPackageName());

                    cityNameText.setText(jsonObject.getString("name"));
                    temperatureText.setText(String.format("%.0f°", temperature));
                    humidityText.setText(String.format("%.0f%%", humidity));
                    windText.setText(String.format("%.0f km/h", windSpeed));
                    descriptionText.setText(description);

                    if (resId != 0) {
                        weatherIcon.setImageResource(resId);
                    } else {
                        weatherIcon.setImageResource(R.drawable.default_icon);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Invalid data received", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                Toast.makeText(MainActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable); // Menghentikan pembaruan saat aktivitas dihancurkan
    }
}