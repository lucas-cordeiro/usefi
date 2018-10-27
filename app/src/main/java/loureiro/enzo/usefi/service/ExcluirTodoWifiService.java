package loureiro.enzo.usefi.service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;

import loureiro.enzo.usefi.R;
import loureiro.enzo.usefi.activity.UsuarioActivity;
import loureiro.enzo.usefi.broadcast.ExcluirTodosWifiBroadcast;
import loureiro.enzo.usefi.model.Wifi;
import loureiro.enzo.usefi.util.AlarmUtil;
import loureiro.enzo.usefi.util.BancoDadosUtil;
import loureiro.enzo.usefi.util.NotificacaoUtil;
import loureiro.enzo.usefi.util.SharedPreferencesUtil;

public class ExcluirTodoWifiService extends IntentService {

    private final static String TAG = "TODOS_WIFI_EXCLUIR";
    public final static String SCANNER_VERIFICACAO = "SCANNER_VERIFICACAO";
    private WifiManager wifiManager;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public ExcluirTodoWifiService() {
        super(TAG);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.i(TAG, " onHandleIntent");
        Context context = getApplicationContext();
        new ExcluirTodoWifiService.WorkerThreadExcluirTodosWifi(context).start();

    }


    class WorkerThreadExcluirTodosWifi extends Thread {

        private Context context;

        public WorkerThreadExcluirTodosWifi(Context context) {
            this.context = context;
        }


        @Override
        public void run() {
            try {
                SharedPreferencesUtil.gravarValor(context, ConexaoWifi.COUNT, -1);
                wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

                if (!wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(true);
                }

                Intent broadcast = new Intent(getApplicationContext(), ExcluirTodosWifiBroadcast.class);
                AlarmUtil.cancel(getApplicationContext().getApplicationContext(), broadcast, ExcluirTodosWifiBroadcast.REQUEST_CODE);

                SharedPreferencesUtil.gravarValor(getApplicationContext(), ConexaoWifi.COUNT, -1);
                wifiManager.startScan();

                registerReceiver(new ExcluirTodoWifiService.ExcluirTodosWifiBroadCastService(), new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

            } catch (Exception e) {
                Log.i(TAG, e.getMessage());

                Intent notificationIntent = new Intent(context, UsuarioActivity.class);
                notificationIntent.putExtra(UsuarioActivity.ERROR, e.getMessage());
                NotificacaoUtil.create(context, 13, notificationIntent, R.drawable.ic_notificacao_preto_24dp, "UseFi", "Error:: " + e.getMessage());

            }
        }
    }

    public class ExcluirTodosWifiBroadCastService extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            try {
                wifiManager.getScanResults();
                List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                List<Wifi> wifisCadastrados = BancoDadosUtil.lerWifis(context);

                for (Wifi wifi : wifisCadastrados) {
                    for (WifiConfiguration i : list) {
                        if (i.SSID != null && i.SSID.equals("\"" + wifi.getNome() + "\"")) {
                            Log.i(TAG, "Remove: "+wifiManager.removeNetwork(i.networkId));

                        }
                    }
                }

                unregisterReceiver(this);

            } catch (Exception e) {

                Log.i(TAG, e.getMessage());
            }
        }
    }
}