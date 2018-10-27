package loureiro.enzo.usefi.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import loureiro.enzo.usefi.R;
import loureiro.enzo.usefi.model.Wifi;

public class AdapterString  extends RecyclerView.Adapter<AdapterString.MyViewHolder>{

    private List<String> strings;
    private String valorSelecionado;
    private int adapter;
    private Context context;

    public AdapterString(List<String> strings, String valorSelecionado, int adapter, Context context) {
        this.strings = strings;
        this.valorSelecionado = valorSelecionado;
        this.adapter = adapter;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(adapter, parent, false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String texto = strings.get(position);
        holder.txtNome.setText(texto);
        int id = R.drawable.borda_retangular;
        if(texto.equals(valorSelecionado))
            id = R.color.colorAzul;
        holder.linearLayout.setBackground(ContextCompat.getDrawable(context, id));
    }

    @Override
    public int getItemCount() {
        return strings.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView txtNome;
        LinearLayout linearLayout;
        public MyViewHolder(View itemView) {
            super(itemView);

            txtNome = itemView.findViewById(R.id.txtString);
            linearLayout = itemView.findViewById(R.id.linearLayoutString);
        }
    }

    public void setValorSelecionado(String valorSelecionado) {
        this.valorSelecionado = valorSelecionado;
        this.notifyDataSetChanged();
    }
}
