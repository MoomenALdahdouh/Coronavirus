package com.moomen.coronavirus.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Date;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Utils {

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String calculateDateDiff(String givenDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = inputFormat.parse(givenDate);
            givenDate = outputFormat.format(date);
            String currentDate = outputFormat.format(new Date());

            long secondsDiff = outputFormat.parse(currentDate).getTime() - outputFormat.parse(givenDate).getTime();
            if(secondsDiff<=0)
                return null;
            long[] resultArray = convertDiffInSecToFullDate(outputFormat, secondsDiff);
            int stopIndex = 0;
            for (int i = 0; i <resultArray.length ; i++)
                if (resultArray[i] != 0) {
                    stopIndex = i;
                    break;
                }
            String[] allDateTerms = new String[] {"year", "month", "day", "hour", "minutes", "seconds"};
            return resultArray[stopIndex] + " " + allDateTerms[stopIndex] + "s";
        }catch (Exception ex) { return null; }
    }

    private static long[] convertDiffInSecToFullDate(SimpleDateFormat format, long secondsDiff) {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        format.setTimeZone(timeZone);
        String time = format.format(new Date(secondsDiff));
        int years = Integer.parseInt(time.substring(0, 4)) - 1970;
        int months = Integer.parseInt(time.substring(5, 7)) - 1;
        int days = Integer.parseInt(time.substring(8, 10)) - 1;
        int hours = Integer.parseInt(time.substring(11,13));
        int minutes = Integer.parseInt(time.substring(14,16));
        int seconds = Integer.parseInt(time.substring(17,19));
        return new long[]{years,months,days,hours,minutes,seconds};
    }

    public static String loadJSONFromAsset(Context context, String name) {
        String json;
        try {
            InputStream stream = context.getAssets().open(name);
            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        return true;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        return true;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
