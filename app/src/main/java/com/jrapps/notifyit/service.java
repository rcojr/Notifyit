package com.jrapps.notifyit;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.net.URI;

public class service extends Service
{
    private static final String TAG = "MyService";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onStart(Intent intent, int startid)
    {

        Log.d(TAG, "onStart");

        //BACKGROUND OPERATIONS

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean service = preferences.getBoolean("service", true);

        if (service) {
            LocalBroadcastManager.getInstance(service.this).registerReceiver(onNotice, new IntentFilter("Msg"));
        }else {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(onNotice);
            stopService(new Intent(service.this, NotificationService.class));
        }


    }

    private BroadcastReceiver onNotice= new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String pack = intent.getStringExtra("package");
            String title = intent.getStringExtra("title");
            String text = intent.getStringExtra("text");
            String icon = "";

            try {

                if (title.substring(0, 1).matches("#")) {

                    title = title.substring(1, title.length());

                }

            }catch (Exception e){

            }

            if (pack.matches("com.whatsapp")) {

                icon = "wpp";

            }else if (pack.matches("com.facebook.katana")){

                icon = "fb";

            }else if (pack.matches("com.facebook.orca")){

                icon = "fbmsg";

            }else if (pack.matches("com.instagram.android")){

                icon = "insta";

            }else if (pack.matches("com.snapchat.android")){

                icon = "snap";

            }else if (pack.matches("com.tozelabs.tvshowtime")){

                icon = "tvshow";

            }else if (pack.matches("com.org.telegram.messenger")){

                icon = "telegram";

            }else if (pack.matches("com.twitter.android")){

                icon = "twitter";

            }else if (pack.matches("com.tumblr")){

                icon = "tumblr";

            }else if (pack.matches("com.skype.raider")){

                icon = "skype";

            }else if (pack.matches("com.quoord.tapatalkpro.activity")){

                icon = "tapatalk";

            }else if (pack.matches("co.vine.android")){

                icon = "vine";

            }

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            boolean service = preferences.getBoolean("service", true);

            if (service) {

                executeGet(title, text, icon);

            }

        }
    };

    private void executeGet(final String notif1, final String notif2, final String notificon) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String ip = preferences.getString("ip", "");

        final String geturl;
        geturl = "http://" + ip + "/?parm=con&" + "notif1=" + replace(notif1) + "&notif2=" + replace(notif2) + "&icon=" + notificon ;

        new Thread(new Runnable() {
            @Override
            public void run() {

                String response = null;
                try {
                    response = CustomHttpClient.executeHttpGet(geturl);
                    String res = response.toString();
                    Log.i("RESPONSE", res);
                    //String[] get_part = res.split(";");

                    //example = get_part[0];

                } catch (Exception e) {
                    //internet error
                    Log.i("ERROR", e.toString());
                }

            }
        }).start();

    }

    public String replace(String str) {
        String[] words = str.split(" ");
        StringBuilder sentence = new StringBuilder(words[0]);

        for (int i = 1; i < words.length; ++i) {
            sentence.append("%20");
            sentence.append(words[i]);
        }

        return sentence.toString();
    }

}