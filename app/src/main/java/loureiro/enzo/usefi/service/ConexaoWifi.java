package loureiro.enzo.usefi.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;

import loureiro.enzo.usefi.R;
import loureiro.enzo.usefi.activity.UsuarioActivity;
import loureiro.enzo.usefi.broadcast.ConexaoWifiBroadcast;
import loureiro.enzo.usefi.broadcast.ExcluirWifiBroadcast;
import loureiro.enzo.usefi.broadcast.VerificarWifiOff;
import loureiro.enzo.usefi.helper.Others;
import loureiro.enzo.usefi.model.Comerciante;
import loureiro.enzo.usefi.model.Usuario;
import loureiro.enzo.usefi.model.Wifi;
import loureiro.enzo.usefi.util.AlarmUtil;
import loureiro.enzo.usefi.util.DateUtil;
import loureiro.enzo.usefi.util.EquipeUseFiUtil;
import loureiro.enzo.usefi.util.NotificacaoUtil;
import loureiro.enzo.usefi.util.SharedPreferencesUtil;

public class ConexaoWifi extends IntentService {

    public static final String TAG = "CONEXAO_WIFI", COUNT = "COUNT";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public ConexaoWifi() {
        super(TAG);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.i(TAG, "onHandleIntent");

        new WorkerThreadConexaoWifi(getApplicationContext()).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    class WorkerThreadConexaoWifi extends Thread {

        private Context context;

        public WorkerThreadConexaoWifi(Context context) {
            this.context = context;
        }

        @Override
        public void run() {
            try {
                if(SharedPreferencesUtil.contemValor(context, EquipeUseFiUtil.CONTA) && SharedPreferencesUtil.lerValor(context, EquipeUseFiUtil.CONTA).equals(Usuario.USUARIOS)) {
                    if (SharedPreferencesUtil.lerValor(context, VerificarWifiOff.CONEXAO).equals(VerificarWifiOff.CONECTANDO))
                        SharedPreferencesUtil.gravarValor(context, VerificarWifiOff.CONEXAO, VerificarWifiOff.DESCONECTADO);

                    final String nome = SharedPreferencesUtil.lerValor(context, Wifi.WIFI_NOME);
                    final String bssid = SharedPreferencesUtil.lerValor(context, Wifi.WIFI_BSSID);

                    if (Others.verificaRedeWifi(context, nome, bssid)) {

                        String idComerciante = SharedPreferencesUtil.lerValor(context, Wifi.WIFI_ID_COMERCIANTE);
                        String idUsuario = SharedPreferencesUtil.lerValor(context, Usuario.ID);
                        int count = SharedPreferencesUtil.lerValorInt(context, COUNT);

                        if (count == -1)
                            count = 0;

                        Log.i(TAG, "Executando... " + count);

                        HashMap<String, Object> usuarioMap = new HashMap<>();
                        //usuarioMap.put("tempoDeConexao", count);
                        usuarioMap.put("ultimaConexao", DateUtil.dataDateToDataCompleta(Others.getNTPDate()));

                        FirebaseApp.initializeApp(context);
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Comerciante.COMERCIANTES).child(idComerciante).child(Usuario.USUARIOS).child(idUsuario);
                        reference.updateChildren(usuarioMap);

                        count++;
                        SharedPreferencesUtil.gravarValor(context, COUNT, count);

                        Calendar c = Calendar.getInstance();
                        c.setTimeInMillis(System.currentTimeMillis());
                        c.add(Calendar.MINUTE, EquipeUseFiUtil.TEMPO_CONEXAO_WIFI);
                        long time = c.getTimeInMillis();

                        Intent broadcast = new Intent(context, ConexaoWifiBroadcast.class);
                        AlarmUtil.schedule(context, broadcast, time, ConexaoWifiBroadcast.REQUEST_CODE);

                    } else {

                        Log.i(TAG, "ExcluirWifiService... ");

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(System.currentTimeMillis());
                        calendar.add(Calendar.SECOND, 5);

                        Intent broadcast = new Intent(context, ExcluirWifiBroadcast.class);
                        AlarmUtil.schedule(context, broadcast, calendar.getTimeInMillis(), ExcluirWifiBroadcast.REQUEST_CODE);

                        broadcast = new Intent(context, ConexaoWifiBroadcast.class);
                        AlarmUtil.cancel(context, broadcast, ConexaoWifiBroadcast.REQUEST_CODE);
                    }
                }else{
                    Log.i(TAG, "ExcluirWifiService... ");

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    calendar.add(Calendar.SECOND, 5);

                    Intent broadcast = new Intent(context, ExcluirWifiBroadcast.class);
                    AlarmUtil.schedule(context, broadcast, calendar.getTimeInMillis(), ExcluirWifiBroadcast.REQUEST_CODE);

                    broadcast = new Intent(context, ConexaoWifiBroadcast.class);
                    AlarmUtil.cancel(context, broadcast, ConexaoWifiBroadcast.REQUEST_CODE);
                }
            } catch (Exception e) {
                Intent notificationIntent = new Intent(context, UsuarioActivity.class);
                NotificacaoUtil.create(context, 2, notificationIntent, R.drawable.ic_notificacao_preto_24dp, "UseFi", "Error: " + e.getMessage());
                SharedPreferencesUtil.gravarValor(context, UsuarioActivity.ERROR, e.getMessage());
            }

        }
    }

}
