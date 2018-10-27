package loureiro.enzo.usefi.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class AlertaUtil {

    public static void alertaErro(String title, String msg, Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void alertaSolicitarNovaSenha(final Activity activity, final String login){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Recuperar Senha");
        builder.setMessage("Esqueceu sua senha? Será enviado uma solicitação para o e-mail cadastrado em sua conta.");
        builder.setCancelable(false);
        builder.setPositiveButton("Recuperar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseAuth firebaseAuth = ConfiguracaoFirebase.getFirebaseAuth();

                firebaseAuth.sendPasswordResetEmail(login)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful())//Sucesso ao Enviar
                                    ToastUtil.toastCustom(activity, "Solicitação Enviada!", true);

                                else//Erro
                                    ToastUtil.toastCustom(activity, "Erro: "+task.getException().getMessage(), false);
                            }
                        });
            }
        });
        builder.setNegativeButton("Cancelar", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void solicitarAtivacaoWifi(final Activity activity) {
        android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(activity);
        alertDialog.setTitle("GPS desativado!");
        alertDialog.setMessage("Ativar GPS?");
        alertDialog.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                activity.startActivity(intent);
            }
        });

        alertDialog.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }
}
