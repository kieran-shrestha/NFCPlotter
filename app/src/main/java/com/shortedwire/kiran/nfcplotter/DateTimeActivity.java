package com.shortedwire.kiran.nfcplotter;

import android.util.Log;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by kiran on 11/28/2017.
 * <p>
 * This activity has no layout. It gives the date and time in the format required by the data structure that we need.
 * currently the date and time is in reversed format, but I have to make it correct.... It subtracts the interval Log
 * from the current time and then subtract the time by four minutes for each of the data
 */

public class DateTimeActivity {

    public DateTimeActivity() {
    }

    public String[] getDateTime(int numOfLogs, int intervalLog) {

        Calendar[] calendar;
        calendar = new Calendar[numOfLogs];
        String[] dateTime;
        dateTime = new String[numOfLogs];
        String[] holder;
        holder = new String[numOfLogs];

        for (int i = 0; i < numOfLogs; i++) {

            calendar[i] = Calendar.getInstance();
            if (i == 0) {
                calendar[i].add(Calendar.MINUTE, -(intervalLog / 60));
            } else {

                calendar[i].add(Calendar.MINUTE, -(4 * i + intervalLog / 60));
            }
            String formatMonth = String.format(Locale.US, "%02d", calendar[i].get(Calendar.MONTH) + 1);
            String formatDate = String.format(Locale.US, "%02d", calendar[i].get(Calendar.DATE));
            String formatHour = String.format(Locale.US, "%02d", calendar[i].get(Calendar.HOUR_OF_DAY));
            String formatMinute = String.format(Locale.US, "%02d", calendar[i].get(Calendar.MINUTE));

            String date = Long.toString(calendar[i].get(Calendar.YEAR)) + '/' + formatMonth + '/' + formatDate + '\n';

            String time = formatHour + ':' + formatMinute + ',';

            dateTime[i] = ',' + time + date;
//            Log.i("INFO",dateTime[i]);

        }

        for ( int i = 0;i< numOfLogs;i++){
            holder[i] = dateTime[numOfLogs-i-1];

        }

        return holder;
    }

    public String getDateTimeFileName() {

        Calendar calendar;
        String dateTime;


        calendar = Calendar.getInstance();
        String formatMonth = String.format(Locale.US, "%02d", calendar.get(Calendar.MONTH) + 1);
        String formatDate = String.format(Locale.US, "%02d", calendar.get(Calendar.DATE));
        String formatHour = String.format(Locale.US, "%02d", calendar.get(Calendar.HOUR_OF_DAY));
        String formatMinute = String.format(Locale.US, "%02d", calendar.get(Calendar.MINUTE));
        String formatSecond = String.format(Locale.US, "%02d", calendar.get(Calendar.SECOND));

        String date = Long.toString(calendar.get(Calendar.YEAR)) + formatMonth + formatDate;
        String time = formatHour + formatMinute+formatSecond;

        dateTime = date + time;

        return dateTime;
}



}
