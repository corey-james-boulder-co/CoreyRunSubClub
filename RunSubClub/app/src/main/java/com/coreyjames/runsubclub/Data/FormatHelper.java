package com.coreyjames.runsubclub.Data;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by csteimel on 2/17/17.
 */
public class FormatHelper {

    public static String dateToString(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        String dateString = formatter.format(date);
        return dateString;
    }

    public static Date stringToDate(Object object) {
        Date date = null;
        String dateString = (String) object;
        if (dateString != null){
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            try {
                date = formatter.parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return date;
    }
}
