package loureiro.enzo.usefi.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import loureiro.enzo.usefi.R;
import loureiro.enzo.usefi.activity.UsuarioActivity;
import loureiro.enzo.usefi.service.ExcluirWifiService;
import loureiro.enzo.usefi.util.NotificacaoUtil;

public class ExcluirWifiBroadcast extends BroadcastReceiver {

    public static final int REQUEST_CODE = 22222222;
    public static final String ACTION = "loureiro.enzo.usefi.service.ExcluirWifiBroadcast";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, ExcluirWifiService.class);
        context.startService(service);
    }
}
