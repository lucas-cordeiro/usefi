package loureiro.enzo.usefi.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import loureiro.enzo.usefi.helper.Others;
import loureiro.enzo.usefi.helper.ValidacaoDados;
import loureiro.enzo.usefi.model.Cliente;
import loureiro.enzo.usefi.model.Comerciante;
import loureiro.enzo.usefi.model.Usuario;
import loureiro.enzo.usefi.R;
import loureiro.enzo.usefi.util.AlarmUtil;
import loureiro.enzo.usefi.util.AlertaUtil;
import loureiro.enzo.usefi.util.AnimacaoUtil;
import loureiro.enzo.usefi.helper.ValidacaoTil;
import loureiro.enzo.usefi.util.ConfiguracaoFirebase;
import loureiro.enzo.usefi.util.EquipeUseFiUtil;
import loureiro.enzo.usefi.util.FirebaseUtil;
import loureiro.enzo.usefi.util.MaskEditUtil;
import loureiro.enzo.usefi.util.PermissaoUtil;
import loureiro.enzo.usefi.util.ToastUtil;

public class EscolhaLoginActivity extends AppCompatActivity implements View.OnClickListener, LocationListener {

    private ImageView logo;

    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private CardView usuario;
    private ConstraintLayout constraintLayoutUsuario;
    private TextInputLayout tilLoginUsuario, tilSenhaUsuario, tilConfirmarSenhaUsuario, tilNomeUsuario, tilNascimentoUsuario;
    private LinearLayout linearLayoutCamposCadastroUsuario;
    private ImageButton imgButtonFoto, imgButtonLocalizacaoUsuario;
    private TextView txtCadastrarUsuario, txtLoginUsuario, txtFoto, txtLocalizacaoUsuario;
    private Button btnEntraUsuario;
    private boolean loginUsuario;
    private boolean cadastroUsuario;
    private boolean animacaoUsuarioAtiva = false;

    private CardView comerciante;
    private ConstraintLayout constraintLayoutComerciante;
    private TextInputLayout tilLoginComerciante, tilSenhaComerciante;
    private TextView txtCadastrarComerciante;
    private Button btnEntrarComerciante;
    private boolean loginComerciante;
    private boolean animacaoComercianteAtiva = false;

    private TextView txtTermosdeUso;
    private LatLng currentLatLng;

    private LinearLayout linearCards;
    private Uri imagemSelecionada = null;
    private ProgressBar progressBar;

    private final int TEMPO_ANIMACAO = 500, TAMANHO_PADRAO_COMERCIANTE = 50,
            TAMANHO_LOGIN_COMERCIANTE = 330;

    private final int TAMANHO_PADRAO_USUARIO = 50,
            TAMANHO_LOGIN_USUARIO = 330,
            TAMANHO_CADASTRO_USUARIO = 700,
            TAMANHO_CAMPOS_CADASTRO_USUARIO = 390;
    private final int REQUEST_PERMISSION_IMGBUTTONFOTO = 101, REQUEST_PERMISSION_LOCATION = 102;
    private final int REQUEST_GALERIA_IMGBUTTONFOTO = 201;

    private final String TAG = "ESCOLHA_LOGIN";

    //Método de Criação
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_escolha_login);

        //Remove foco automático dos EditText
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        logo = findViewById(R.id.imgBorda);

        //Componentes do Usuario
        usuario = findViewById(R.id.cardUsuario);
        constraintLayoutUsuario = findViewById(R.id.constraintUsuario);
        linearLayoutCamposCadastroUsuario = findViewById(R.id.linearCamposCadastroUsuario);
        imgButtonFoto = findViewById(R.id.imgBtnFotoUsuario);
        imgButtonLocalizacaoUsuario = findViewById(R.id.imgBtnLocalizacaoUsuario);
        tilLoginUsuario = findViewById(R.id.tilLoginUsuario);
        tilSenhaUsuario = findViewById(R.id.tilSenhaUsuario);
        tilConfirmarSenhaUsuario = findViewById(R.id.tilConfirmarSenhaUsuario);
        tilNomeUsuario = findViewById(R.id.tilNomeUsuario);
        tilNascimentoUsuario = findViewById(R.id.tilNascimentoUsuario);
        txtCadastrarUsuario = findViewById(R.id.txtCadastrarUsuario);
        txtLoginUsuario = findViewById(R.id.txtLoginUsuario);
        txtFoto = findViewById(R.id.txtFoto);
        txtLocalizacaoUsuario = findViewById(R.id.txtLocalizacaoUsuario);
        btnEntraUsuario = findViewById(R.id.btnEntrarUsuario);

        //Componentes do Comerciante
        comerciante = findViewById(R.id.cardComerciante);
        constraintLayoutComerciante = findViewById(R.id.constraintComerciante);
        tilLoginComerciante = findViewById(R.id.tilLoginComerciante);
        tilSenhaComerciante = findViewById(R.id.tilSenhaComerciante);
        txtTermosdeUso = findViewById(R.id.txtTermosdeUso);
        txtCadastrarComerciante = findViewById(R.id.txtCadastrarComerciante);
        btnEntrarComerciante = findViewById(R.id.btnEntrarComerciante);

        linearCards = findViewById(R.id.linearCardsLogin);
        progressBar = findViewById(R.id.progressBarLogin);

        //Máscara para os EditText
        tilNascimentoUsuario.getEditText().addTextChangedListener(MaskEditUtil.mask(tilNascimentoUsuario.getEditText(), MaskEditUtil.FORMAT_DATE));

        /*
        //Animação Logo
        animacaoLogo();*/

        //Define ovento de clique nos constraints
        constraintLayoutComerciante.setOnClickListener(this);
        constraintLayoutUsuario.setOnClickListener(this);

        //Define o evento de clique nos botões
        btnEntrarComerciante.setOnClickListener(this);
        btnEntraUsuario.setOnClickListener(this);

        //Define o evento de clique nos imgbuttons
        imgButtonFoto.setOnClickListener(this);
        imgButtonLocalizacaoUsuario.setOnClickListener(this);

        //Define o evento de clique dos textos
        txtCadastrarUsuario.setOnClickListener(this);
        txtLoginUsuario.setOnClickListener(this);
        txtCadastrarComerciante.setOnClickListener(this);
        txtTermosdeUso.setOnClickListener(this);

        if (Others.isOnline(EscolhaLoginActivity.this)) {
            Animation animation = AnimationUtils.loadAnimation(EscolhaLoginActivity.this, R.anim.rotate);
            logo.startAnimation(animation);
            DatabaseReference referenceUseFi = ConfiguracaoFirebase.getReferenceFirebase().child(EquipeUseFiUtil.UseFi).child("Dados");
            referenceUseFi.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    EquipeUseFiUtil.TEMPO_CONEXAO_WIFI = Integer.valueOf(dataSnapshot.child(EquipeUseFiUtil.TEMPO_CONEXAO).getValue().toString());
                    EquipeUseFiUtil.TEMPO_CONEXAO_LIMITE = Integer.valueOf(dataSnapshot.child(EquipeUseFiUtil.TEMPO_LIMITE_CONEXAO).getValue().toString());
                    EquipeUseFiUtil.EMAIL = dataSnapshot.child("email").getValue().toString();

                    Log.i(TAG, "UseFi: " + dataSnapshot.getValue().toString() + " | E-mail: " + EquipeUseFiUtil.EMAIL + " | TempoConexaoWifi: " + EquipeUseFiUtil.TEMPO_CONEXAO_WIFI + " | TempoConexaoLimite: " + EquipeUseFiUtil.TEMPO_CONEXAO_LIMITE);

                    //Se houver um usuário logado ele é logado automaticamente
                    if (FirebaseUtil.verificaContaLogada())
                        FirebaseUtil.identificarConta(EscolhaLoginActivity.this, null, linearCards);
                    else{
                        animacaoLogo();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.i(TAG, "Error: "+databaseError.getMessage());
                    animacaoLogo();
                }
            });
        } else {
            animacaoLogo();
            if (FirebaseUtil.verificaContaLogada())
                FirebaseUtil.identificarConta(EscolhaLoginActivity.this, progressBar, linearCards);
        }
    }

    //Método para Recebimento de Resultados de Permissões Solicitadas
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.i(TAG, "requesCode: " + requestCode);
        if (requestCode == REQUEST_PERMISSION_IMGBUTTONFOTO) {
            for (int permissaoResultado : grantResults) {
                if (permissaoResultado == PackageManager.PERMISSION_DENIED) {
                    AlertaUtil.alertaErro("Permissão Negada", "É necessário aceitar as permissões para uso do Aplicativo!", this);
                } else {
                    Intent galeria = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    if (galeria.resolveActivity(getPackageManager()) != null) {

                        int request = REQUEST_GALERIA_IMGBUTTONFOTO;

                        startActivityForResult(galeria, request);
                    }
                }
            }
        } else if (requestCode == REQUEST_PERMISSION_LOCATION) {
            Log.i(TAG, "REQUEST_PERMISSION_LOCATION");
            finishGettingLocations();
        }
    }

    //Método para Recebimento de Resultados desta Activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(REQUEST_GALERIA_IMGBUTTONFOTO == requestCode){
            if(resultCode == RESULT_OK){
                try{
                    imagemSelecionada = data.getData();

                    txtFoto.setText(Others.getFileName(imagemSelecionada, getContentResolver()));
                    txtFoto.setTextColor(Color.WHITE);

                }
                catch (Exception ex){
                    ex.printStackTrace();
                }

            }else {
                ToastUtil.toastCustom(this,"É preciso adicionar uma Imagem!", false);
            }
        }


    }


    //Método para Controle de Eventos de Clique
    @Override
    public void onClick(View v) {//Gerenciador de Eventos de Clique

        int id = v.getId();

        if (id == constraintLayoutUsuario.getId()) {//Constraint do Usuario

            if (loginComerciante) {
                loginComerciante = false;
                animacaoComerciante(false);
            }

            loginUsuario = !loginUsuario;
            animacaoUsuario(loginUsuario);

        } else if (id == txtCadastrarUsuario.getId()) {//TXT Cadastrar um Usuario

            cadastroUsuario = true;
            animacaoUsuario(loginUsuario);
            btnEntraUsuario.setText("Cadastrar");

        } else if (id == txtLoginUsuario.getId()) {//TXT Logar com uma conta de Usuario

            cadastroUsuario = false;
            animacaoUsuario(loginUsuario);
            btnEntraUsuario.setText("Entrar");

        } else if (id == btnEntraUsuario.getId()) {//Button de Logar/Cadastrar o Usuario

            if (!animacaoUsuarioAtiva) {

                //Validação de Dados [Início]
                List<Boolean> campos = new ArrayList<>();
                boolean campoInvalido = false;
                int countErros = 0;

                if (cadastroUsuario) {//Cadastro

                    setTextErroUsuario(false);

                    campos.add(ValidacaoTil.verificarEhEmailCadastro(tilLoginUsuario));
                    campos.add(ValidacaoTil.verificarEhSenhaCadastro(tilSenhaUsuario));
                    campos.add(ValidacaoTil.verificarConfirmacaoSenha(tilSenhaUsuario, tilConfirmarSenhaUsuario));
                    campos.add(ValidacaoTil.verificarTahPreenchido(tilNomeUsuario));
                    campos.add(ValidacaoTil.verificarData(tilNascimentoUsuario));

                    if (txtFoto.getText().toString().equals("Foto")) {//Validação escolheu Imagem para Logo
                        countErros++;
                        AnimacaoUtil.piscarTextView(txtFoto, 2, TEMPO_ANIMACAO, getResources().getColor(R.color.colorError));
                        campoInvalido = true;
                    }

                    if (txtLocalizacaoUsuario.getText().toString().equals("Localização")) {
                        countErros++;
                        AnimacaoUtil.piscarTextView(txtLocalizacaoUsuario, 2, TEMPO_ANIMACAO, getResources().getColor(R.color.colorError));
                        campoInvalido = true;
                    }

                } else {//Login

                    setTextErroUsuario(true);

                    campos.add(ValidacaoTil.verificarEhEmail(tilLoginUsuario));
                    campos.add(ValidacaoTil.verificarEhSenha(tilSenhaUsuario));

                }

                for (boolean campo : campos) {
                    if (!campo) {
                        campoInvalido = true;
                        break;
                    }
                }

                //Validação de Dados [Fim]

                if (!campoInvalido) {//Campos Preenchidos Corretamente

                    animacaoUsuario(false);
                    if (cadastroUsuario) {//Cadastrar um Usuário
                        cadastrarUsuario();
                    } else {//Logar com um Usuário
                        loginUsuario();
                    }
                } else {//Campo(s) Preenchido(s) Incorretamente
                    String msg = "Preencha os Campos Corretamente!";

                    if (countErros == 1)
                        msg = "Preencha o Campo Corretamente!";

                    ToastUtil.toastCustom(this, msg, false);
                }

            }

        } else if (id == imgButtonFoto.getId()) {//Escolha da foto

            //Verificar as Permissões
            if (PermissaoUtil.validarPermissoes(permissoesNecessarias, this, REQUEST_PERMISSION_IMGBUTTONFOTO)) {

                Intent galeria = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (galeria.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(galeria, REQUEST_GALERIA_IMGBUTTONFOTO);
                }
            }

        } else if (id == constraintLayoutComerciante.getId()) {//Constraint do Comerciante

            if (loginUsuario) {
                loginUsuario = false;
                animacaoUsuario(false);
            }

            loginComerciante = !loginComerciante;
            animacaoComerciante(loginComerciante);

        } else if (id == txtCadastrarComerciante.getId()) {//TXT Cadastrar um Comerciante

            AlertDialog.Builder builder = new AlertDialog.Builder(EscolhaLoginActivity.this);
            builder.setTitle("Cadastro");
            builder.setMessage("Novo? Efetue seu cadastro em nosso site!");
            builder.setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ToastUtil.toastCustom(EscolhaLoginActivity.this, "Ir para a página de cadastro do site.", true);
                }
            });
            builder.show();

        } else if (id == txtTermosdeUso.getId()) {//TXT Termos de Uso do Comerciante

            ToastUtil.toastCustom(this, "Termos de Uso do Aplicativo!", true);

        } else if (id == btnEntrarComerciante.getId()) {//Button de Logar/Cadastrar o Comerciante

            if (!animacaoComercianteAtiva) {
                //Validação de Dados [Início]
                List<Boolean> campos = new ArrayList<>();
                boolean campoInvalido = false;
                int countErros = 0;

                setTextErroComerciante();
                campos.add(ValidacaoTil.verificarEhEmailCadastro(tilLoginComerciante));
                campos.add(ValidacaoTil.verificarEhSenhaCadastro(tilSenhaComerciante));


                for (Boolean campo : campos) {
                    if (!campo) {
                        campoInvalido = true;
                        break;
                    }
                }

                //Validação de Dados [Fim]

                if (!campoInvalido) {//Campos Preenchidos Corretamente
                    animacaoComerciante(false);
                    loginComerciante();

                } else {//Campo(s) Preenchido(s) Incorretamente
                    String msg = "Preencha os Campos Corretamente!";

                    if (countErros == 1)
                        msg = "Preencha o Campo Corretamente!";

                    ToastUtil.toastCustom(this, msg, false);
                }
            }

        } else if (id == imgButtonLocalizacaoUsuario.getId()) {//Localização usuário
            startGettingLocations();

        }
    }

    //Definir TextErro Usuario
    private void setTextErroUsuario(boolean login){
        int style = R.style.ErrorTextColorCinza;

        tilLoginUsuario.setErrorTextAppearance(style);
        tilLoginUsuario.setError(Html.fromHtml("<font><em>email@dominio.com</em></font>"));
        tilSenhaUsuario.setErrorTextAppearance(style);
        tilSenhaUsuario.setError(Html.fromHtml("<font><em>A - a | 0 - 9 |</em></font>"));

        if(!login){
            tilNomeUsuario.setErrorTextAppearance(style);
            tilNomeUsuario.setError(Html.fromHtml("<font><em>Thiago da Silva</em></font>"));
            tilNascimentoUsuario.setErrorTextAppearance(style);
            tilNascimentoUsuario.setError(Html.fromHtml("<font><em>01/01/1990</em></font>"));
            tilConfirmarSenhaUsuario.setErrorTextAppearance(style);
            tilConfirmarSenhaUsuario.setError(Html.fromHtml("<font><em>Repetir Senha</em></font>"));
        }
    }

    //Definir TextErro Comerciante
    private void setTextErroComerciante(){
        int style = R.style.ErrorTextColorAzul;
        tilLoginComerciante.setErrorTextAppearance(style);
        tilLoginComerciante.setError(Html.fromHtml("<font><em>email@dominio.com</em></font>"));
        tilSenhaComerciante.setErrorTextAppearance(style);
        tilSenhaComerciante.setError(Html.fromHtml("<font><em>A - a | 0 - 9 |</em></font>"));
    }

    //Método para logar um Usuário
    private void loginUsuario(){
        loginUsuario = false;
        animacaoUsuario(loginUsuario);
        FirebaseUtil.login(EscolhaLoginActivity.this, tilLoginUsuario.getEditText().getText().toString(), tilSenhaUsuario.getEditText().getText().toString(),progressBar, linearCards, Usuario.USUARIOS);
    }

    //Método para cadastrar um Usuário
    private void cadastrarUsuario(){
        Usuario usuario = new Usuario();
        usuario.setLogin(tilLoginUsuario.getEditText().getText().toString());
        usuario.setSenha(tilSenhaUsuario.getEditText().getText().toString());
        usuario.setNome(tilNomeUsuario.getEditText().getText().toString());
        usuario.setIdade(tilNascimentoUsuario.getEditText().getText().toString());
        usuario.setGenero(Cliente.OUTRO);
        usuario.setPlataforma(Cliente.ANDROID);
        usuario.setImgUrl(imagemSelecionada.toString());
        usuario.setLatitude(currentLatLng.latitude);
        usuario.setLongitude(currentLatLng.longitude);
        usuario.setLocalizacao("São Paulo, SP");

        loginUsuario = false;
        animacaoUsuario(loginUsuario);
        FirebaseUtil.cadastrarUsuario(EscolhaLoginActivity.this, usuario, progressBar, linearCards);
    }

    //Método para logar um Comerciante
    private void loginComerciante() {
        String login = tilLoginComerciante.getEditText().getText().toString(),
                senha = tilSenhaComerciante.getEditText().getText().toString();

        FirebaseUtil.login(EscolhaLoginActivity.this, login, senha, progressBar, linearCards, Comerciante.COMERCIANTES);
    }


    // Método de quando o botão voltar é pressionado
    @Override
    public void onBackPressed() {
        if(loginComerciante)
        {
            loginComerciante = false;
            animacaoComerciante(false);
        }
        else if(loginUsuario){
            loginUsuario = false;
            animacaoUsuario(false);
        }
        else {
            super.onBackPressed();
        }
    }

    //Animações

    //Animação do CardView do Comerciante
    private void animacaoComerciante(final boolean abrir) {

        constraintLayoutComerciante.setEnabled(false);

        int tamanhoConstraintComerciante = AnimacaoUtil.convertDiptoPix(EscolhaLoginActivity.this,TAMANHO_LOGIN_COMERCIANTE);



        if (!abrir) {
            tamanhoConstraintComerciante = AnimacaoUtil.convertDiptoPix(EscolhaLoginActivity.this,TAMANHO_PADRAO_COMERCIANTE);

        }else{
            setTextErroComerciante();
        }

        ValueAnimator anim = ValueAnimator.ofInt(comerciante.getMeasuredHeight(), tamanhoConstraintComerciante);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = comerciante.getLayoutParams();
                layoutParams.height = val;
                comerciante.setLayoutParams(layoutParams);

                int visibility = View.VISIBLE;
                float porcentagem = valueAnimator.getAnimatedFraction();

                if (!abrir) {
                    visibility = View.GONE;

                    if (porcentagem == 1f) {
                        tilLoginComerciante.setVisibility(visibility);
                        tilSenhaComerciante.setVisibility(visibility);
                        txtCadastrarComerciante.setVisibility(visibility);
                        btnEntrarComerciante.setVisibility(visibility);
                    }

                } else {

                    if(porcentagem == 0.0f) {

                        tilLoginComerciante.setVisibility(visibility);
                        tilSenhaComerciante.setVisibility(visibility);
                        txtCadastrarComerciante.setVisibility(visibility);
                        btnEntrarComerciante.setVisibility(visibility);
                    }
                }

                if(porcentagem == 1f)
                    constraintLayoutComerciante.setEnabled(true);
            }
        });
        anim.setDuration(TEMPO_ANIMACAO);
        anim.start();

    }

    //Animação do CardView do Usuario
    private void animacaoUsuario(final boolean abrir){

        constraintLayoutUsuario.setEnabled(false);

        int tempo = TEMPO_ANIMACAO;
        int tamanhoConstraintUsuario = AnimacaoUtil.convertDiptoPix(EscolhaLoginActivity.this,TAMANHO_LOGIN_USUARIO);

        if(!abrir) {
            tamanhoConstraintUsuario = AnimacaoUtil.convertDiptoPix(EscolhaLoginActivity.this,TAMANHO_PADRAO_USUARIO);
        }else {
            if (cadastroUsuario)
                tamanhoConstraintUsuario = AnimacaoUtil.convertDiptoPix(EscolhaLoginActivity.this, TAMANHO_CADASTRO_USUARIO);

            setTextErroUsuario(!cadastroUsuario);
            txtFoto.setTextColor(Color.WHITE);
            txtLocalizacaoUsuario.setTextColor(Color.WHITE);
        }

        ValueAnimator anim = ValueAnimator.ofInt(usuario.getMeasuredHeight(), tamanhoConstraintUsuario);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = usuario.getLayoutParams();
                layoutParams.height = val;
                usuario.setLayoutParams(layoutParams);

                int visibility = View.VISIBLE;
                float porcentagem = valueAnimator.getAnimatedFraction();

                if(!abrir) {
                    visibility = View.GONE;

                    if(porcentagem >= 1){
                        tilLoginUsuario.setVisibility(visibility);
                        tilSenhaUsuario.setVisibility(visibility);
                        txtCadastrarUsuario.setVisibility(visibility);
                        btnEntraUsuario.setVisibility(visibility);
                    }
                    else if(porcentagem == 0.0f)
                        animacaoCamposCadastroUsuario(false, 0);

                }
                else {

                    if(porcentagem == 0.0f) {
                        tilLoginUsuario.setVisibility(visibility);
                        tilSenhaUsuario.setVisibility(visibility);
                        txtCadastrarUsuario.setVisibility(visibility);
                        btnEntraUsuario.setVisibility(visibility);

                        if(cadastroUsuario){
                            animacaoCamposCadastroUsuario(true, TAMANHO_CAMPOS_CADASTRO_USUARIO);
                            txtCadastrarUsuario.setVisibility(View.GONE);
                        }else
                            animacaoCamposCadastroUsuario(false, 0);
                    }
                }

                if(porcentagem == 1f)
                    constraintLayoutUsuario.setEnabled(true);
            }
        });
        anim.setDuration(tempo);
        anim.start();

    }

    //Animação do LinearLayout que contem os campos de cadastro do Comerciante
    private void animacaoCamposCadastroUsuario(boolean abrir , float tamanho){

        if(abrir){
            AnimacaoUtil.alterarTamanhoObjetoHeigth(linearLayoutCamposCadastroUsuario, this, TEMPO_ANIMACAO, tamanho);
        }
        else{
            AnimacaoUtil.encolherTamanhoObjetoHeigth(linearLayoutCamposCadastroUsuario, TEMPO_ANIMACAO);
        }
    }

    //Animação Logo UseFi
    private void animacaoLogo() {
        int valor = AnimacaoUtil.convertDiptoPix(EscolhaLoginActivity.this, 500);
        usuario.animate().x(valor).setDuration(0);
        comerciante.animate().x(valor).setDuration(0);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                //Define a animação de rotação para a borda azul do logo.
                usuario.setVisibility(View.VISIBLE);
                usuario.animate().x(AnimacaoUtil.convertDiptoPix(EscolhaLoginActivity.this, 0)).setDuration(1500);

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        //Animação do Card do comerciante para retornar a tela
                        comerciante.setVisibility(View.VISIBLE);
                        comerciante.animate().x(AnimacaoUtil.convertDiptoPix(EscolhaLoginActivity.this, 0)).setDuration(1500).setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                logo.clearAnimation();
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        });
                    }
                }, 500);
            }
        }, 300);
    }

    //Localização
    @Override
    public void onLocationChanged(Location location) {
        currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        String localizacao = currentLatLng.latitude + "/" + currentLatLng.longitude;

        txtLocalizacaoUsuario.setText(localizacao);
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


    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("GPS desativado!");
        alertDialog.setMessage("Ativar GPS?");
        alertDialog.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        alertDialog.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    private void startGettingLocations() {

        Log.i(TAG, "startGettingLocations");
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isNetwork = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isNetwork) {
            showSettingsAlert();
        } else {
            String[] permissoes = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            Log.i(TAG, "permissionsToRequest");
            //Verificar as Permissões
            if(PermissaoUtil.validarPermissoes(permissoes, this, REQUEST_PERMISSION_LOCATION)){
                finishGettingLocations();
            }else{
                ToastUtil.toastLoad(this, "Recuperando Localização...", true);
            }
        }
    }

    private void finishGettingLocations(){
        Log.i(TAG, "finishGettingLocations");
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;// Distance in meters
        long MIN_TIME_BW_UPDATES = 1000 * 10;// Time in milliseconds

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ToastUtil.toastLoad(this, "É necessário aceitar as permissões!", true);
            return;
        }
        lm.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
    }

}
