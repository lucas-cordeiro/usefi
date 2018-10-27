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
import loureiro.enzo.usefi.myInterface.IFragment;
import loureiro.enzo.usefi.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class PerfilUsuarioFragment extends Fragment implements IFragment{

    private static final String TAG = "PERFIL_USUARIO_FRAGMENT";
    private FrameLayout frameLayout;
    private boolean edit;

    public PerfilUsuarioFragment() {
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
        View view = inflater.inflate(R.layout.fragment_perfil_usuario, container, false);

        frameLayout = view.findViewById(R.id.frameContainerUsuarioPerifl);

        edit = true;
        trocarFragment();

        return view;
    }

    public void trocarFragment(){
        edit = !edit;
        if(edit){
            PerfilUsuarioEditFragment fragment = new PerfilUsuarioEditFragment();
            fragment.setFragment(this);
            abrirFragment(fragment);

        }else{
            PerfilUsuarioExibirFragment fragment = new PerfilUsuarioExibirFragment();
            fragment.setFragment(this);
            abrirFragment(fragment);
        }
    }

    private void abrirFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.animator.fade_in, R.animator.fade_out);
        fragmentTransaction.replace(frameLayout.getId(),fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onStart() {
        super.onStart();
        setToolbarTitle();
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
        ((UsuarioActivity) getActivity()).setToolbarTitle("Perfil");
    }
}
