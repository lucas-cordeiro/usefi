package loureiro.enzo.usefi.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import loureiro.enzo.usefi.fragment.DashboardFragment;
import loureiro.enzo.usefi.util.AlarmUtil;
import loureiro.enzo.usefi.util.DateUtil;
import loureiro.enzo.usefi.util.EquipeUseFiUtil;

public class DataAtualDashboardBroadcast extends BroadcastReceiver {

    public static final int REQUEST_CODE = 55555555;

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.i("DASHBOARD_FRAGMENT", "BROADCAST");
        Intent broadcast = new Intent(context, DataAtualDashboardBroadcast.class);
        AlarmUtil.cancel(context, broadcast, REQUEST_CODE);

        broadcast = new Intent(DashboardFragment.TAG);
        context.sendBroadcast(broadcast);
    }
}
