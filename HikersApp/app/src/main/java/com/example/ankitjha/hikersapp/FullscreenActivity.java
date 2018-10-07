package com.example.ankitjha.hikersapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.FileNotFoundException;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */

    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };
//--------------------------------------------------------------------
    TextView lat,lng,alt,acc,add,temps;
    Location lastLoc;
    LocationManager locationManager;
    LocationListener locationListener;
    //ProgressBar bar;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, locationListener);
        }
    }


    public class weathers extends AsyncTask<String,Void,String>
    {

        @Override
        protected String doInBackground(String... strings) {
            try {
                Log.i("City",strings[0]);
                URL url=new URL("https://api.openweathermap.org/data/2.5/weather?q="+strings[0]+"&APPID=5b1625e2a9a935be9c3f96b63e556cc7");
                HttpURLConnection connection=(HttpURLConnection) url.openConnection();
                InputStream stream=connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(stream);
                int data=reader.read();
                String content="";
                while(data!=-1)
                {
                    content+=(char)data;
                    data=reader.read();
                }
                return content;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "SomeError";
        }
        @Override
        public void onPostExecute(String str)
        {
            if(str=="SomeError")
            {
                Toast.makeText(getApplicationContext(),"There was some error!!",Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                String temp1;
                JSONObject object = new JSONObject(str);
                try{temp1=object.getString("message");
                if(temp1=="city not found")
                {
                    Toast.makeText(getApplicationContext(),"City Not Found",Toast.LENGTH_SHORT).show();
                    return;
                }}catch(Exception e){}
                temp1=object.getString("main");
                JSONObject obj=new JSONObject(temp1);
                String temperature=obj.getString("temp");
                double t=Double.parseDouble(temperature);
                t=t-273.15;
                temps.setText("Temperature: "+Double.toString(t)+" C");
                //bar.setVisibility(View.INVISIBLE);
            }
            catch(JSONException e)
            {
                e.printStackTrace();
            }

        }
    }
    public void getTemp(String city)
    {
        weathers w=new weathers();
        w.execute(city);
    }

    public String assignVal()
    {
        if(lastLoc!=null){
        lat.setText("Latitude: "+Double.toString(lastLoc.getLatitude()));
        lng.setText("Longitude: "+Double.toString(lastLoc.getLongitude()));
        alt.setText("Altitude: "+Double.toString(lastLoc.getAltitude()));}
        acc.setText("Accuracy: "+Double.toString(lastLoc.getAccuracy()));
        Geocoder code=new Geocoder(getApplicationContext(), Locale.getDefault());
        String city="";
        try {
            List<Address> locAddr = code.getFromLocation(lastLoc.getLatitude(), lastLoc.getLongitude(), 1);
            String address="";
            add.setText(("City: "+locAddr.get(0).getSubAdminArea()));
            city=locAddr.get(0).getSubAdminArea().toString().toLowerCase();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return city;
    }
    public void getLocView(View view)
    {

        //bar.setVisibility(View.VISIBLE);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }
        else
        {
            Location loc1,loc2;
            loc1=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            loc2=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if(loc1==null)lastLoc=loc2;
            else if(loc2==null)lastLoc=loc1;
            else
            {
                lastLoc=loc1.getAccuracy()>loc2.getAccuracy()?loc1:loc2;
            }
            String city=assignVal();
            if(city=="")return;
            getTemp(city);
        }
        //bar.setVisibility(View.INVISIBLE);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);
        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });
        lat=findViewById(R.id.lat);
        lng=findViewById(R.id.lng);
        alt=findViewById(R.id.alt);
        acc=findViewById(R.id.acc);
        add=findViewById(R.id.add);
        temps=findViewById(R.id.temp);
        //bar=findViewById(R.id.load);
        locationManager=(LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        locationListener=new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //Log.i("Location",location.toString());
                lastLoc=location;
                //assignVal();
            }
            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }
        else
            {
              locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
            }
        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        //findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
