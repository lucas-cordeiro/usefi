package loureiro.enzo.usefi.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import loureiro.enzo.usefi.R;
import loureiro.enzo.usefi.activity.UsuarioActivity;
import loureiro.enzo.usefi.adapter.AdapterString;
import loureiro.enzo.usefi.clickListener.RecyclerItemClickListener;
import loureiro.enzo.usefi.fragment.CategoriaFragment;
import loureiro.enzo.usefi.fragment.CategoriasFragment;
import loureiro.enzo.usefi.model.Categoria;
import loureiro.enzo.usefi.model.Comerciante;
import loureiro.enzo.usefi.myInterface.IFragment;
import loureiro.enzo.usefi.util.AnimacaoUtil;


/**
 * A simple {@link Fragment} subclass.
 */
public class FiltroFragment extends Fragment implements IFragment{

    private List<Categoria> categorias;
    private Categoria categoria;
    private Categoria categoriaSelecionada;

    private Button btnFiltro;
    private RecyclerView recyclerView;

    public static final String TAG = "FILTRO_FRAGMENT", INICIO = "Início";

    public FiltroFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_filtro, container, false);

        final LinearLayout linearLayout = view.findViewById(R.id.linearLayoutFiltro);
        recyclerView = view.findViewById(R.id.recyclerviewCategorias);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());


        final List<Categoria> categoriasVerificacao = new ArrayList<>();
        Categoria inicio = new Categoria();
        inicio.setId("0");
        inicio.setNome("Início");
        categoriasVerificacao.addAll(((UsuarioActivity) getActivity()).getCategorias());

        categorias = new ArrayList<>();
        categorias.add(inicio);
        for(Categoria categoria : categoriasVerificacao){
            String nome = categoria.getNome();
            if(!nome.equals(Comerciante.TODOS) && !nome.equals(Comerciante.NOVOS) && !nome.equals(Comerciante.POPULAR))
                categorias.add(categoria);
        }
        List<String> values = new ArrayList<>();
        for(Categoria categoria : categorias){
            values.add(categoria.getNome());
        }
        categoriaSelecionada = categoria;
        String nome = categoria.getNome();
        if(categoria.getNome().equals(Comerciante.TODOS) || categoria.getNome().equals(Comerciante.NOVOS) || categoria.getNome().equals(Comerciante.POPULAR)) {
            nome = INICIO;
            categoriaSelecionada.setNome(INICIO);
        }
        final AdapterString adapterString = new AdapterString(values, nome, R.layout.adapter_categorias, getActivity());
        recyclerView.setAdapter(adapterString);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                adapterString.setValorSelecionado(categorias.get(position).getNome());
                categoriaSelecionada = categorias.get(position);
                if(position==0)
                    categoriaSelecionada.setNome(INICIO);
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        }));

        btnFiltro = view.findViewById(R.id.btnFiltrar);
        btnFiltro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment;
                IFragment iFragment;
                boolean valor = categoriaSelecionada.getNome().equals(INICIO);
                Log.i(TAG, "valor: "+valor+" Nome: "+categoriaSelecionada.getNome());
                if(valor){
                    fragment = new CategoriasFragment();
                    iFragment = (CategoriasFragment)fragment;
                }else{
                    fragment = new CategoriaFragment();
                    ((CategoriaFragment)fragment).categoria = categoriaSelecionada;
                    iFragment = (CategoriaFragment)fragment;
                }
                ((UsuarioActivity) getActivity()).abrirFragment(iFragment, fragment);

                linearLayout.startAnimation(AnimacaoUtil.desaparecerView(500));
            }
        });

        linearLayout.startAnimation(AnimacaoUtil.mostrarView(500));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        setToolbarTitle();
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    @Override
    public String getTAG() {
        return TAG;
    }

    @Override
    public int getMenuItem() {
        return 0;
    }

    @Override
    public void setToolbarTitle() {
        ((UsuarioActivity)getActivity()).setToolbarTitle("Filtro");
    }
}
