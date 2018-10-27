package loureiro.enzo.usefi.fragment;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import loureiro.enzo.usefi.activity.UsuarioActivity;
import loureiro.enzo.usefi.helper.ValidacaoTil;
import loureiro.enzo.usefi.model.Cliente;
import loureiro.enzo.usefi.model.Usuario;
import loureiro.enzo.usefi.R;
import loureiro.enzo.usefi.util.AlertaUtil;
import loureiro.enzo.usefi.util.AnimacaoUtil;
import loureiro.enzo.usefi.util.Base64Util;
import loureiro.enzo.usefi.util.ConfiguracaoFirebase;
import loureiro.enzo.usefi.util.DateUtil;
import loureiro.enzo.usefi.util.FirebaseUtil;
import loureiro.enzo.usefi.util.MaskEditUtil;
import loureiro.enzo.usefi.util.PermissaoUtil;
import loureiro.enzo.usefi.util.ToastUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class PerfilUsuarioEditFragment extends Fragment implements LocationListener{

    private ProgressBar progressBarEdit, progressBarFab;
    private FloatingActionButton fabSalvar, fabCancelar;

    private TextInputLayout tilNome, tilSenha, tilConfirmarSenha, tilData, tilLocalizacao;
    private RadioButton radM, radF, radO;
    private ImageView imgFotoEdit;
    private ImageButton imageButton, imgLocalizacao;

    private boolean usuarioDefinido = false;
    private Usuario usuario;
    private DatabaseReference referenceUser;
    private ValueEventListener valueEventListenerUsuario;

    private PerfilUsuarioFragment fragment;

    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private Uri localImagem;

    private final int REQUEST_PERMISSION_IMGBUTTONFOTO = 100, REQUEST_PERMISSION_LOCATION = 101;
    private final int REQUEST_GALERIA_IMGBUTTONFOTO = 200;

    private static final String TAG = "PERFIL_EDIT_FRAGMENT";

    public PerfilUsuarioEditFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_perfil_usuario_edit, container, false);

        tilNome = view.findViewById(R.id.tilNomeUsuarioEdit);
        tilData = view.findViewById(R.id.tilNascimentoUsuarioEdit);
        tilLocalizacao = view.findViewById(R.id.tilLocalizacaoUsuarioEdit);
        tilSenha = view.findViewById(R.id.tilSenhaUsuarioEdit);
        tilConfirmarSenha = view.findViewById(R.id.tilConfirmarSenhaUsuarioEdit);

        tilData.getEditText().addTextChangedListener(MaskEditUtil.mask(tilData.getEditText(), MaskEditUtil.FORMAT_DATE));

        radM = view.findViewById(R.id.radMasculinoUsuarioEdit);
        radF = view.findViewById(R.id.radFemininoUsuarioEdit);
        radO = view.findViewById(R.id.radOutroUsuarioEdit);

        imgFotoEdit = view.findViewById(R.id.fotoUsuarioEditPerfil);
        imgLocalizacao = view.findViewById(R.id.imgLocalizacaoUsuarioEditPerfil);
        imgLocalizacao.setOnClickListener(listenerLocalizacao);

        fabSalvar = view.findViewById(R.id.fabSalvarUsuarioPerfil);
        fabSalvar.setOnClickListener(listenerFabSalvar);
        fabCancelar = view.findViewById(R.id.fabEditCancelarUsuarioPerfil);
        fabCancelar.setOnClickListener(listenerFabCancelar);


        imageButton = view.findViewById(R.id.imgUsuarioEditPerfil);
        imageButton.setOnClickListener(listenerImgButtonFoto);

        progressBarEdit = view.findViewById(R.id.progressBarUsuarioEditPerfil);
        progressBarFab = view.findViewById(R.id.progressBarFabUsuarioPerfil);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStop");
        if (!usuarioDefinido) {
            recuperarUsuario();
            AnimacaoUtil.progressBar(progressBarEdit, imgFotoEdit, true);
        }
        else {
            definirDadosUsuario();
        }
    }
    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
        if(valueEventListenerUsuario!=null)
            referenceUser.removeEventListener(valueEventListenerUsuario);
    }

    //Método para Recebimento de Resultados de Permissões Solicitadas
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_PERMISSION_IMGBUTTONFOTO){//RequestCode do ImgButonFoto
            for(int permissaoResultado : grantResults){
                if(permissaoResultado == PackageManager.PERMISSION_DENIED) {
                    AlertaUtil.alertaErro("Permissão Negada","É necessário aceitar as permissões para uso do Aplicativo!",getContext());
                }else {
                    Intent galeria = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    if(galeria.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivityForResult(galeria, REQUEST_GALERIA_IMGBUTTONFOTO);
                    }
                }
            }
        }
        else if(requestCode == REQUEST_PERMISSION_LOCATION){
            boolean aceitou = true;
            for(int permissaoResultado : grantResults){
                if(permissaoResultado == PackageManager.PERMISSION_DENIED) {
                    AlertaUtil.alertaErro("Permissão Negada","É necessário aceitar as permissões para uso do Aplicativo!",getContext());
                    View[] views = {fabSalvar, fabCancelar};
                    AnimacaoUtil.progressBar(progressBarFab,views, false );
                    aceitou = false;
                    break;
                }
            }

            if(aceitou)
                finishGettingLocations();
        }
    }

    //Método para Recebimento de Resultados desta Activity
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_GALERIA_IMGBUTTONFOTO) {//ResquetCode do ImgButton da Galeria

            if (resultCode == getActivity().RESULT_OK) {

                //Configurando Imagem [Início]
                try {
                    ToastUtil.toastCustom(getActivity(), "Nova imagem Selecionada!", true);
                    localImagem = data.getData();
                    Bitmap imagem = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), localImagem);
                    imgFotoEdit.setImageBitmap(imagem);
                    AnimacaoUtil.progressBar(progressBarEdit, imgFotoEdit, false);
                } catch (Exception e) {//Erro na Imagem
                    ToastUtil.toastCustom(getActivity(), "Erro Bitmap: " + e.getMessage(), false);
                }
                //Configurando Imagem [Fim]

            } else {
                ToastUtil.toastCustom(getActivity(), "É preciso adicionar uma Imagem!", false);
            }
        }

    }

    //Recupera o Usuario Logado
    private void recuperarUsuario() {
        new Thread(){
            @Override
            public void run() {
                FirebaseAuth firebaseAuth = ConfiguracaoFirebase.getFirebaseAuth();
                FirebaseUser user = firebaseAuth.getCurrentUser();
                final String id = Base64Util.dadoToBase64(user.getEmail());
                referenceUser = ConfiguracaoFirebase.getReferenceFirebase().child("Usuarios").child(id);
                valueEventListenerUsuario = referenceUser.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.i(TAG, dataSnapshot.getValue().toString());
                        usuario = dataSnapshot.getValue(Usuario.class);
                        usuario.setId(id);

                        StorageReference referenceImagem = ConfiguracaoFirebase.getStorageReference().child(Usuario.USUARIOS).child(usuario.getId()).child("perfil.png");
                        referenceImagem.getDownloadUrl().addOnCompleteListener(getActivity(), new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if(task.isSuccessful()){
                                    usuario.setImgUrl(task.getResult().toString());
                                }


                                definirDadosUsuario();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }.start();
    }

    private void definirDadosUsuario() {
       getActivity().runOnUiThread(new Runnable() {
           @Override
           public void run() {
               tilNome.getEditText().setText(usuario.getNome());
               tilData.getEditText().setText("");
               tilData.getEditText().setText(DateUtil.dataFirebaseToDataNormal(usuario.getIdade()));
               tilLocalizacao.getEditText().setText(usuario.getLocalizacao());
               tilSenha.getEditText().setText("Password");
               tilConfirmarSenha.getEditText().setText("");

               if(usuario.getGenero().equals(Cliente.MASCULINO))
                   radM.setChecked(true);
               else if(usuario.getGenero().equals(Cliente.FEMINO))
                   radF.setChecked(true);
               else
                   radO.setChecked(true);

               FirebaseUtil.recuperarImagemStorage(getActivity(), Uri.parse(usuario.getImgUrl()), progressBarEdit, imgFotoEdit, imgFotoEdit);
           }
       });
    }

    private View.OnClickListener listenerFabSalvar = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AnimacaoUtil.progressBar(progressBarFab, fabSalvar, true);

            if(validarCampos()){
                Usuario usuario = new Usuario();
                usuario.setId(PerfilUsuarioEditFragment.this.usuario.getId());
                usuario.setNome(tilNome.getEditText().getText().toString());
                usuario.setLogin(PerfilUsuarioEditFragment.this.usuario.getLogin());
                usuario.setIdade(DateUtil.dataNormalToDataFirebase(tilData.getEditText().getText().toString()));
                usuario.setLocalizacao(tilLocalizacao.getEditText().getText().toString());
                if(radM.isChecked())
                    usuario.setGenero(Cliente.MASCULINO);
                else if(radF.isChecked())
                    usuario.setGenero(Cliente.FEMINO);
                else
                    usuario.setGenero(Cliente.OUTRO);

                if(localImagem!=null)
                    usuario.setImgUrl(localImagem.toString());

                if(PerfilUsuarioEditFragment.this.usuario.getLatitude()!=0){
                    usuario.setLatitude(PerfilUsuarioEditFragment.this.usuario.getLatitude());
                    usuario.setLongitude(PerfilUsuarioEditFragment.this.usuario.getLongitude());
                }

                View[] views = {fabSalvar, fabCancelar};
                FirebaseUtil.atualizarDadosUsuario(getActivity(),PerfilUsuarioEditFragment.this, usuario,true, progressBarFab, views);
            }else{
                AnimacaoUtil.progressBar(progressBarFab, fabSalvar, false);
            }
        }
    };

    private View.OnClickListener listenerFabCancelar = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            fragment.trocarFragment();
        }
    };

    private View.OnClickListener listenerLocalizacao = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ToastUtil.toastLoad(getActivity(), "Carregando Localização...", true);
            startGettingLocations();
        }
    };

    private View.OnClickListener listenerImgButtonFoto = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //Verificar as Permissões
            if(PermissaoUtil.validarPermissoes(permissoesNecessarias, PerfilUsuarioEditFragment.this, REQUEST_PERMISSION_IMGBUTTONFOTO)){
                AnimacaoUtil.progressBar(progressBarEdit, imgFotoEdit, true);
                Intent galeria = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if(galeria.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivityForResult(galeria, REQUEST_GALERIA_IMGBUTTONFOTO);
                }
            }
        }
    };

    private boolean validarCampos() {
        boolean campoInvalido = false;
        ArrayList<Boolean> campos = new ArrayList<>();

        campos.add(ValidacaoTil.verificarTahPreenchido(tilNome));
        campos.add(ValidacaoTil.verificarData(tilData));
        campos.add(ValidacaoTil.verificarTahPreenchido(tilLocalizacao));
        if(!tilSenha.getEditText().getText().toString().equals("Password"))//Caso queira alterar a senha
        {
            campos.add(ValidacaoTil.verificarEhSenhaCadastro(tilSenha));
            campos.add(ValidacaoTil.verificarConfirmacaoSenha(tilSenha, tilConfirmarSenha));
        }

        for (Boolean valido:campos) {
            if (!valido) {
                campoInvalido = true;
                break;
            }
        }

        return !campoInvalido;
    }


    //Localização
    @Override
    public void onLocationChanged(Location location) {
        if(PerfilUsuarioEditFragment.this.isVisible()){
            usuario.setLongitude(location.getLongitude());
            usuario.setLatitude(location.getLatitude());
            ToastUtil.toastCustom(getActivity(), "Localização Encontrada!\nLat: "+usuario.getLatitude()+"\nLong: "+usuario.getLongitude(), true);
            View[] views = {fabSalvar, fabCancelar};
            FirebaseUtil.atualizarLocalizacao(getActivity(),this, usuario, progressBarFab, views);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }



    private void startGettingLocations() {
        View[] views = {fabSalvar, fabCancelar};
        AnimacaoUtil.progressBar(progressBarFab,views, true );
        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        boolean isNetwork = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isNetwork) {
            AlertaUtil.solicitarAtivacaoWifi(getActivity());
        } else {
            String[] permissoes = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            //Verificar as Permissões
            if(PermissaoUtil.validarPermissoes(permissoes,PerfilUsuarioEditFragment.this, REQUEST_PERMISSION_LOCATION)){
                finishGettingLocations();
            }
        }
    }

    private void finishGettingLocations(){
        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;// Distance in meters
        long MIN_TIME_BW_UPDATES = 1000 * 10;// Time in milliseconds

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ToastUtil.toastLoad(getActivity(), "É necessário aceitar as permissões!", true);
            View[] views = {fabSalvar, fabCancelar};
            AnimacaoUtil.progressBar(progressBarFab,views, false );
            return;
        }
        lm.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
    }



    public void setFragment(PerfilUsuarioFragment fragment) {
        this.fragment = fragment;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        usuarioDefinido = true;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(TAG, "onDetach");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(TAG, "onDestroyView");
    }
}
