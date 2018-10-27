package loureiro.enzo.usefi.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import loureiro.enzo.usefi.model.ItemPlanos;
import loureiro.enzo.usefi.R;

public class AdapterItensPlanos extends RecyclerView.Adapter<AdapterItensPlanos.MyViewHolder>{

    private List<ItemPlanos> itemPlanos;

    public AdapterItensPlanos(List<ItemPlanos> itemPlanos) {
        this.itemPlanos = itemPlanos;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_itensplanos, parent, false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ItemPlanos item = itemPlanos.get(position);

        holder.item.setText(item.getItem());
        holder.item.setTextColor(item.getColor());
        if(item.isVisivel())
            holder.item.setVisibility(View.VISIBLE);
        else
            holder.item.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return itemPlanos.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView item;

        public MyViewHolder(View itemView) {
            super(itemView);

            item = itemView.findViewById(R.id.itemPlano);
        }
    }
}
