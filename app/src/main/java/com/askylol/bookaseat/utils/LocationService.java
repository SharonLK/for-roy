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
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

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
    public static final Integer NOTIFICATION_ID = 0xFF1;
    private static final String baseUrl = "https://ml.internalpositioning.com";

    public static CountDownTimer timer = null;

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
            boolean showToast = false;
            boolean showCountdownNotification = false;

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                if (showToast) {
                    ((Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(300);
                    Toast.makeText(context, R.string.in_library_message, Toast.LENGTH_LONG).show();
                }
                showToast = false;

                if (showCountdownNotification && Data.INSTANCE.library != null) {
                    if (timer != null) {
                        timer.cancel();
                    }

                    timer = new Timer(Data.INSTANCE.library.getMaxDelay() * 60 * 1000, 1000, context);
                    timer.start();
                }
                showCountdownNotification = false;
            }

            @Override
            protected Void doInBackground(String... params) {
                try {
                    JSONObject res = LocationService.track(context, Data.INSTANCE.mail);

                    if (res == null || !res.getBoolean("success")) {
                        return null;
                    }

                    if ("library".equals(res.getString("location"))) {
                        if (!Data.INSTANCE.isInLibrary) {
                            Data.INSTANCE.isInLibrary = true;
                            ((NotificationManager) context.getSystemService(NOTIFICATION_SERVICE))
                                    .cancel(LocationService.NOTIFICATION_ID);
                            showToast = true;
                        }
                        markReservedSeatForUser(Data.INSTANCE.mail);
                    } else {
                        Data.INSTANCE.isInLibrary = false;
                        if (Data.INSTANCE.isSitting) {
                            Data.INSTANCE.isSitting = false;

                            if (Data.INSTANCE.library == null) {
                                return null;
                            }

                            Calendar now = Calendar.getInstance();
                            Pair<String, Reservation> reservationPair = Data.INSTANCE.library.reservationByUser(now, Data.INSTANCE.mail);

                            if (reservationPair == null || reservationPair.first == null || reservationPair.second == null) {
                                return null;
                            }

                            String seatId = reservationPair.first;
                            Reservation reservation = reservationPair.second;

                            reservation.setOccupied(false);
                            reservation.setLastSeen(CalendarUtils.getTimeOfDay(now));
                            Data.INSTANCE.library.updateReservation(seatId, reservation);
                            Intent openDialogIntent = new Intent(context, MainActivity.class);
                            openDialogIntent.putExtra("notificationStatus", MainActivity.NOTIFICATION_CLICK);

                            if (Data.INSTANCE.isInForeground.get()) {
                                context.startActivity(openDialogIntent);
                                return null;
                            }

                            final Intent keepSeatIntent = new Intent(context, MainActivity.class);
                            keepSeatIntent.putExtra("notificationStatus", MainActivity.NOTIFICATION_YES);
                            Intent freeSeatIntent = new Intent(context, MainActivity.class);
                            freeSeatIntent.putExtra("notificationStatus", MainActivity.NOTIFICATION_NO);

                            final PendingIntent pIntentKeepSeat = PendingIntent.getActivity(context, NOTIFICATION_REQUEST_CODE, keepSeatIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                            final PendingIntent pIntentFreeSeat = PendingIntent.getActivity(context, NOTIFICATION_REQUEST_CODE + 1, freeSeatIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                            final PendingIntent pIntentOpenDialog = PendingIntent.getActivity(context, NOTIFICATION_REQUEST_CODE + 2, openDialogIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                            Notification notification = new NotificationCompat.Builder(context)
                                    .setSmallIcon(R.drawable.app_icon)
                                    .setContentTitle(context.getString(R.string.notification_title))
                                    .setContentText(context.getString(R.string.notification_content))
                                    .setContentIntent(pIntentOpenDialog)
                                    .setAutoCancel(true)
                                    .setOngoing(true)
                                    .addAction(R.drawable.ic_done_black_24dp, context.getString(R.string.yes), pIntentKeepSeat)
                                    .addAction(R.drawable.ic_clear_black_24dp, context.getString(R.string.no), pIntentFreeSeat)
                                    .build();
                            notification.defaults |= Notification.DEFAULT_SOUND;
                            notification.defaults |= Notification.DEFAULT_VIBRATE;
                            notification.flags |= Notification.FLAG_AUTO_CANCEL;

                            final NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
                            notificationManager.notify(NOTIFICATION_ID, notification);

                            Data.INSTANCE.keepNotification = true;
                            showCountdownNotification = true;
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
        if (library == null)
            return;

        Calendar currentTime = Calendar.getInstance();
        Pair<String, Reservation> reservationPair = library.reservationByUser(currentTime, username);
        if (reservationPair == null)
            return;
        String seatId = reservationPair.first;
        Reservation reservation = reservationPair.second;
        if (reservation != null && !reservation.isOccupied() &&
                reservation.getStart().add(library.getMaxDelay()).isAfter(CalendarUtils.getTimeOfDay(currentTime))) {
            Data.INSTANCE.isSitting = true;
            reservation.setOccupied(true);
            library.updateReservation(seatId, reservation);
        }
    }

    private static class Timer extends CountDownTimer {
        private Context context;

        public Timer(long millisInFuture, long countDownInterval, Context context) {
            super(millisInFuture, countDownInterval);

            this.context = context;
        }

        @Override
        public void onTick(long millisUntilFinished) {
            Intent openDialogIntent = new Intent(context, MainActivity.class);
            openDialogIntent.putExtra("notificationStatus", MainActivity.NOTIFICATION_CLICK);

            final Intent keepSeatIntent = new Intent(context, MainActivity.class);
            keepSeatIntent.putExtra("notificationStatus", MainActivity.NOTIFICATION_YES);
            Intent freeSeatIntent = new Intent(context, MainActivity.class);
            freeSeatIntent.putExtra("notificationStatus", MainActivity.NOTIFICATION_NO);

            final PendingIntent pIntentKeepSeat = PendingIntent.getActivity(context, NOTIFICATION_REQUEST_CODE, keepSeatIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            final PendingIntent pIntentFreeSeat = PendingIntent.getActivity(context, NOTIFICATION_REQUEST_CODE + 1, freeSeatIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            final PendingIntent pIntentOpenDialog = PendingIntent.getActivity(context, NOTIFICATION_REQUEST_CODE + 2, openDialogIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            long seconds = millisUntilFinished / 1000;
            long minutes = seconds / 60;
            seconds -= minutes * 60;

            Notification notification = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.app_icon)
                    .setContentTitle(context.getString(R.string.notification_title))
                    .setContentText(String.format(context.getString(R.string.notification_content), minutes, seconds))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(String.format(context.getString(R.string.notification_content), minutes, seconds)))
                    .setContentIntent(pIntentOpenDialog)
                    .setAutoCancel(true)
                    .setOngoing(true)
                    .addAction(R.drawable.ic_done_black_24dp, context.getString(R.string.yes), pIntentKeepSeat)
                    .addAction(R.drawable.ic_clear_black_24dp, context.getString(R.string.no), pIntentFreeSeat)
                    .build();
            notification.flags |= Notification.FLAG_AUTO_CANCEL;

            final NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID, notification);

            if (!Data.INSTANCE.keepNotification) {
                cancel();
            }
        }

        @Override
        public void onFinish() {
            Data.INSTANCE.keepNotification = false;
        }
    }
}
