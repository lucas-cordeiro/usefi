package loureiro.enzo.usefi.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import loureiro.enzo.usefi.service.ExcluirTodoWifiService;

public class ExcluirTodosWifiBroadcast extends BroadcastReceiver {


    public static final int REQUEST_CODE = 33333333;
    public static final String ACTION = "loureiro.enzo.usefi.service.ExcluirTodosWifiBroadcast";
    private static final String TAG = "TODOS_WIFI_EXCLUIR";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive");
        Intent service = new Intent(context, ExcluirTodoWifiService.class);
        context.startService(service);
    }
}
