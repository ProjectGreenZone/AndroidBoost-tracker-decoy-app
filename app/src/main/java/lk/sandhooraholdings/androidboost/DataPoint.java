package lk.sandhooraholdings.androidboost;

import android.location.Location;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DataPoint {
    String date;
    String location;
    String location_accuracy;
    String location_accuracy_net;
    String location_altitude;
    String location_net;
    String location_speed;
    String location_time;
    String location_time_net;
    String time;
    String time_sec;
    DataPoint(Location location){
        if(location.hasAccuracy()){
            this.location_accuracy = Float.toString(location.getAccuracy());
            this.location_accuracy_net = Float.toString(location.getAccuracy());
        } else {
            this.location_accuracy = "0";
            this.location_accuracy_net = "0";
        }
        if(location.hasAltitude()){
            this.location_altitude = Double.toString(location.getAltitude());
        } else {
            this.location_altitude = "0";
        }
        if(location.hasSpeed()){
            this.location_speed = Float.toString(location.getSpeed());
        } else {
            this.location_speed = "0";
        }

        this.location = Double.toString(location.getLatitude()).concat(",").concat(Double.toString(location.getLongitude()));
        this.location_net = this.location;
        this.time_sec = Long.toString(location.getTime());
        this.location_time = this.time_sec;
        this.location_time_net = this.time_sec;

        Format dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Format timeFormat = new SimpleDateFormat("HH.mm");
        Date date = new Date(location.getTime());
        this.time = timeFormat.format(date);
        this.date = dateFormat.format(date);
    }

}
