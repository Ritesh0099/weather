package com.ritesh.wetherapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    Button searchBtn;
    EditText cityEt;
    TextView dataTv;
    FrameLayout progressOverlay;
    FrameLayout rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        searchBtn = findViewById(R.id.search);
        cityEt = findViewById(R.id.cityName);
        dataTv = findViewById(R.id.data);
        progressOverlay = findViewById(R.id.progressOverlay);
        rootLayout = findViewById(R.id.rootLayout);

        searchBtn.setOnClickListener(v -> {
            String city = cityEt.getText().toString().trim();
            if (!city.isEmpty()) {
                showProgress(true);
                searchWeatherData(city);
            } else {
                Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showProgress(boolean show) {
        runOnUiThread(() -> progressOverlay.setVisibility(show ? View.VISIBLE : View.GONE));
    }

    private void searchWeatherData(String cityName) {
        new Thread(() -> {
            String apiKey = "996a7d055083dea4446ec1174df54b20";
            String urlString = "https://api.openweathermap.org/data/2.5/weather?q=" + cityName + ",IN-MH,IN&appid=" + apiKey + "&units=metric";

            try {
                URL url = new URL(urlString);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);

                BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                reader.close();
                parseWeatherData(result.toString());
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(MainActivity.this, "Data not available for this city/state", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void parseWeatherData(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result);

            String city = jsonObject.getString("name");
            JSONObject sys = jsonObject.getJSONObject("sys");
            String country = sys.getString("country");

            JSONObject main = jsonObject.getJSONObject("main");
            double temperature = main.getDouble("temp");

            JSONArray weatherArray = jsonObject.getJSONArray("weather");
            String weatherMain = weatherArray.getJSONObject(0).getString("main").toLowerCase();
            String description = weatherArray.getJSONObject(0).getString("description");

            runOnUiThread(() -> {
                String info = "City: " + city +
                        "\nCountry: " + country +
                        "\nTemperature: " + temperature + "°C" +
                        "\nCondition: " + description;
                dataTv.setText(info);
                showProgress(false);

                // Change background based on weather condition
                if (weatherMain.contains("rain")) {
                    rootLayout.setBackgroundResource(R.drawable.bg_rainy);
                } else if (weatherMain.contains("cloud")) {
                    rootLayout.setBackgroundResource(R.drawable.bg_cloudy);
                } else if (weatherMain.contains("clear")) {
                    rootLayout.setBackgroundResource(R.drawable.bg_sunny);
                } else if (temperature <= 15) {
                    rootLayout.setBackgroundResource(R.drawable.bg_cold);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                showProgress(false);
                Toast.makeText(this, "Error parsing weather data", Toast.LENGTH_SHORT).show();
            });
        }
    }
}
