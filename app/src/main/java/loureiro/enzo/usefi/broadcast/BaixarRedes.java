package loureiro.enzo.usefi.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import loureiro.enzo.usefi.model.Comerciante;
import loureiro.enzo.usefi.model.Wifi;
import loureiro.enzo.usefi.util.BancoDadosUtil;
import loureiro.enzo.usefi.util.ConfiguracaoFirebase;
import loureiro.enzo.usefi.util.ToastUtil;

public class BaixarRedes extends BroadcastReceiver {

    public static final String ACTION = "BAIXAR_REDES";
    private static final String TAG = "BAIXAR_REDES";

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.i(TAG, "onReceive");

        FirebaseApp.initializeApp(context);
        final String[] comerciantes = intent.getStringArrayExtra(Comerciante.COMERCIANTES);

    }
}
