package lk.sandhooraholdings.androidboost;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;
import java.util.Calendar;
import java.util.Timer;
import android.os.CountDownTimer;

public class MainActivity extends AppCompatActivity { // implements View.OnClickListener{

    AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private TimePicker alarmTimePicker;
    private static MainActivity inst;
    private TextView alarmTextView;
    CountDownTimer countDown;

    EditText deviceName;
    EditText interval;

    ToggleButton alarmToggle;
    ToggleButton serviceToggle;

    public static MainActivity instance() {
        return inst;
    }

    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); // hide the title bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //enable full screen

        setContentView(R.layout.activity_main);

        this.alarmTimePicker = (TimePicker) findViewById(R.id.alarmTimePicker);
        this.alarmTextView = (TextView) findViewById(R.id.alarmText);
        this.alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        this.deviceName = findViewById(R.id.deviceNameTextField);
        this.interval = findViewById(R.id.intervalTextField);


        this.alarmToggle = (ToggleButton) findViewById(R.id.alarmToggle);
        this.alarmToggle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onAlarmToggleClicked(v);
            }
        });

        this.serviceToggle = (ToggleButton) findViewById(R.id.serviceToggle);
        this.serviceToggle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onServiceToggleClicked(v);
            }
        });

        ActivityCompat.requestPermissions(this, new String[]{
                "android.permission.FOREGROUND_SERVICE",
                "android.permission.INTERNET",
                "android.permission.ACCESS_FINE_LOCATION",
                "android.permission.ACCESS_NETWORK_STATE",
                "android.permission.WAKE_LOCK"}, 123);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    public void onAlarmToggleClicked(View view) {
        if (((ToggleButton) view).isChecked()) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, alarmTimePicker.getCurrentHour());
            calendar.set(Calendar.MINUTE, alarmTimePicker.getCurrentMinute());
            Calendar calendarNow = Calendar.getInstance();
            final int DelayInMiliSeconds = Math.abs((int)(calendar.getTimeInMillis() - calendarNow.getTimeInMillis()));

            startForeGroundService(DelayInMiliSeconds);

        } else {
            stopForeGroundService();
            this.countDown.cancel();
            setAlarmText("Service Off");
            Log.e("MyActivity", "Alarm Off");
        }
    }

    public void onServiceToggleClicked(View view) {
        if (((ToggleButton) view).isChecked()) {
            Log.e("MyActivity", "Service On");
            startForeGroundService(0);
        } else {
            stopForeGroundService();
            setAlarmText("Service Off");
            Log.e("MyActivity", "Service Off");
        }
    }

    public void setAlarmText(String alarmText) {
        alarmTextView.setText(alarmText);
    }


    public void startForeGroundService(int delay) {
        String device = this.deviceName.getText().toString();
        String interval = this.interval.getText().toString();
        Intent intent = new Intent(MainActivity.this, MyForeGroundService.class);
        intent.putExtra("deviceName", device);
        intent.putExtra("interval", interval);
        intent.putExtra("delay", Integer.toString(delay));
        intent.setAction(MyForeGroundService.ACTION_START_FOREGROUND_SERVICE);
        startService(intent);
    }

    public void stopForeGroundService() {
        Intent intent = new Intent(MainActivity.this, MyForeGroundService.class);
        intent.setAction(MyForeGroundService.ACTION_STOP_FOREGROUND_SERVICE);
        startService(intent);
    }

}

