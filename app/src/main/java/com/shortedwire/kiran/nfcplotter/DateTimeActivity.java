package com.shortedwire.kiran.nfcplotter;
import android.util.Log;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by kiran on 11/28/2017.
 */

public class DateTimeActivity {

    public DateTimeActivity( ){
     }

    public String[] getDateTime(int numOfLogs,int intervalLog){

        Calendar[] calendar;
        calendar = new Calendar[numOfLogs];
        String[] dateTime;
        dateTime = new String[numOfLogs];

        for(int i = 0;i<numOfLogs;i++) {

            calendar[i] = Calendar.getInstance();
            if(i ==0 ){
                calendar[i].add(Calendar.MINUTE,-(intervalLog/60));
            }else {

                calendar[i].add(Calendar.MINUTE, -(4 * i+intervalLog/60));
            }
            String formatMonth = String.format(Locale.US, "%02d", calendar[i].get(Calendar.MONTH)+1);
            String formatDate = String.format(Locale.US, "%02d", calendar[i].get(Calendar.DATE));
            String formatHour = String.format(Locale.US, "%02d", calendar[i].get(Calendar.HOUR_OF_DAY));
            String formatMinute = String.format(Locale.US, "%02d", calendar[i].get(Calendar.MINUTE));

            String date = Long.toString(calendar[i].get(Calendar.YEAR)) + '/' + formatMonth + '/' + formatDate + '\n';

            String time = formatHour + ':' + formatMinute + ',';

            dateTime[i] = ',' + time + date;
//            Log.i("INFO",dateTime[i]);

        }
        return dateTime;
    }

}
