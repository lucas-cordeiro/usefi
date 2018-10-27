package loureiro.enzo.usefi.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
public class VisitadosRecentementeFragment extends Fragment implements IFragment{

    private static final String TAG = "VISITADOS_RECENTEMENTE_FRAGMENT";
    private static final int itemMenu= 2;

    private Usuario usuario;
    public VisitadosRecentementeFragment() {
        // Required empty public constructor
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        Animation animation = super.onCreateAnimation(transit, enter, nextAnim);

        if (animation == null && nextAnim != 0) {
            animation = AnimationUtils.loadAnimation(getActivity(), nextAnim);
        }

        if (animation != null) {
            getView().setLayerType(View.LAYER_TYPE_HARDWARE, null);

            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                public void onAnimationEnd(Animation animation) {
                    getView().setLayerType(View.LAYER_TYPE_NONE, null);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }

                // ...other AnimationListener methods go here...
            });
        }
        return animation;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_visitados_recentemente, container, false);

        FrameLayout frameLayout = view.findViewById(R.id.container_VisitadosRecentemente);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        CategoriaFragment fragment = new CategoriaFragment();
        Categoria categoria = new Categoria();
        categoria.setId("0");
        categoria.setNome(Comerciante.VISITADOS_RECENTEMENTE);
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
        return itemMenu;
    }

    @Override
    public void setToolbarTitle() {
        ((UsuarioActivity) getActivity()).setToolbarTitle(Comerciante.VISITADOS_RECENTEMENTE);
    }
}
