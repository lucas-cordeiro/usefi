package loureiro.enzo.usefi.adapter;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.joda.time.LocalDate;
import org.joda.time.Years;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import loureiro.enzo.usefi.model.Cliente;
import loureiro.enzo.usefi.R;
import loureiro.enzo.usefi.util.DateUtil;
import loureiro.enzo.usefi.util.FirebaseUtil;

public class AdapterClientes extends RecyclerView.Adapter<AdapterClientes.MyViewHolder>{

    List<Cliente> clientes = new ArrayList<>();
    private Date dateAtual;
    private Activity activity;

    public AdapterClientes(List<Cliente> clientes, Date dateAtual, Activity activity) {
        this.clientes = clientes;
        this.dateAtual = dateAtual;
        this.activity = activity;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_clientes, parent, false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        new Thread(){
            @Override
            public void run() {
                final Cliente cliente = clientes.get(position);
                Date dateCliente = DateUtil.dataNormalToDate(cliente.getIdade());

                LocalDate aniversarioCliente = new LocalDate(dateCliente);
                LocalDate atual = new LocalDate(dateAtual);
                final Years age = Years.yearsBetween(aniversarioCliente, atual);

                final int color;
                if(cliente.getGenero().equals(Cliente.MASCULINO))
                    color = activity.getResources().getColor(R.color.color3);
                else if(cliente.getGenero().equals(Cliente.FEMINO))
                    color = activity.getResources().getColor(R.color.color2);
                else
                    color = activity.getResources().getColor(R.color.color1);

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        FirebaseUtil.recuperarImagemStorage(activity, Uri.parse(cliente.getImgUrl()),holder.progressBar, holder.imgPerfil, holder.imgPerfil);

                        holder.txtNome.setText(cliente.getNome());
                        holder.txtNome.setTextColor(color);
                        holder.txtIdade.setText("Idade: "+String.valueOf(age.getYears())+" anos");
                        holder.txtPlataforma.setText("Plataforma: "+cliente.getPlataforma());
                        holder.txtUltimoAcesso.setText("Última visita: "+DateUtil.dataCompletaToDataNormal(cliente.getUltimaVisita()));
                    }
                });
            }
        }.start();

    }

    @Override
    public int getItemCount() {
        return clientes.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        ImageView imgPerfil;
        ProgressBar progressBar;
        TextView txtNome;
        TextView txtIdade;
        TextView txtPlataforma;
        TextView txtUltimoAcesso;

        public MyViewHolder(View itemView) {
            super(itemView);

            imgPerfil = itemView.findViewById(R.id.fotoCliente);
            progressBar = itemView.findViewById(R.id.progressBarFotoCliente);
            txtNome = itemView.findViewById(R.id.txtNomeCliente);
            txtIdade = itemView.findViewById(R.id.txtIdadeCliente);
            txtPlataforma = itemView.findViewById(R.id.txtPlataformaCliente);
            txtUltimoAcesso = itemView.findViewById(R.id.txtUltimoAcessoCliente);
        }
    }
}
