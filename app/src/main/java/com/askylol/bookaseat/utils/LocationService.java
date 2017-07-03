package com.askylol.bookaseat.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;

import com.askylol.bookaseat.R;
import com.askylol.bookaseat.activities.MainActivity;
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

import static android.content.Context.NOTIFICATION_SERVICE;

public class LocationService extends BroadcastReceiver {

    private static final Integer NOTIFICATION_REQUEST_CODE = 123;
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
        jsonParam.put("mail", username);
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
                    JSONObject res = LocationService.track(context, Data.INSTANCE.mail);

                    if (res == null || !res.getBoolean("success")) {
                        return null;
                    }

                    if (res.getString("location").equals("library")) {
                        markReservedSeatForUser(Data.INSTANCE.mail);
                    } else {
                        if (Data.INSTANCE.isSitting) {
                            Data.INSTANCE.isSitting = false;

                            Calendar now = Calendar.getInstance();
                            Pair<String, Reservation> reservationPair = Data.INSTANCE.library.reservationByUser(now, Data.INSTANCE.mail);

                            if (reservationPair != null && reservationPair.first != null && reservationPair.second != null) {
                                reservationPair.second.setOccupied(false);
                                reservationPair.second.setLastSeen(CalendarUtils.getTimeOfDay(now));
                                Data.INSTANCE.library.updateReservation(reservationPair.first, reservationPair.second);

                                Intent keepSeatIntent = new Intent(context, MainActivity.class);
                                keepSeatIntent.putExtra("notificationStatus", MainActivity.NOTIFICATION_YES);
                                Intent freeSeatIntent = new Intent(context, MainActivity.class);
                                freeSeatIntent.putExtra("notificationStatus", MainActivity.NOTIFICATION_NO);
                                Intent openDialogIntent = new Intent(context, MainActivity.class);
                                openDialogIntent.putExtra("notificationStatus", MainActivity.NOTIFICATION_CLICK);

                                PendingIntent pIntentKeepSeat = PendingIntent.getActivity(context, NOTIFICATION_REQUEST_CODE, keepSeatIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                                PendingIntent pIntentFreeSeat = PendingIntent.getActivity(context, NOTIFICATION_REQUEST_CODE, freeSeatIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                                PendingIntent pIntentOpenDialog = PendingIntent.getActivity(context, NOTIFICATION_REQUEST_CODE, openDialogIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                                Notification notification = new NotificationCompat.Builder(context)
                                        .setSmallIcon(R.drawable.app_icon)
                                        .setContentTitle(context.getString(R.string.notification_title))
                                        .setContentText(context.getString(R.string.notification_content))
                                        .setContentIntent(pIntentOpenDialog)
                                        .setAutoCancel(true)
                                        .addAction(R.drawable.ic_done_black_24dp, context.getString(R.string.yes), pIntentKeepSeat)
                                        .addAction(R.drawable.ic_clear_black_24dp, context.getString(R.string.no), pIntentFreeSeat)
                                        .build();
                                notification.defaults |= Notification.DEFAULT_SOUND;
                                notification.defaults |= Notification.DEFAULT_VIBRATE;

                                NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
                                notificationManager.notify(0xFFFF, notification);
                            }
                        }
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(Data.INSTANCE.mail);
    }

    private void markReservedSeatForUser(String username) {
        Library library = Data.INSTANCE.library;
        Calendar currentTime = Calendar.getInstance();
        Pair<String, Reservation> reservationPair = library.reservationByUser(currentTime, username);
        String seatId = reservationPair.first;
        Reservation reservation = reservationPair.second;
        if (reservation != null &&
                reservation.getStart().add(library.getMaxDelay()).isAfter(CalendarUtils.getTimeOfDay(currentTime))) {
            reservation.setOccupied(true);
            library.updateReservation(seatId, reservation);
        }
    }
}
