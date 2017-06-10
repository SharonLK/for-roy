package com.askylol.bookaseat.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

public class LocationService {

    private static final String baseUrl = "http://blabla.com"; //TODO

    static public JSONObject track(Context context, String username) throws IOException, JSONException {
        try {
            HttpURLConnection connection = (HttpURLConnection)(new URL(baseUrl + "/track")).openConnection();
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("charset", "utf-8");
            OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
            wr.write(getJsonForTrack(username, getWifiFingerprints(context)).toString());
            wr.flush();
            wr.close();

            int responseCode = connection.getResponseCode();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return new JSONObject(response.toString());
        } catch (MalformedURLException e) {
            // Shouldn't happen if url is hardcoded
            e.printStackTrace();
        }
        return null;
    }

    private static JSONObject getJsonForTrack(String username, JSONArray wifiFingerprints) throws JSONException {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("group", "BookaSeat");//TODO: change params
        jsonParam.put("username", username);
        jsonParam.put("location", "location"); //TODO: why do we need this param?
        jsonParam.put("time", Calendar.getInstance().getTimeInMillis());
        jsonParam.put("wifi-fingerprint", wifiFingerprints);
        return jsonParam;
    }

    private static JSONArray getWifiFingerprints(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        JSONArray jsonArray = new JSONArray();
        for (ScanResult result : wifiManager.getScanResults()) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("mac", "bla");
                jsonObject.put("rssi", result.level);
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonArray;
    }
}
