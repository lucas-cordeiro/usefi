package loureiro.enzo.usefi.service;

import android.app.IntentService;
import android.app.Service;
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
import loureiro.enzo.usefi.broadcast.ConexaoWifiBroadcast;
import loureiro.enzo.usefi.broadcast.ExcluirWifiBroadcast;
import loureiro.enzo.usefi.fragment.CategoriasFragment;
import loureiro.enzo.usefi.model.Usuario;
import loureiro.enzo.usefi.model.Wifi;
import loureiro.enzo.usefi.util.AlarmUtil;
import loureiro.enzo.usefi.util.BancoDadosUtil;
import loureiro.enzo.usefi.util.NotificacaoUtil;
import loureiro.enzo.usefi.util.SharedPreferencesUtil;

public class ExcluirWifiService extends IntentService {

    private final static String TAG = "CONEXAO_WIFI_EXCLUIR";
    public final static String SCANNER_VERIFICACAO = "SCANNER_VERIFICACAO";
    private WifiManager wifiManager;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public ExcluirWifiService() {
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
        new WorkerThreadExcluirWifi(context).start();

    }


    class WorkerThreadExcluirWifi extends Thread{

        private Context context;
        public WorkerThreadExcluirWifi(Context context) {
            this.context = context;
        }


        @Override
        public void run() {
            try {
                SharedPreferencesUtil.gravarValor(context, ConexaoWifi.COUNT, -1);
                wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

                if (!wifiManager.isWifiEnabled())
                {
                    wifiManager.setWifiEnabled(true);
                }

                Intent broadcast = new Intent(getApplicationContext(), ExcluirWifiBroadcast.class);
                AlarmUtil.cancel(getApplicationContext().getApplicationContext(), broadcast, ExcluirWifiBroadcast.REQUEST_CODE);

                SharedPreferencesUtil.gravarValor(getApplicationContext(), ConexaoWifi.COUNT, -1);
                wifiManager.startScan();

                registerReceiver(new ExcluirWifiBroadCastService(), new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

            } catch (Exception e) {
                Log.i(TAG, e.getMessage());

                Intent notificationIntent = new Intent(context, UsuarioActivity.class);
                notificationIntent.putExtra(UsuarioActivity.ERROR, e.getMessage());
                NotificacaoUtil.create(context, 13, notificationIntent, R.drawable.ic_notificacao_preto_24dp, "UseFi", "Error:: " + e.getMessage());

            }
        }
    }

    public class ExcluirWifiBroadCastService extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

            try{
                Log.i(TAG, "onReceive");
                wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                wifiManager.getScanResults();
                List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                //List<Wifi> wifisCadastrados = BancoDadosUtil.lerWifis(context);
                String nome = SharedPreferencesUtil.lerValor(context, Wifi.WIFI_NOME);

                if(!nome.contains("\""))
                    nome = "\""+nome+"\"";

                //for(Wifi wifi : wifisCadastrados){
                    for(WifiConfiguration i : list){
                        if(i.SSID!=null && i.SSID.equals(nome)) {
                            wifiManager.removeNetwork(i.networkId);
                        }
                    }
                //}
                Intent notificationIntent = new Intent(context, UsuarioActivity.class);
                NotificacaoUtil.create(context, 13, notificationIntent, R.drawable.ic_notificacao_preto_24dp, "UseFi", "Desconectado da Rede: "+nome.replace("\"",""));

                unregisterReceiver(this);

            }catch (Exception e){

                Log.i(TAG, e.getMessage());
            }
        }
    }

}
