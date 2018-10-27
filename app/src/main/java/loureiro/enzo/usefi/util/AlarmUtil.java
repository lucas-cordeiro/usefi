package loureiro.enzo.usefi.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.AlarmClock;
import android.util.Log;

import loureiro.enzo.usefi.R;
import loureiro.enzo.usefi.activity.UsuarioActivity;
import loureiro.enzo.usefi.model.Usuario;

public class AlarmUtil {

    private static final String TAG = "ALARM_UTIL";;

    // Agenda o alarme
    public static void schedule(Context context, Intent intent, long triggerAtMillis, int id) {
        //PendingIntent ptAlarmClock=PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent p = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarme = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(triggerAtMillis, p);
        try {
            //alarme.setAlarmClock(alarmClockInfo, p);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarme.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, p);
            }else {
                alarme.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, p);
            }
        }
        catch (Exception e){
            Intent notificationIntent = new Intent(context, UsuarioActivity.class);
            NotificacaoUtil.create(context, 2, notificationIntent, R.drawable.ic_notificacao_preto_24dp, "UseFi", "Error: " + e.getMessage());
            SharedPreferencesUtil.gravarValor(context, UsuarioActivity.ERROR, e.getMessage());
        }
        Log.d(TAG, "Alarme agendado com sucesso.");
    }

    public static void cancel(Context context, Intent intent, int id) {
       // Intent notificationIntent = new Intent(context, UsuarioActivity.class);
       // NotificacaoUtil.create(context, 6, notificationIntent, R.drawable.ic_notificacao_preto_24dp, "UseFi", "Cancelando Wi-fi");
        AlarmManager alarme = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent p = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarme.cancel(p);
        Log.d(TAG, "Alarme cancelado com sucesso.");
    }
}
