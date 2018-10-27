package loureiro.enzo.usefi.adapter;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import loureiro.enzo.usefi.helper.Others;
import loureiro.enzo.usefi.model.Comerciante;
import loureiro.enzo.usefi.model.Usuario;
import loureiro.enzo.usefi.R;
import loureiro.enzo.usefi.util.FirebaseUtil;
import loureiro.enzo.usefi.util.ToastUtil;

public class AdapterComerciantes extends RecyclerView.Adapter<AdapterComerciantes.MyViewHolder>{

    private List<Comerciante> comerciantes;
    private Activity activity;
    private Usuario usuario;

    public AdapterComerciantes(List<Comerciante> comerciantes, Activity activity, Usuario usuario) {
        this.comerciantes = comerciantes;
        this.activity = activity;
        this.usuario = usuario;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_comerciantes, parent, false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        Comerciante comerciante = comerciantes.get(position);
        holder.txtNome.setText(comerciante.getNomeLoja());
        holder.txtLocal.setText(comerciante.getLocal());

        double distance = Others.meterDistanceBetweenPoints(usuario.getLatitude(), usuario.getLongitude(), comerciante.getLatitude(), comerciante.getLongitude());
        distance /= 1000;
        String distacia = String.format("%.2f", distance).replace('.',',')/*String.valueOf(distance)*/;
        if(distacia.equals("NaN"))
            distacia = "0.00";
        holder.txtDistancia.setText(distacia+" km");
        FirebaseUtil.recuperarImagemStorage(activity, Uri.parse(comerciante.getUrlLogo()), holder.progressBar, holder.imgComercio, holder.imgComercio);
    }

    @Override
    public int getItemCount() {
        return comerciantes.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        CircleImageView imgComercio;
        ProgressBar progressBar;
        TextView txtNome;
        TextView txtDistancia;
        TextView txtLocal;

        public MyViewHolder(View itemView) {
            super(itemView);

            imgComercio = itemView.findViewById(R.id.fotoComercianteAdapter);
            progressBar = itemView.findViewById(R.id.progressBarFotoComerciante);
            txtNome = itemView.findViewById(R.id.txtNomeLojaComerciante);
            txtDistancia = itemView.findViewById(R.id.txtDistanciaComerciante);
            txtLocal = itemView.findViewById(R.id.txtLocalComerciante);
        }
    }
}
