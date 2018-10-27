package loureiro.enzo.usefi.service;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.JobService;
import com.google.firebase.database.DatabaseReference;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import loureiro.enzo.usefi.R;
import loureiro.enzo.usefi.activity.UsuarioActivity;
import loureiro.enzo.usefi.broadcast.ConexaoWifiBroadcast;
import loureiro.enzo.usefi.helper.Others;
import loureiro.enzo.usefi.model.Comerciante;
import loureiro.enzo.usefi.model.Usuario;
import loureiro.enzo.usefi.model.Wifi;
import loureiro.enzo.usefi.util.AlarmUtil;
import loureiro.enzo.usefi.util.ConfiguracaoFirebase;
import loureiro.enzo.usefi.util.DateUtil;
import loureiro.enzo.usefi.util.EquipeUseFiUtil;
import loureiro.enzo.usefi.util.NotificacaoUtil;
import loureiro.enzo.usefi.util.SharedPreferencesUtil;

import static loureiro.enzo.usefi.service.ConexaoWifi.COUNT;

public class JobConexaoWifi extends JobService {

    public static final String TAG = "JOB_CONEXAO_WIFI", DATA_ULTIMA_CONEXAO = "DATA_ULTIMA_CONEXAO";
    private AsyncTask backgroundTask;

    @Override
    public boolean onStartJob(com.firebase.jobdispatcher.JobParameters job) {
        Log.i(TAG, "onStartJob");
       if(SharedPreferencesUtil.contemValor(getApplicationContext(), EquipeUseFiUtil.CONTA) && SharedPreferencesUtil.lerValor(getApplicationContext(), EquipeUseFiUtil.CONTA).equals(Usuario.USUARIOS)) {
           new ConexaoWifiAsyncTask().execute();
           return true;
       }else {
           FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(getApplicationContext()));
           dispatcher.cancel(TAG);
           return false;
       }
    }

    @Override
    public boolean onStopJob(com.firebase.jobdispatcher.JobParameters job) {
        Log.i(TAG, "onStopJob");
        return false;
    }


    public class ConexaoWifiAsyncTask extends AsyncTask<String,Integer,Integer> {

        @Override
        protected void onPreExecute() {
            //Codigo
        }

        @Override
        protected Integer doInBackground(String... strings) {
            Context context = getApplicationContext();

            String nome = SharedPreferencesUtil.lerValor(context, Wifi.WIFI_NOME);
            String bssid = SharedPreferencesUtil.lerValor(context, Wifi.WIFI_BSSID);

            if(Others.verificaRedeWifi(context, nome, bssid)) {

                Intent broadcast = new Intent(context, ConexaoWifiBroadcast.class);
                AlarmUtil.cancel(context, broadcast, ConexaoWifiBroadcast.REQUEST_CODE);

                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(System.currentTimeMillis());
                c.add(Calendar.SECOND, 5);
                long time = c.getTimeInMillis();

                broadcast = new Intent(context, ConexaoWifiBroadcast.class);
                AlarmUtil.schedule(context, broadcast, time, ConexaoWifiBroadcast.REQUEST_CODE);

                Date date = Others.getNTPDate();
                String idComerciante = SharedPreferencesUtil.lerValor(context, Wifi.WIFI_ID_COMERCIANTE);
                String idUsuario = SharedPreferencesUtil.lerValor(context, Usuario.ID);
                Date dateUltimaConexao = DateUtil.dataCompletaSegundosToDate(SharedPreferencesUtil.lerValor(context, DATA_ULTIMA_CONEXAO));

                long diferencaDasDatas = date.getTime() - dateUltimaConexao.getTime();
                Log.i(TAG, "Diferença: " + diferencaDasDatas + " " + EquipeUseFiUtil.TEMPO_CONEXAO_LIMITE + " minutos: " + EquipeUseFiUtil.TEMPO_CONEXAO_LIMITE * DateUtil.ONE_MINUTE_IN_MILLIS + "\nDate: " + date.toString() + " | DateUltimaConexao: " + dateUltimaConexao.toString());
                if (diferencaDasDatas >= EquipeUseFiUtil.TEMPO_CONEXAO_LIMITE * DateUtil.ONE_MINUTE_IN_MILLIS || SharedPreferencesUtil.lerValorInt(context, ConexaoWifi.COUNT) == -1) {

                    HashMap<String, Object> usuarioMap = new HashMap<>();
                    usuarioMap.put("ultimaVisita", DateUtil.dataDateToDataCompleta(date));
                    SharedPreferencesUtil.gravarValor(context, DATA_ULTIMA_CONEXAO, DateUtil.dataDateToDataCompletaSegundos(date));

                    final DatabaseReference referenceComerciante = ConfiguracaoFirebase.getReferenceFirebase().child("Comerciantes").child(idComerciante).child("Usuarios").child(idUsuario);
                    referenceComerciante.updateChildren(usuarioMap);

                    Intent notificationIntent = new Intent(context, UsuarioActivity.class);
                    NotificacaoUtil.create(context, 13, notificationIntent, R.drawable.ic_notificacao_preto_24dp, "UseFi", "Conectado na Rede: " + nome.replace("\"", ""));
                }

            }

            return null;
        }

        @Override
        protected void onPostExecute(Integer numero) {
            //Codigo
        }


    }
}
