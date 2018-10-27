package loureiro.enzo.usefi.fragment;


import android.app.Dialog;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import loureiro.enzo.usefi.activity.UsuarioActivity;
import loureiro.enzo.usefi.model.Categoria;
import loureiro.enzo.usefi.myInterface.IFragment;
import loureiro.enzo.usefi.model.Usuario;
import loureiro.enzo.usefi.R;
import loureiro.enzo.usefi.util.ToastUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProximoUsuarioFragment extends Fragment implements IFragment{

    private static final String TAG = "PROXIMO_USUARIO_FRAGMENT";
    private static final int itemMenu = 4;
    private Usuario usuario;

    public ProximoUsuarioFragment() {
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
        final View view = inflater.inflate(R.layout.fragment_proximo_usuario, container, false);

        //Senha para validar as edições
        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog_text_input_layout);
        TextView title = dialog.findViewById(R.id.txtTitle);
        title.setText("Próximos");
        TextView msg = dialog.findViewById(R.id.txtMsg);
        msg.setText("Informe a distância máxima de comércios:");
        final TextInputLayout tilRaio = dialog.findViewById(R.id.tilCampo);
        tilRaio.setHint("Distância em Kms");
        final Button cancell = dialog.findViewById(R.id.btnCancelarDialog);
        cancell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                getActivity().onBackPressed();
            }
        });
        Button enviar = dialog.findViewById(R.id.btnEnviarDialog);
        enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tilRaio.getEditText().getText().toString().length()>0){
                    try{
                        double raio = Double.valueOf(tilRaio.getEditText().getText().toString());
                        dialog.dismiss();
                        FrameLayout frameLayout = view.findViewById(R.id.frameContainerProximos);
                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        CategoriaFragment fragment = new CategoriaFragment();
                        Categoria categoria = new Categoria();
                        categoria.setNome(Usuario.PROXIMOS);
                        categoria.setId("0");
                        fragment.categoria = categoria;
                        fragment.setRaio(raio);
                        fragmentTransaction.replace(frameLayout.getId(), fragment);
                        fragmentTransaction.commit();
                    }
                    catch (Exception e){
                        ToastUtil.toastLoad(getActivity(), "Número inválido! Apenas números!!", false);
                    }
                }else{
                    ToastUtil.toastLoad(getActivity(), "Informe uma distância!", false);
                }
            }
        });

        dialog.show();

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
        ((UsuarioActivity) getActivity()).setToolbarTitle("Próximos a você");
    }
}
