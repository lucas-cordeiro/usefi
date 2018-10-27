package loureiro.enzo.usefi.fragment;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Date;

import loureiro.enzo.usefi.activity.MainActivity;
import loureiro.enzo.usefi.rnum.EnumPlanos;
import loureiro.enzo.usefi.helper.Others;
import loureiro.enzo.usefi.model.Comerciante;
import loureiro.enzo.usefi.model.Notificacao;
import loureiro.enzo.usefi.model.Usuario;
import loureiro.enzo.usefi.R;
import loureiro.enzo.usefi.util.AlertaUtil;
import loureiro.enzo.usefi.util.FirebaseUtil;
import loureiro.enzo.usefi.util.PermissaoUtil;
import loureiro.enzo.usefi.util.PlanoUtil;
import loureiro.enzo.usefi.util.ToastUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class NotificacaoFragment extends Fragment {

    private TextView txtNomeImage, txtNomeCliente, txtIdadeCliente;
    private EditText edtTitle, edtMensagem;
    private ImageButton imgButtonImagem;
    private Button btnEnviar;
    private ProgressBar progressBar;

    public Comerciante comerciante;
    public Date dateAtual;

    private Uri localImagem;
    private final int REQUEST_PERMISSION_IMGBUTTONBANNER = 100;
    private final int REQUEST_GALERIA_IMGBUTTONBANNER = 200;

    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private static final String TAG = "NOTIFICACAO_FRAGMENT";

    public NotificacaoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notificacao, container, false);

        edtTitle = view.findViewById(R.id.edtTitle);
        edtMensagem = view.findViewById(R.id.edtMensagem);
        imgButtonImagem = view.findViewById(R.id.imgButtonImageNotificacao);
        btnEnviar = view.findViewById(R.id.btnEnviar);
        progressBar = view.findViewById(R.id.progressBarNotificacaoComerciante);
        txtNomeImage = view.findViewById(R.id.txtImagemNotificacao);
        txtNomeCliente = view.findViewById(R.id.txtNomeClienteNotificao);
        txtIdadeCliente = view.findViewById(R.id.txtIdadeClienteNotificacao);

        txtNomeCliente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              addTag(Usuario.NOME_NOTIFICACAO);
            }
        });
        txtIdadeCliente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTag(Usuario.IDADE_NOTIFICACAO);
            }
        });

        imgButtonImagem.setOnClickListener(listenerImgButton);
        btnEnviar.setOnClickListener(listenerEnviar);

        comerciante = ((MainActivity) getActivity()).getComerciante();

        return view;
    }

    private void addTag(String tag){
        EditText editText;
        if(edtMensagem.isFocused()){
            editText = edtMensagem;
        }
        else if(edtTitle.isFocused()){
           editText = edtTitle;
        }else {
            ToastUtil.toastCustom(getActivity(), "Selecione um campo para inserir a TAG!", false);
            return;
        }

        int position = editText.getSelectionStart();
        String texto = editText.getText().toString();
        if(texto.length()==position){
           editText.setText(texto+tag);
        }else{
           String textoFinal = texto.substring(position);
           editText.setText(texto.substring(0,position)+tag+textoFinal);
        }
        editText.setSelection(editText.getText().toString().length());
        ToastUtil.toastCustom(getActivity(), "Tag "+tag+" adicionada", true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_PERMISSION_IMGBUTTONBANNER){//RequestCode do ImgButonLogo
            for(int permissaoResultado : grantResults){
                if(permissaoResultado == PackageManager.PERMISSION_DENIED) {
                    AlertaUtil.alertaErro("Permissão Negada","É necessário aceitar as permissões para uso do Aplicativo!",getContext());
                }else {
                    Intent galeria = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    if(galeria.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivityForResult(galeria, REQUEST_GALERIA_IMGBUTTONBANNER);
                    }
                }
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_GALERIA_IMGBUTTONBANNER) {//ResquetCode do ImgButton da Galeria

            if(resultCode == getActivity().RESULT_OK){
                try{
                    //Configurando Imagem [Início]
                    try {
                        localImagem = data.getData();
                        txtNomeImage.setText(Others.getFileName(localImagem, getActivity().getContentResolver()));

                        //imagem = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), localImagem);

                        //FirebaseUtil.enviarBannerStorageComerciante(imagem, comerciante, getActivity(), progressBar, btnEnviar, false);
                    }
                    catch (Exception e){//Erro na Imagem
                        ToastUtil.toastCustom(getActivity(), "Erro Bitmap: "+e.getMessage(), false);
                    }
                    //Configurando Imagem [Fim]
                }
                catch (Exception ex){
                    ex.printStackTrace();
                }

            }else {
                ToastUtil.toastCustom(getActivity(),"Imagem não selecionada.", false);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    private View.OnClickListener listenerImgButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //Verificar as Permissões
            if(PermissaoUtil.validarPermissoes(permissoesNecessarias, NotificacaoFragment.this, REQUEST_PERMISSION_IMGBUTTONBANNER)){

                Intent galeria = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if(galeria.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivityForResult(galeria, REQUEST_GALERIA_IMGBUTTONBANNER);
                }
            }
        }
    };

    private View.OnClickListener listenerEnviar = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(edtTitle.getText().toString().length()>0 && edtMensagem.getText().toString().length()>0){
                Notificacao notificacao = new Notificacao();
                notificacao.setTitle(edtTitle.getText().toString());
                notificacao.setMensagem(edtMensagem.getText().toString());
                if(localImagem!=null)
                    notificacao.setUrlImagen(localImagem.toString());

                FirebaseUtil.enviarNotificacao(getActivity(), notificacao, dateAtual, progressBar, btnEnviar);

            }
            else
                ToastUtil.toastCustom(getActivity(), "Preencha os campos corretamente!", false);
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        if(PlanoUtil.stringToEnumPlano(comerciante.getPlano()) == EnumPlanos.Basic){
            ToastUtil.toastCustom(getActivity(), "Seu plano não possui a função de Notificações. Atualize para o Plano Standard ou Premium!", false);
            btnEnviar.setEnabled(false);
        }else if(comerciante.getDataPagamento().equals(Comerciante.DATA_NULL)){
            ToastUtil.toastCustom(getActivity(), "Sua conta ainda não foi ativada por nossa Equipe para poder utilizar a função de Notificações", false);
            btnEnviar.setEnabled(false);
        }
    }
}
