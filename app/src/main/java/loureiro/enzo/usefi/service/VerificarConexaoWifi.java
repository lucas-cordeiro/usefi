package loureiro.enzo.usefi.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.Calendar;
import java.util.List;

import loureiro.enzo.usefi.R;
import loureiro.enzo.usefi.activity.UsuarioActivity;
import loureiro.enzo.usefi.broadcast.ConexaoWifiBroadcast;
import loureiro.enzo.usefi.broadcast.ExcluirTodosWifiBroadcast;
import loureiro.enzo.usefi.helper.Others;
import loureiro.enzo.usefi.model.Wifi;
import loureiro.enzo.usefi.util.AlarmUtil;
import loureiro.enzo.usefi.util.AlertaUtil;
import loureiro.enzo.usefi.util.BancoDadosUtil;
import loureiro.enzo.usefi.util.NotificacaoUtil;
import loureiro.enzo.usefi.util.SharedPreferencesUtil;

public class VerificarConexaoWifi extends JobService {

    private static final String TAG = "VERIFICAR_CONEXAO";
    private WifiManager wifiManager;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(TAG, "onStartJob");

        Context context = getApplicationContext();

        Intent broadcast = new Intent(context, ConexaoWifiBroadcast.class);
        AlarmUtil.cancel(context, broadcast, ConexaoWifiBroadcast.REQUEST_CODE);
       if(Others.isOnline(context)){
           Log.i(TAG, "Online");
           Calendar calendar = Calendar.getInstance();
           calendar.setTimeInMillis(System.currentTimeMillis());
           calendar.add(Calendar.SECOND, 5);
           AlarmUtil.schedule(context, broadcast, calendar.getTimeInMillis(),ConexaoWifiBroadcast.REQUEST_CODE);
       }else{
           Log.i(TAG, "Offline");
           new VerificarConexaoWifi.WorkerThreadExcluirTodosWifi(context).start();
       }

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i(TAG, "onStopJob");
        return false;
    }


    class WorkerThreadExcluirTodosWifi extends Thread {

        private Context context;

        public WorkerThreadExcluirTodosWifi(Context context) {
            this.context = context;
        }


        @Override
        public void run() {
            try {
                Log.i(TAG, "run()");
                SharedPreferencesUtil.gravarValor(context, ConexaoWifi.COUNT, -1);
                wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

                if (!wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(true);
                }

                Intent broadcast = new Intent(getApplicationContext(), ExcluirTodosWifiBroadcast.class);
                AlarmUtil.cancel(getApplicationContext().getApplicationContext(), broadcast, ExcluirTodosWifiBroadcast.REQUEST_CODE);

                SharedPreferencesUtil.gravarValor(getApplicationContext(), ConexaoWifi.COUNT, -1);
                wifiManager.startScan();

                registerReceiver(new VerificarConexaoWifi.ExcluirTodosWifiBroadCastService(), new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

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
                            wifiManager.removeNetwork(i.networkId);
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
