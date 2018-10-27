package loureiro.enzo.usefi.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import loureiro.enzo.usefi.R;
import loureiro.enzo.usefi.util.AlertaUtil;
import loureiro.enzo.usefi.util.ConfiguracaoFirebase;
import loureiro.enzo.usefi.util.EmailUtil;
import loureiro.enzo.usefi.util.EquipeUseFiUtil;
import loureiro.enzo.usefi.util.ToastUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class SuporteFragment extends Fragment {


    public SuporteFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_suporte, container, false);

        TextView txtSenha = view.findViewById(R.id.txtSenhaSuporte);
        txtSenha.setOnClickListener(new View.OnClickListener() {//Recuperar Senha
            @Override
            public void onClick(View v) {
                AlertaUtil.alertaSolicitarNovaSenha(getActivity(), ConfiguracaoFirebase.getFirebaseAuth().getCurrentUser().getEmail());
            }
        });
        TextView txtTermos = view.findViewById(R.id.txtTermosDeUsoSuporte);
        txtTermos.setOnClickListener(new View.OnClickListener() {//Termos de Uso
            @Override
            public void onClick(View v) {
                ToastUtil.toastCustom(getActivity(),"Termos de Uso do Aplicativo!", true);//Implementar
            }
        });

        final EditText edtAssunto = view.findViewById(R.id.edtTitle);
        final EditText edtMensagem = view.findViewById(R.id.edtMensagem);

        Button btnEnviar = view.findViewById(R.id.btnEnviar);
        btnEnviar.setOnClickListener(new View.OnClickListener() {//Enviar Email
            @Override
            public void onClick(View v) {
                if(edtAssunto.getText().toString().length() > 0 && edtMensagem.getText().toString().length()>0){
                    EmailUtil.enviarEmail(getActivity(), true, true, EquipeUseFiUtil.EMAIL, edtAssunto.getText().toString(), edtMensagem.getText().toString());
                    edtAssunto.setText(null);
                    edtMensagem.setText(null);
                }else{
                    ToastUtil.toastCustom(getActivity(), "Preencha os campos corretamente!", false);
                }
            }
        });

        return view;
    }

}
