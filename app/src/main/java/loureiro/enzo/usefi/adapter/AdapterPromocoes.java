package loureiro.enzo.usefi.adapter;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

import loureiro.enzo.usefi.model.Notificacao;
import loureiro.enzo.usefi.model.Usuario;
import loureiro.enzo.usefi.R;
import loureiro.enzo.usefi.util.AnimacaoUtil;
import loureiro.enzo.usefi.util.Base64Util;
import loureiro.enzo.usefi.util.ConfiguracaoFirebase;
import loureiro.enzo.usefi.util.FirebaseUtil;
import loureiro.enzo.usefi.util.ToastUtil;

public class AdapterPromocoes extends RecyclerView.Adapter<AdapterPromocoes.MyViewHolder>{

    List<Notificacao> notificacaos = new ArrayList<>();
    private Activity activity;

    public AdapterPromocoes(List<Notificacao> notificacaos, Activity activity) {
        this.notificacaos = notificacaos;
        this.activity = activity;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_promocoes, parent, false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        final Notificacao notificacao = notificacaos.get(position);
        holder.txtTitle.setText(notificacao.getTitle());
        holder.txtMensagem.setText(notificacao.getMensagem());
        holder.imgExcluir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnimacaoUtil.progressBar(holder.progressBar, holder.imgNotificacao, true);
                excluirNotificacao(notificacao, holder.progressBar, holder.imgNotificacao);
            }
        });
        holder.imgCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnimacaoUtil.progressBar(holder.progressBar, holder.imgNotificacao, true);
                FirebaseMessaging.getInstance().unsubscribeFromTopic(notificacao.getComercianteId()).addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(!task.isSuccessful()){
                            AnimacaoUtil.progressBar(holder.progressBar, holder.imgNotificacao, false);
                            ToastUtil.toastCustom(activity, "Falha: "+task.getException().getMessage(), false);
                        }else{
                            FirebaseAuth firebaseAuth = ConfiguracaoFirebase.getFirebaseAuth();
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            String id = Base64Util.dadoToBase64(user.getEmail());
                            DatabaseReference referenceTopico = ConfiguracaoFirebase.getReferenceFirebase().child(Usuario.USUARIOS).child(id).child(Usuario.TOPICOS).child(notificacao.getComercianteId());
                            referenceTopico.removeValue().addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(!task.isSuccessful()){
                                        AnimacaoUtil.progressBar(holder.progressBar, holder.imgNotificacao, false);
                                        ToastUtil.toastCustom(activity, "Falha: "+task.getException().getMessage(), false);
                                    }else{
                                        excluirNotificacao(notificacao, holder.progressBar, holder.imgNotificacao);
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });
        FirebaseUtil.recuperarImagemStorage(activity, Uri.parse(notificacao.getUrlImagen()), holder.progressBar, holder.imgNotificacao, holder.imgNotificacao);
    }

    private void excluirNotificacao(final Notificacao notificacao, final ProgressBar progressBar, final View view){
        FirebaseAuth firebaseAuth = ConfiguracaoFirebase.getFirebaseAuth();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        final String id = Base64Util.dadoToBase64(user.getEmail());
        DatabaseReference referenceNotificacao = ConfiguracaoFirebase.getReferenceFirebase().child(Usuario.USUARIOS).child(id).child(Usuario.NOTIFICACOES).child(notificacao.getId());
        referenceNotificacao.removeValue().addOnCompleteListener(activity, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(!task.isSuccessful()){
                    ToastUtil.toastCustom(activity, "Falha: "+task.getException().getMessage(), false);
                }else{
                    DatabaseReference referenceRedes = ConfiguracaoFirebase.getReferenceFirebase().child(Usuario.USUARIOS).child(id).child(Usuario.REDES_BAIXADAS).child(notificacao.getComercianteId());
                    referenceRedes.removeValue().addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            ToastUtil.toastCustom(activity, "Falha: "+e.getMessage(), false);
                        }
                    });
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificacaos.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        ImageView imgNotificacao;
        ProgressBar progressBar;
        TextView txtTitle, txtMensagem;
        ImageButton imgExcluir, imgCancelar;

        public MyViewHolder(View itemView) {
            super(itemView);

            imgNotificacao = itemView.findViewById(R.id.imgNotificacaoAdapter);
            progressBar = itemView.findViewById(R.id.progressBarNotificacaoAdapter);
            txtTitle = itemView.findViewById(R.id.txtTitleNotificacaoAdapter);
            txtMensagem = itemView.findViewById(R.id.txtMensagemNotificacaoAdapter);
            imgExcluir = itemView.findViewById(R.id.imgButtonExcluirNotificacaoAdapter);
            imgCancelar = itemView.findViewById(R.id.imgButtonCancelarNotificacaoAdapter);
        }
    }
}
