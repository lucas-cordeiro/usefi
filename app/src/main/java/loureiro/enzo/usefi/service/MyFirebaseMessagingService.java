package loureiro.enzo.usefi.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;

import loureiro.enzo.usefi.activity.UsuarioActivity;
import loureiro.enzo.usefi.helper.Others;
import loureiro.enzo.usefi.model.Comerciante;
import loureiro.enzo.usefi.model.Notificacao;
import loureiro.enzo.usefi.model.Usuario;
import loureiro.enzo.usefi.R;
import loureiro.enzo.usefi.util.Base64Util;
import loureiro.enzo.usefi.util.ConfiguracaoFirebase;
import loureiro.enzo.usefi.util.DateUtil;
import loureiro.enzo.usefi.util.EquipeUseFiUtil;
import loureiro.enzo.usefi.util.FirebaseUtil;
import loureiro.enzo.usefi.util.SharedPreferencesUtil;

public class MyFirebaseMessagingService extends FirebaseMessagingService{

    private static final String TAG = "MyFirebaseMsgService";
    public static final int REQUEST_CODE_NOTIFICACAO = 11111, ID_NOTIFICACAO = 22222;
    private Bitmap bitmap;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        Log.d(TAG, "CONTA: "+SharedPreferencesUtil.lerValor(getApplicationContext(), EquipeUseFiUtil.CONTA));
        if(SharedPreferencesUtil.lerValor(getApplicationContext(), EquipeUseFiUtil.CONTA).equals(Usuario.USUARIOS)){

            // Check if message contains a data payload.
            if (remoteMessage.getData().size() > 0) {
                Log.d(TAG, "Message data payload: " + remoteMessage.getData());

                Notificacao notificacao = new Notificacao();
                notificacao.setTitle(remoteMessage.getData().get("title"));
                notificacao.setMensagem(remoteMessage.getData().get("mensagem"));
                notificacao.setUrlImagen(remoteMessage.getData().get("urlImagen"));
                notificacao.setComercianteId(remoteMessage.getData().get("idComerciante"));
                recuperarDadosUsuario(notificacao);

            }

            // Check if message contains a notification payload.
            if (remoteMessage.getNotification() != null) {
                Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            }

            // Also if you intend on generating your own notifications as a result of a received FCM
            // message, here is where that should be initiated. See sendNotification method below.
        }
    }

    private void recuperarDadosUsuario(Notificacao notificacao) {
        String nome = SharedPreferencesUtil.lerValor(getApplicationContext(), Usuario.NOME);
        String idade = DateUtil.dataFirebaseToDataNormal(SharedPreferencesUtil.lerValor(getApplicationContext(), Usuario.IDADE));
        notificacao.setMensagem(notificacao.getMensagem().replace(Usuario.NOME_NOTIFICACAO, nome));
        notificacao.setMensagem(notificacao.getMensagem().replace(Usuario.IDADE_NOTIFICACAO, idade));
        notificacao.setTitle(notificacao.getTitle().replace(Usuario.NOME_NOTIFICACAO, nome));
        notificacao.setTitle(notificacao.getTitle().replace(Usuario.IDADE_NOTIFICACAO, idade));

        recuperarImagem(notificacao);
    }

    private void recuperarImagem(final Notificacao notificacao) {
        new Thread(){
            @Override
            public void run() {
                try {
                    if(!notificacao.getUrlImagen().equals("false")) {
                        bitmap = Others.getBitmapfromUrl(notificacao.getUrlImagen());
                        salvarDadosUsuario(bitmap, notificacao);
                    }
                    else{
                        DatabaseReference referenceComerciante = ConfiguracaoFirebase.getReferenceFirebase().child(Comerciante.COMERCIANTES).child(notificacao.getComercianteId());
                        referenceComerciante.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                                new Thread(){
                                    @Override
                                    public void run() {
                                        try {
                                            Comerciante comerciante = dataSnapshot.getValue(Comerciante.class);
                                            notificacao.setUrlImagen(comerciante.getUrlFotoComercio());
                                            bitmap = Others.getBitmapfromUrl(comerciante.getUrlFotoComercio());
                                            salvarDadosUsuario(bitmap, notificacao);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }.start();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    private void salvarDadosUsuario(final Bitmap bitmap, final Notificacao notificacao){
        Log.d(TAG, "salvarDados");

        if(FirebaseUtil.verificaContaLogada()) {
            String login = ConfiguracaoFirebase.getFirebaseAuth().getCurrentUser().getEmail();
            String id = Base64Util.dadoToBase64(login);
            DatabaseReference referenceUser = ConfiguracaoFirebase.getReferenceFirebase().child(Usuario.USUARIOS).child(id).child(Usuario.NOTIFICACOES).push();
            Log.d(TAG, "reference: "+referenceUser);
            referenceUser.setValue(notificacao).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        criarNotificacao(notificacao, bitmap);
                    }
                }
            });
        }
    }

    private void criarNotificacao(Notificacao notificacao, Bitmap bitmap){
        Intent intent = new Intent(MyFirebaseMessagingService.this, UsuarioActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Comerciante.COMERCIANTES,notificacao.getComercianteId());
        PendingIntent pendingIntent = PendingIntent.getActivity(MyFirebaseMessagingService.this, REQUEST_CODE_NOTIFICACAO, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(MyFirebaseMessagingService.this, channelId)
                        .setSmallIcon(R.drawable.ic_notifications_24dp)
                        .setColor(getResources().getColor(R.color.colorAccent))
                        .setStyle(new NotificationCompat.BigPictureStyle().setSummaryText(notificacao.getMensagem()).bigPicture(bitmap))
                        .setContentTitle(notificacao.getTitle())
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Promoção",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(ID_NOTIFICACAO, notificationBuilder.build());
    }
}
