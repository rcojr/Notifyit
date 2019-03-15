package com.jrapps.notifyit;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import net.sourceforge.zbar.Symbol;

public class MainActivity extends AppCompatActivity {

    Button btnQR;
    Switch switchNotif;

    Boolean notifEnabled = false;
    Boolean onResumeNotifCheck = false;
    Boolean pref_switch = false;
    Boolean first_run = false;

    static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    private static final int ZBAR_SCANNER_REQUEST = 0;
    private static final int ZBAR_QR_SCANNER_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btnQR = (Button)findViewById(R.id.btnQR);
        switchNotif = (Switch)findViewById(R.id.switchNotif);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        pref_switch = preferences.getBoolean("switch", false);
        first_run = preferences.getBoolean("first_run", true);

        if (first_run){
            Intent i = new Intent(MainActivity.this, SetupWizardActivity.class);
            startActivity(i);
            finish();
        }

        switchNotif.setChecked(pref_switch);

        requestNotifPermission();

        switchNotif.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if (isChecked) {

                        ComponentName cn = new ComponentName(getApplicationContext(), NotificationService.class);
                        String flat = Settings.Secure.getString(getApplicationContext().getContentResolver(), "enabled_notification_listeners");
                        final boolean enabled = flat != null && flat.contains(cn.flattenToString());

                        if (enabled) {

                            notifEnabled = true;

                            switchPrefSave(true);

                        }else {

                            onResumeNotifCheck = true;
                            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                            alertDialog.setTitle(getResources().getString(R.string.app_name));
                            alertDialog.setMessage(getResources().getString(R.string.strHabilitarNotificacoes));
                            alertDialog.setIcon(R.mipmap.ic_launcher);
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();

                                            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                                            startActivity(intent);

                                        }
                                    });
                            alertDialog.show();

                        }

                    launchService(true);

                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    String ip = preferences.getString("ip", "");
                    if (ip.matches("")){
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.strPleaseScanQrCode), Toast.LENGTH_SHORT).show();
                        switchNotif.setChecked(false);
                    }

                } else {

                    notifEnabled = false;

                    switchPrefSave(false);

                    launchService(false);

                }

            }
        });

        btnQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            MY_PERMISSIONS_REQUEST_CAMERA);

                } else {

                    launchQRScanner(v);

                }

            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String ip = preferences.getString("ip", "");

                if (ip.matches("")) {

                    Snackbar.make(view, getResources().getString(R.string.strNoDevice), Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();

                }else {

                    executeGet(getResources().getString(R.string.app_name), getResources().getString(R.string.strNotificacaoTeste));

                    Snackbar.make(view, getResources().getString(R.string.strNotificationTesting), Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();

                }


            }
        });
    }

    private void launchService(boolean serviceStatus) {

        if (serviceStatus) {

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("service", true);
            editor.apply();

            Intent intent = new Intent(MainActivity.this, service.class);
            startService(intent);

        }else {

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("service", false);
            editor.apply();

            Intent intent = new Intent(MainActivity.this, service.class);
            stopService(intent);
            startService(intent);

        }

    }

    private void requestNotifPermission() {

        ComponentName cn = new ComponentName(getApplicationContext(), NotificationService.class);
        String flat = Settings.Secure.getString(getApplicationContext().getContentResolver(), "enabled_notification_listeners");
        final boolean enabled = flat != null && flat.contains(cn.flattenToString());

        if (enabled) {

            notifEnabled = true;

            switchPrefSave(true);

        }else {

            onResumeNotifCheck = true;
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle(getResources().getString(R.string.app_name));
            alertDialog.setMessage(getResources().getString(R.string.strHabilitarNotificacoes));
            alertDialog.setIcon(R.mipmap.ic_launcher);
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                            startActivity(intent);

                        }
                    });
            alertDialog.show();

        }

    }

    private void switchPrefSave(boolean b) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("switch", b);
        editor.apply();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {

            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    btnQR.performClick();

                } else {

                    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                    alertDialog.setTitle(getResources().getString(R.string.app_name));
                    alertDialog.setMessage(getResources().getString(R.string.strPermissionCameraDenied));
                    alertDialog.setIcon(R.mipmap.ic_launcher);
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    requestCameraPermission();
                                }
                            });
                    alertDialog.show();

                }
                return;
            }

        }
    }

    private void requestCameraPermission() {

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);

        }

    }

    public void launchQRScanner(View v) {
        if (isCameraAvailable()) {
            Intent intent = new Intent(this, ZBarScannerActivity.class);
            intent.putExtra(ZBarConstants.SCAN_MODES, new int[]{Symbol.QRCODE});
            startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
        } else {
            Toast.makeText(this, "Camera Error", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isCameraAvailable() {
        PackageManager pm = getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ZBAR_SCANNER_REQUEST:
            case ZBAR_QR_SCANNER_REQUEST:
                if (resultCode == RESULT_OK) {

                    String qrcode = data.getStringExtra(ZBarConstants.SCAN_RESULT);

                    try {

                        if (qrcode.substring(qrcode.length() - 5, qrcode.length()).toString().matches(":8097")) {

                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("ip", qrcode);
                            editor.apply();

                            executeGet(getResources().getString(R.string.app_name), getResources().getString(R.string.strConectado) + " " + android.os.Build.MANUFACTURER + " " + Build.MODEL);

                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.strConectadoPC), Toast.LENGTH_SHORT).show();

                        } else {

                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.strQrCodeError), Toast.LENGTH_SHORT).show();

                        }

                    } catch (Exception e) {

                    }


                } else if(resultCode == RESULT_CANCELED && data != null) {
                    String error = data.getStringExtra(ZBarConstants.ERROR_INFO);
                    if(!TextUtils.isEmpty(error)) {
                        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    private void executeGet(final String notif1, final String notif2) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String ip = preferences.getString("ip", "");

        final String geturl;
        geturl = "http://" + ip + "/?parm=con&" + "notif1=" + replace(notif1) + "&notif2=" + replace(notif2);

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

    @Override
    public void onResume()
    {
        super.onResume();

        if (onResumeNotifCheck){

            ComponentName cn = new ComponentName(getApplicationContext(), NotificationService.class);
            String flat = Settings.Secure.getString(getApplicationContext().getContentResolver(), "enabled_notification_listeners");
            final boolean enabled = flat != null && flat.contains(cn.flattenToString());

            if (enabled) {

                notifEnabled = true;
                switchPrefSave(true);

            }else {

                notifEnabled = false;
                switchNotif.setChecked(false);

            }

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.help) {

            Intent i = new Intent(MainActivity.this, SetupWizardActivity.class);
            startActivity(i);

            return true;
        }

        if (id == R.id.about) {

            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle(getResources().getString(R.string.app_name));
            alertDialog.setMessage(getResources().getString(R.string.strSobreDialog));
            alertDialog.setIcon(R.mipmap.ic_launcher);
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
