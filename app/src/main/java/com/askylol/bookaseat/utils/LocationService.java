package com.askylol.bookaseat.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;

import com.askylol.bookaseat.logic.Library;
import com.askylol.bookaseat.logic.Reservation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class LocationService extends BroadcastReceiver {

    private static final String baseUrl = "https://ml.internalpositioning.com"; //TODO

    static public JSONObject track(Context context, String username) throws JSONException, IOException {
        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");

        RequestBody body = RequestBody.create(JSON, getJsonForTrack(username, getWifiFingerprints(context)).toString());

        OkHttpClient client = new OkHttpClient.Builder()
                .build();

        Request request = new Request.Builder()
                .url(baseUrl + "/track")
                .post(body)
                .build();

        Response response = client.newCall(request).execute();
        int responseCode = response.code();
        ResponseBody responseBody = response.body();
        return responseBody == null ? null : new JSONObject(responseBody.string());
    }

    private static JSONObject getJsonForTrack(String username, JSONArray wifiFingerprints) throws JSONException {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("group", "Bookaseat1");//TODO: change params
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
                jsonObject.put("mac", result.BSSID);
                jsonObject.put("rssi", result.level);
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonArray;
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... params) {
                try {
                    JSONObject res = LocationService.track(context, Data.INSTANCE.username);
                    if (res != null && res.getBoolean("success")
                            && res.getString("location").equals("library")) {
                        markReservedSeatForUser(Data.INSTANCE.username);
                    }

                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(Data.INSTANCE.username);
    }

    private void markReservedSeatForUser(String username) {
        Library library = Data.INSTANCE.library;
        Calendar currentTime = Calendar.getInstance();
        Pair<String, Reservation> reservationPair = library.reservationByUser(currentTime, username);
        String seatId = reservationPair.first;
        Reservation reservation = reservationPair.second;
        if (reservation != null &&
                reservation.getStart().add(library.getIdleLimit()).isAfter(CalendarUtils.getTimeOfDay(currentTime))) {
            reservation.setOccupied(true);
            library.updateReservation(seatId, reservation);
        }
    }
}
