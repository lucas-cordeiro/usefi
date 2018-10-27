package loureiro.enzo.usefi.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import loureiro.enzo.usefi.model.Wifi;
import loureiro.enzo.usefi.R;

public class AdapterWifis extends RecyclerView.Adapter<AdapterWifis.MyViewHolder>{

    private List<Wifi> wifis;

    public AdapterWifis(List<Wifi> wifis) {
        this.wifis = wifis;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_wifi, parent, false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        holder.txtNome.setText(wifis.get(position).getNome());

    }

    @Override
    public int getItemCount() {
        return wifis.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView txtNome;

        public MyViewHolder(View itemView) {
            super(itemView);

            txtNome = itemView.findViewById(R.id.txtNomeWifi);
        }
    }
}
