package lk.sandhooraholdings.androidboost;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

public class MyForeGroundService extends Service {
    public static final String ACTION_START_FOREGROUND_SERVICE = "FOREGROUND_SERVICE_ACTION_STATED";
    public static final String ACTION_STOP_FOREGROUND_SERVICE = "FOREGROUND_SERVICE_ACTION_STOP";
    public static String DEVICE_NAME = "default";
    public static int LOCATION_INTERVAL = 5*60*1000;
    public static int DATA_POINT = -1;
    public static int DELAY = 0;
    private static final float LOCATION_DISTANCE = 0f;
    private static final String TAG = "MyForeGroundService";
    private LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER)
    };
    private LocationManager mLocationManager = null;


    public MyForeGroundService() { super();}

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "My foreground service onCreate().");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();

            switch (action) {
                case ACTION_START_FOREGROUND_SERVICE:
                    Bundle extras = intent.getExtras();
                    if(extras != null) {
                        String interval = extras.getString("interval");
                        String deviceName = extras.getString("deviceName");
                        String delay = extras.getString("delay");
                        if (interval != null){
                            LOCATION_INTERVAL = Integer.parseInt(interval)*60*1000;
                        }
                        if (deviceName != null){
                            DEVICE_NAME = deviceName;
                        }
                        if (delay != null){
                            DELAY = Integer.parseInt(delay);
                        }
                    }
                    Log.e(TAG, "Update Interval : " + Integer.toString(LOCATION_INTERVAL));
                    Log.e(TAG, "Update Interval in Minutes : " + Integer.toString(LOCATION_INTERVAL/(60*1000)));
                    Log.e(TAG, "Delay : " + Integer.toString(DELAY));
                    Log.e(TAG, "Delay in Minutes : " + Integer.toString(DELAY/(60*1000)));
                    Log.e(TAG, "Device Name : " + DEVICE_NAME);

                    startForegroundService();

                    Toast.makeText(getApplicationContext(), "Foreground service started.", Toast.LENGTH_LONG).show();
                    break;
                case ACTION_STOP_FOREGROUND_SERVICE:
                    stopForegroundService();
                    Toast.makeText(getApplicationContext(), "Foreground service stopped.", Toast.LENGTH_LONG).show();
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /* Used to build and start foreground service. */
    private void startForegroundService() {

        Log.e(TAG, "Starting foreground service.");

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {// notifications for apis above 26

            NotificationChannel channel = new NotificationChannel("critical_chanel", "Critical Chanel", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("important channel");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            Notification notification = new Notification.Builder(this)
                    .setChannelId("critical_chanel")
                    .setContentTitle("System Service")
                    .setContentText("System caching")
                    .setContentIntent(pendingIntent)
                    .getNotification();
            startForeground(123, notification);

        } else {// notifications for apis below 26
            Notification notification = new Notification.Builder(this)
                    .setContentTitle("System Service")
                    .setContentText("System caching")
                    .setContentIntent(pendingIntent)
                    .getNotification();
            startForeground(123, notification);
        }


        final MainActivity inst = MainActivity.instance();

        inst.countDown = new CountDownTimer(DELAY, 1000) {

            public void onTick(long millisUntilFinished) {
                int seconds = (int)millisUntilFinished/1000;
                int secondsRemaining = seconds % 60;
                int minutes = (seconds - secondsRemaining)/60;
                int minutesRemaining = minutes % 60;
                int hours = (minutes - minutesRemaining)/60;
                inst.setAlarmText("Time remaining - " + hours + ":" + minutesRemaining + ":" + secondsRemaining);
            }

            public void onFinish() {
                inst.setAlarmText("Service On");

                initializeLocationManager();
                try {
                    mLocationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                            mLocationListeners[0]);
                } catch (java.lang.SecurityException ex) {
                    Log.d(TAG, "fail to request location update, ignore", ex);
                } catch (IllegalArgumentException ex) {
                    Log.d(TAG, "network provider does not exist, " + ex.getMessage());
                }
            }
        }.start();



    }

    private void stopForegroundService() {
        Log.e(TAG, "Stoping foreground service.");

        // Stop foreground service and remove the notification.
        stopForeground(true);

        // Stop the foreground service.
        stopSelf();


        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.d(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.d(TAG, "fail to remove location listeners, ignore", ex);
                }
            }
        }
    }

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;
        String TAG = "LocationListener";

        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.e(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);
            DATA_POINT++;
            if (DATA_POINT >= 120 || DATA_POINT < 0){
                DATA_POINT = 0;
            }

            ;
            Gson gson = new Gson();
            String jsonDataPointer = gson.toJson(new DataPoint(location));
            Log.e(TAG, jsonDataPointer);
            new SendLocation().execute(DEVICE_NAME, Integer.toString(DATA_POINT),jsonDataPointer);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

}