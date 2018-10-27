package loureiro.enzo.usefi.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

import loureiro.enzo.usefi.model.Wifi;
import loureiro.enzo.usefi.service.ConexaoWifi;
import loureiro.enzo.usefi.service.ExcluirTodoWifiService;
import loureiro.enzo.usefi.util.AlarmUtil;
import loureiro.enzo.usefi.util.BancoDadosUtil;
import loureiro.enzo.usefi.util.SharedPreferencesUtil;

public class VerificarWifiOff extends BroadcastReceiver {

    public static final String CONEXAO = "CONEXAO", CONECTANDO = "CONECTANDO", DESCONECTADO = "DESCONECTADO";
    private static final String TAG = "VERIFICAR_WIFI_OFF";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals (intent.getAction())) {
            String conexao = SharedPreferencesUtil.lerValor(context, CONEXAO);
            Log.i(TAG, "onReceive " + conexao);

            Intent broadcast = new Intent(context, ConexaoWifiBroadcast.class);
            AlarmUtil.cancel(context, broadcast, ConexaoWifiBroadcast.REQUEST_CODE);

            if (conexao.equals(DESCONECTADO)) {
                Intent service = new Intent(context, ExcluirTodoWifiService.class);
                context.startService(service);
            }
        }
    }
}
