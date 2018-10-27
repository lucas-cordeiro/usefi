package loureiro.enzo.usefi.adapter;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.joda.time.LocalDate;
import org.joda.time.Years;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import loureiro.enzo.usefi.R;
import loureiro.enzo.usefi.model.Cliente;
import loureiro.enzo.usefi.util.AnimacaoUtil;
import loureiro.enzo.usefi.util.DateUtil;
import loureiro.enzo.usefi.util.EquipeUseFiUtil;
import loureiro.enzo.usefi.util.FirebaseUtil;

public class AdapterHistorico extends RecyclerView.Adapter<AdapterHistorico.MyViewHolder>{

    List<Cliente> clientes = new ArrayList<>();
    private Date dateAtual;
    private Activity activity;

    private static final String TAG = "ADAPTER_HISTORICO";

    public AdapterHistorico(List<Cliente> clientes, Date dateAtual, Activity activity) {
        this.clientes = clientes;
        this.dateAtual = dateAtual;
        this.activity = activity;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_historico, parent, false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        new Thread(){
            @Override
            public void run() {
                final Cliente cliente = clientes.get(position);

                final int color;
                if(cliente.getGenero().equals(Cliente.MASCULINO))
                    color = activity.getResources().getColor(R.color.color3);
                else if(cliente.getGenero().equals(Cliente.FEMINO))
                    color = activity.getResources().getColor(R.color.color2);
                else
                    color = activity.getResources().getColor(R.color.color1);
                final Date dateUltimaVisita = DateUtil.dataCompletaToDate(cliente.getUltimaVisita());
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(dateUltimaVisita);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                String horario = "";
                if(hour<10)
                    horario+="0"+hour;
                else
                    horario+=hour;

                if(minute<10)
                    horario+=":0"+minute;
                else
                    horario+= ":"+minute;

                final Date dateUltimaConexao =  DateUtil.dataCompletaToDate(cliente.getUltimaConexao());

                final String finalHorario = horario;

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        FirebaseUtil.recuperarImagemStorage(activity, Uri.parse(cliente.getImgUrl()),holder.progressBar, holder.imgPerfil, holder.imgPerfil);

                        String nome = cliente.getNome();
                        holder.txtNome.setText(nome);
                        holder.txtNome.setTextColor(color);


                        holder.txtUltimoAcesso.setText("Dia: "+DateUtil.dataDateToDataNormal(dateUltimaVisita).substring(0,5));
                        holder.txtHorario.setText("Horário: "+finalHorario);
                        long diferenca = dateAtual.getTime() - dateUltimaConexao.getTime();
                        Log.i(TAG, "Diferenca: "+diferenca+"\n Date: "+dateAtual.toString()+" | UltimaConexao: "+ dateUltimaConexao.toString());
                        int color = activity.getResources().getColor(R.color.colorAzul);
                        holder.imgConectado.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_conectado));
                        if(diferenca >= (DateUtil.ONE_MINUTE_IN_MILLIS * (EquipeUseFiUtil.TEMPO_CONEXAO_WIFI))+DateUtil.ONE_MINUTE_IN_MILLIS/2) {
                            holder.imgConectado.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_desconectado));
                            color = activity.getResources().getColor(R.color.color3);
                        }
                        holder.imgConectado.startAnimation(AnimacaoUtil.mostrarView(500));
                        holder.txtConectado.setTextColor(color);
                    }
                });
            }
        }.start();

    }

    public void setDateAtual(Date dateAtual) {
        this.dateAtual = dateAtual;
    }

    @Override
    public int getItemCount() {
        return clientes.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        ImageView imgPerfil, imgConectado;
        ProgressBar progressBar;
        TextView txtNome;
        TextView txtUltimoAcesso;
        TextView txtHorario;
        TextView txtConectado;

        public MyViewHolder(View itemView) {
            super(itemView);

            imgPerfil = itemView.findViewById(R.id.fotoCliente);
            imgConectado = itemView.findViewById(R.id.imgConectado);
            progressBar = itemView.findViewById(R.id.progressBarFotoCliente);
            txtNome = itemView.findViewById(R.id.txtNomeCliente);
            txtUltimoAcesso = itemView.findViewById(R.id.txtUltimoAcessoCliente);
            txtHorario = itemView.findViewById(R.id.txtHorarioCliente);
            txtConectado = itemView.findViewById(R.id.txtConectado);
        }
    }


}
