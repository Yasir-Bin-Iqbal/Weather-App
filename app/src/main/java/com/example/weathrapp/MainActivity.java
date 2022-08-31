package com.example.weathrapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.OnSwipe;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    TextView city,date_time,currentTemp,minMax,descrip,wind,humidity,pressure,visibility;
    SwipeRefreshLayout swipeRefreshLayout;
    Dialog dialog;
    SharedPreferences sharedPreferences;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView2);
        city = findViewById(R.id.cityTextView);
        date_time = findViewById(R.id.DateTimeTextView);
        currentTemp = findViewById(R.id.TempTextView);
        minMax = findViewById(R.id.MinMaxTempTextView);
        descrip = findViewById(R.id.DescrptextView);
        wind = findViewById(R.id.WindTextView);
        humidity = findViewById(R.id.HumidityTextView);
        pressure = findViewById(R.id.PressureTextView);
        visibility = findViewById(R.id.VisibilityTextView);
        dialog = new Dialog(this);
        sharedPreferences = getSharedPreferences("Location",MODE_PRIVATE);
        swipeRefreshLayout = findViewById(R.id.SwipeRefreshLayout);


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                GetWeather(sharedPreferences.getString("city",""));
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(MainActivity.this, "Weather Updated !", Toast.LENGTH_SHORT).show();
            }
        });


        File file = new File("/data/data/com.example.weathrapp/shared_prefs/Location.xml");
        if (file.exists()){
            String city = sharedPreferences.getString("city","error getting name");
            GetWeather(city);
        }
        else{

            SharedPreferences.Editor editor = sharedPreferences.edit();
            dialog.setContentView(R.layout.location);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setCancelable(false);

            EditText location = dialog.findViewById(R.id.LocationeditText);
            Button save = dialog.findViewById(R.id.Savebutton);

            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(location.getText().toString().isEmpty()){
                        Toast.makeText(getApplicationContext(), "Please enter city name !", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        editor.putString("city",location.getText().toString());
                        editor.apply();

                        GetWeather(location.getText().toString());

                        dialog.dismiss();

                    }
                }
            });
            dialog.show();
        }


    }

    public void GetWeather( String city){
        Weather weather = new Weather();
        weather.execute("https://api.openweathermap.org/data/2.5/weather?q=" + city +"&appid=b045804ab93431828b3e101e2be26dc1");
    }

    public String GetDate(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E dd MMM K:mm a ");
        String date = simpleDateFormat.format(calendar.getTime());
        return date;
    }

    public class Weather extends AsyncTask<String , Void , String>{

        @Override
        protected String doInBackground(String... strings) {
            String result = "";
            URL url;
            HttpURLConnection httpURLConnection = null;

            try{
                url = new URL(strings[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                int data = inputStreamReader.read();

                while(data != -1){
                    char current = (char) data;
                    result += current;
                    data = inputStreamReader.read();
                }
                return result;
            }catch(Exception e){
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                JSONObject jsonObject = new JSONObject(s);

                String weather = jsonObject.getString("weather");
                JSONArray jsonArray = new JSONArray(weather);

                String description="";
                String icon="";

                for(int i=0; i< jsonArray.length(); i++){
                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                     description = jsonObject1.getString("description");
                     icon = jsonObject1.getString("icon");
                }



                JSONObject main = jsonObject.getJSONObject("main");
                String temp= main.getString("temp");
                String feelslike = main.getString("feels_like");
                String minTemp = main.getString("temp_min");
                String maxTemp = main.getString("temp_max");
                String pressure = main.getString("pressure");
                String humidity = main.getString("humidity");

                String visibility = jsonObject.getString("visibility");

                JSONObject wind = jsonObject.getJSONObject("wind");
                String windspeed = wind.getString("speed");

                String city = jsonObject.getString("name");

                setValues(city,description,icon,temp,feelslike,minTemp,maxTemp,windspeed,pressure,humidity,visibility);

               Log.i("name",city);
                Log.i("description:",description);
                Log.i("icon",icon);
                Log.i("temp",temp);
                Log.i("feelslike",feelslike);
                Log.i("min",minTemp);
                Log.i("max",maxTemp);
                Log.i("wind",windspeed);
                Log.i("pressure",pressure);
                Log.i("humidity",humidity);
                Log.i("visibility",visibility);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    public class Icon extends AsyncTask<String, Void , Bitmap>{

        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap bitmap;
            URL url;
            HttpURLConnection httpURLConnection = null;

            try{
                url = new URL(strings[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                bitmap= BitmapFactory.decodeStream(inputStream);
                return bitmap;
            }catch(Exception e){
                e.printStackTrace();
                return  null;
            }

        }
    }

    public void setValues(String cityname,String description,String iconNo,String temp,String feelslike,String min,String max,String windSpeed,String pressureValue,String humidityValue,String visibilityValue ){
        int Temperature = (int) (Float.parseFloat(temp) - 273.15);
        int minTemperature = (int) (Float.parseFloat(min) - 273.15);
        int maxTemperature = (int) (Float.parseFloat(max) - 273.15);
        int feels = (int) (Float.parseFloat(feelslike) - 273.15);
        int winds = (int) ((int) (Float.parseFloat(windSpeed)) * 3.6);
        int visible = (int) (Float.parseFloat(visibilityValue)) / 1000;
        String date = GetDate();

        Icon icon = new Icon();
        try {
            imageView.setImageBitmap(icon.execute("https://openweathermap.org/img/wn/" + iconNo + "@2x.png").get());
        }catch(Exception e){
            e.printStackTrace();
        }
        city.setText(cityname);
        date_time.setText(date);
        currentTemp.setText(Temperature + "\u2103");
        minMax.setText(minTemperature + "\u2103" + "/" + maxTemperature + "\u2103"+" Feels like "+ feels + "\u2103");
        descrip.setText(description);
        wind.setText(winds + " km/h");
        humidity.setText(humidityValue + " %");
        pressure.setText(pressureValue + " mb");
        visibility.setText(visible + " km");
    }

    public void NewlocationDialog(View view){
        dialog.setContentView(R.layout.change_location);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);

        EditText location  = dialog.findViewById(R.id.NewLocationEditText);
        Button save = dialog.findViewById(R.id.NewLocationSaveButton);
        Button  cancel = dialog.findViewById(R.id.NewLocationcancelButton);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(location.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(), "Please enter city name !", Toast.LENGTH_SHORT).show();
                }
                else{
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("city",location.getText().toString());
                    editor.apply();

                    GetWeather(location.getText().toString());

                    dialog.dismiss();

                }

            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

}