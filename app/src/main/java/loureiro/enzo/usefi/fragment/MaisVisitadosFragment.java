package loureiro.enzo.usefi.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import loureiro.enzo.usefi.activity.UsuarioActivity;
import loureiro.enzo.usefi.model.Categoria;
import loureiro.enzo.usefi.myInterface.IFragment;
import loureiro.enzo.usefi.model.Comerciante;
import loureiro.enzo.usefi.model.Usuario;
import loureiro.enzo.usefi.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MaisVisitadosFragment extends Fragment implements IFragment{

    private int menuItem = 1;
    private static final String TAG = "MAIS_VISITADOS_FRAGMENT";

    private Usuario usuario;

    public MaisVisitadosFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mais_visitados, container, false);

        FrameLayout frameLayout = view.findViewById(R.id.container_MaisVisitados);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        CategoriaFragment fragment = new CategoriaFragment();
        Categoria categoria = new Categoria();
        categoria.setId("0");
        categoria.setNome(Comerciante.MAIS_VISITADOS);
        fragment.categoria = categoria;
        fragmentTransaction.replace(frameLayout.getId(), fragment);
        fragmentTransaction.commit();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        setToolbarTitle();
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public String getTAG() {
        return TAG;
    }

    @Override
    public int getMenuItem() {
        return menuItem;
    }

    @Override
    public void setToolbarTitle() {
        ((UsuarioActivity)getActivity()).setToolbarTitle(Comerciante.MAIS_VISITADOS);
    }
}
