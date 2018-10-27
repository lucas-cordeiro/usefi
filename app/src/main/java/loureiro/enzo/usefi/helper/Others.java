package loureiro.enzo.usefi.helper;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.OpenableColumns;
import android.util.Log;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.time.TimeTCPClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import loureiro.enzo.usefi.model.Wifi;
import loureiro.enzo.usefi.util.DateUtil;
import loureiro.enzo.usefi.util.SharedPreferencesUtil;

public class Others {
    public static String getFileName(Uri uri, ContentResolver contentResolver) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    /*
    public static Date getTime(){
        Date date = new Date();
        Log.i("Others_Data", "getTime()");//É mostrado a hora
        try
        {
            TimeTCPClient client = new TimeTCPClient(); //É configurado o cliente para realizar a conexão
            try {
                client.setDefaultTimeout(5000);
                // Conecta ao servidor
                client.connect("time.nist.gov");

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(client.getDate().getTime());//É atribuido ao objeto Calendar a data recolhida

                calendar.set(Calendar.HOUR, calendar.get(Calendar.HOUR)-3);//Altera-se a hora para -3, por causa do fuso horário
                //calendar.set(Calendar.MINUTE, 0);
                //calendar.set(Calendar.SECOND, 0);

                date.setTime(calendar.getTimeInMillis());

                Log.i("Others_Data", "calendar.getTime(): "+String.valueOf(calendar.getTime()));//É mostrado a hora
                return date;
            } finally {
                Log.i("Others_Data", "cliente.getTime(): "+String.valueOf(client.getTime()));//É mostrado a hora
                client.disconnect();
            }

        } catch(Exception e) {
                Log.i("Data Exception", "Error: "+e.getMessage());
            return date;
        }

    }*/

    public static Date getNTPDate() {

        String[] hosts = new String[]{"a.st1.ntp.br",
                "b.st1.ntp.br", "c.st1.ntp.br",
                "d.st1.ntp.br "};

        NTPUDPClient client = new NTPUDPClient();
        // We want to timeout if a response takes longer than 5 seconds
        client.setDefaultTimeout(5000);

        for (String host : hosts) {

            try {
                InetAddress hostAddr = InetAddress.getByName(host);
                Log.i("Others_Data","> " + hostAddr.getHostName() + "/" + hostAddr.getHostAddress());
                TimeInfo info = client.getTime(hostAddr);
                Date date = new Date(info.getReturnTime());
                //date.setTime(date.getTime()-(DateUtil.ONE_HOUR_IN_MILLIS*3));
                Log.i("Others_Data","> " +date.toString());
                return date;

            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        client.close();

        return null;

    }


    public static<TType> void invertUsingCollectionsReverse(List<TType> list ) {
        Collections.reverse(list);
    }

    public static boolean isOnline(Activity activity) {
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isAvailable() && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isAvailable() && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    public static Bitmap getBitmapfromUrl(String imageUrl) throws IOException {

        Bitmap bitmap;
        InputStream in = new URL(imageUrl).openStream();

        bitmap = BitmapFactory.decodeStream(in);
        in.close();

        return bitmap;
    }

    public static double meterDistanceBetweenPoints(double lat_a, double lng_a, double lat_b, double lng_b) {
        double pk = (180.f/Math.PI);

        double a1 = lat_a / pk;
        double a2 = lng_a / pk;
        double b1 = lat_b / pk;
        double b2 = lng_b / pk;

        double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
        double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
        double t3 = Math.sin(a1) * Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);

        return 6366000 * tt;
    }

    public static String getWifiSSID(Context context) {
        if (context == null) {
            return "";
        }
        final Intent intent = context.registerReceiver(
                null, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
        if (intent != null) {
            final WifiInfo wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
            if (wifiInfo != null) {
                final String ssid = wifiInfo.getSSID();
                if (ssid != null) {
                    return ssid;
                }
            }
        }
        return "";
    }

    public static int getWifiNetworkId(Context context) {
        if (context == null) {
            return -1;
        }
        final Intent intent = context.registerReceiver(
                null, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
        if (intent != null) {
            final WifiInfo wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
            if (wifiInfo != null) {
                final int networkId = wifiInfo.getNetworkId();
                if (networkId != -1) {
                    return networkId;
                }
            }
        }
        return -1;
    }

    public static boolean verificaRedeWifi(Context context, Wifi wifi) {

        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {
            final WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            String ssid = connectionInfo.getSSID();
            String bssid = connectionInfo.getBSSID();
            Log.i("RECARREGAR_APP", "SSID: "+ssid+" BSSID: "+bssid+" Wifi:"+wifi.getNome());

            boolean igual = ssid.equals(wifi.getNome());
            if(!igual)
                igual = bssid.equals(wifi.getBssid());
            return igual;
        }else
            return false;


    }

    public static boolean verificaRedeWifi(Context context, String ssidWifi, String bssidWifi) {

        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {
            final WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            String ssid = connectionInfo.getSSID();
            String bssid = connectionInfo.getBSSID();
            Log.i("EXCLUIR_WIFI", "SSID: "+ssid+" BSSID: "+bssid+" Wifi:"+ssid);

            boolean igual = ssid.equals(ssidWifi);
            if(!igual)
                igual = bssid.equals(bssidWifi);
            return igual;
        }else
            return false;


    }

    public static boolean wifiConnection(Context context, String wifiSSID, String password) {
        WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = wifiSSID;
        config.preSharedKey = password;
        int id = wifi.addNetwork(config);
        SharedPreferencesUtil.gravarValor(context, Wifi.ID, String.valueOf(id));
        wifi.saveConfiguration();
        return wifi.enableNetwork(id, true);
    }

}
