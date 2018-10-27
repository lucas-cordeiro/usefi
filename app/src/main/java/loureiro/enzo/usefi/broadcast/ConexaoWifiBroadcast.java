package loureiro.enzo.usefi.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import loureiro.enzo.usefi.service.ConexaoWifi;

public class ConexaoWifiBroadcast extends BroadcastReceiver {

    public static final int REQUEST_CODE = 11111111;
    public static final String ACTION = "loureiro.enzo.usefi.service.ConexaoWifiBroadcast";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, ConexaoWifi.class);
        context.startService(service);
    }
}
