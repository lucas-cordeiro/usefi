package loureiro.enzo.usefi.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import loureiro.enzo.usefi.helper.ValidacaoDados;
import loureiro.enzo.usefi.helper.ValidacaoTil;
import loureiro.enzo.usefi.model.Comerciante;
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

public class PerfilActivity extends AppCompatActivity implements LocationListener{

    TextInputLayout tilNomeLoja, tilCNPJComerciante, tilLocal, tilSenha, tilConfirmacaoSenha;
    TextView txtPlano, txtData;
    Button btnSalvar;
    ImageButton imageButtonLogo, imageButtonFoto, imageButtonLocalizacao, btnPlano;
    CircleImageView imageViewLogo;
    ImageView imageViewFoto;
    LinearLayout linearCampos;

    ProgressBar progressBarSalvar, progressBarFoto, progressBarComercio;

    Comerciante comerciante;
    public Date dateAtual;

    private DatabaseReference referenceUser;
    private ValueEventListener eventListenerRecuperarComerciante;

    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private final int REQUEST_PERMISSION_IMGBUTTONLOGO = 100, REQUEST_PERMISSION_IMGBUTTONFOTO = 101, REQUEST_PERMISSION_LOCATION = 102;
    private final int REQUEST_GALERIA_IMGBUTTONLOGO = 200, REQUEST_GALERIA_IMGBUTTONFOTO = 201;

    private final String TAG = "PERFIL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        tilNomeLoja = findViewById(R.id.tilNomeLojaComerciantePerfil);
        tilCNPJComerciante = findViewById(R.id.tilCNPJComerciantePerfil);
        tilLocal = findViewById(R.id.tilLocalComerciantePerfil);
        tilSenha = findViewById(R.id.tilSenhaComerciantePerfil);
        tilConfirmacaoSenha = findViewById(R.id.tilConfirmarSenhaComerciantePerfil);

        txtData = findViewById(R.id.txtDataPlanoPerfil);
        txtPlano = findViewById(R.id.txtPlano);

        btnSalvar = findViewById(R.id.btnSalvarComerciantePerfil);
        btnPlano = findViewById(R.id.btnPlanoPerfil);

        imageButtonLogo = findViewById(R.id.imgButtonLogoPerfil);
        imageButtonFoto = findViewById(R.id.imgButtonFotoComercioPerfil);
        imageButtonLocalizacao = findViewById(R.id.imgButtonLocalizacaoPerfil);

        imageViewLogo = findViewById(R.id.fotoComerciante);
        imageViewFoto = findViewById(R.id.imgFotoComercioPerfil);

        progressBarSalvar = findViewById(R.id.progressBarSalvarPerfil);
        progressBarFoto = findViewById(R.id.progressBarFotoPerfil);
        progressBarComercio = findViewById(R.id.progressBarComercio);

        linearCampos = findViewById(R.id.linearCamposComerciantePerfil);

        //ToolBar [Início]
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        //Toolbar [Fim]

        //Remove foco automático dos EditText
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


        //Máscara para o EditText
        tilCNPJComerciante.getEditText().addTextChangedListener(MaskEditUtil.mask(tilCNPJComerciante.getEditText(), MaskEditUtil.FORMAT_CNPJ));

        //Evento de Clique nos botões
        btnSalvar.setOnClickListener(listenerSalvar);
        btnPlano.setOnClickListener(listenerPlano);
        imageButtonLogo.setOnClickListener(listenerLogo);
        imageButtonFoto.setOnClickListener(listenerFoto);
        imageButtonLocalizacao.setOnClickListener(listenerLocalizacao);

        dateAtual = (Date) getIntent().getSerializableExtra("date");
    }

    private void recuperarComercianteLogado() {
        AnimacaoUtil.progressBar(progressBarFoto, imageViewFoto, true);
        new Thread(){
            @Override
            public void run() {

            FirebaseAuth firebaseAuth = ConfiguracaoFirebase.getFirebaseAuth();
            FirebaseUser user = firebaseAuth.getCurrentUser();
            String id = Base64Util.dadoToBase64(user.getEmail());

            referenceUser = ConfiguracaoFirebase.getReferenceFirebase().child(Comerciante.COMERCIANTES).child(id);

            eventListenerRecuperarComerciante = referenceUser.addValueEventListener( new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    PerfilActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AnimacaoUtil.progressBar(progressBarSalvar, btnSalvar, true);
                        }
                    });

                    comerciante = dataSnapshot.getValue(Comerciante.class);
                    comerciante.setId(dataSnapshot.getKey());

                    if(!comerciante.getDataPagamento().equals(Comerciante.DATA_NULL))
                        FirebaseUtil.verificarPlanoComerciante(comerciante, false, PerfilActivity.this, dateAtual);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Define os dados do Comerciante
                            defineDadosComerciante();
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

    //Atribui os dados do comerciante logado para os campos
    private void defineDadosComerciante() {

        definirErrorText();

        tilNomeLoja.getEditText().setText(comerciante.getNomeLoja());
        tilCNPJComerciante.getEditText().setText("");
        tilCNPJComerciante.getEditText().setText(comerciante.getCNPJ());
        tilLocal.getEditText().setText(comerciante.getLocal());
        tilSenha.getEditText().setText("Password");
        tilConfirmacaoSenha.getEditText().setText("Password");
        if(!comerciante.isPago()) {
            int colorErro = getResources().getColor(R.color.colorError);
            txtPlano.setTextColor(colorErro);
            txtData.setTextColor(colorErro);
        }
        else{
            int color = Color.WHITE;
            txtPlano.setTextColor(color);
            txtData.setTextColor(color);
        }
        txtPlano.setText(comerciante.getPlano());
        txtData.setText(DateUtil.dataFirebaseToDataNormal(comerciante.getDataPagamento()));

        FirebaseUtil.recuperarImagemStorage(this, Uri.parse(comerciante.getUrlLogo()), progressBarFoto, imageViewLogo, imageViewLogo);
        FirebaseUtil.recuperarImagemStorage(this, Uri.parse(comerciante.getUrlFotoComercio()), progressBarComercio, imageViewFoto, imageViewFoto );

        AnimacaoUtil.progressBar(progressBarSalvar, btnSalvar, false);
    }

    private void definirErrorText() {
        int style = R.style.ErrorTextColorAzul;
        tilNomeLoja.setErrorTextAppearance(style);
        tilCNPJComerciante.setErrorTextAppearance(style);
        tilLocal.setErrorTextAppearance(style);
        tilSenha.setErrorTextAppearance(style);
        tilConfirmacaoSenha.setErrorTextAppearance(style);

        tilNomeLoja.setError("Nome do seu comércio");
        tilCNPJComerciante.setError("99.999.999/9999-99");
        tilLocal.setError("Shopping / Centro");
        tilSenha.setError("A senha para a sua conta");
        tilConfirmacaoSenha.setError("Informe novamente a senha");
    }

    //Criação do Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    //Opções do Menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.menu_perfil://Perfil
                //Continua na mesma tela
                break;

            case R.id.menu_sair://Sair
                FirebaseUtil.deslogarConta(PerfilActivity.this);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    //Método para Recebimento de Resultados de Permissões Solicitadas
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            if(REQUEST_PERMISSION_IMGBUTTONFOTO == requestCode || REQUEST_PERMISSION_IMGBUTTONLOGO == requestCode){
                for(int permissaoResultado : grantResults){
                    if(permissaoResultado == PackageManager.PERMISSION_DENIED) {
                        AlertaUtil.alertaErro("Permissão Negada","É necessário aceitar as permissões para uso do Aplicativo!",this);
                    }else {
                        Intent galeria = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        if(galeria.resolveActivity(getPackageManager()) != null) {

                            if (requestCode == REQUEST_PERMISSION_IMGBUTTONLOGO)
                                startActivityForResult(galeria, REQUEST_GALERIA_IMGBUTTONLOGO);
                            else if(requestCode == REQUEST_PERMISSION_IMGBUTTONFOTO)
                                startActivityForResult(galeria, REQUEST_GALERIA_IMGBUTTONFOTO);
                        }
                    }
                }
            }
            else if(REQUEST_PERMISSION_LOCATION == requestCode){
                finishGettingLocations();
            }
    }

    //Método para Recebimento de Resultados desta Activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

            if(requestCode == REQUEST_GALERIA_IMGBUTTONFOTO || requestCode == REQUEST_GALERIA_IMGBUTTONLOGO){
                if(resultCode == RESULT_OK){
                    try{
                        //Configurando Imagem [Início]
                        Bitmap imagem = null;
                        try {
                            Uri localImagem = data.getData();
                            imagem = MediaStore.Images.Media.getBitmap(this.getContentResolver(), localImagem);
                            imageViewLogo.setImageBitmap(imagem);

                            if (requestCode == REQUEST_GALERIA_IMGBUTTONLOGO) {//ResquetCode do ImgButtonLogo da Galeria
                                FirebaseUtil.enviarImagemStorageComerciante(imagem, comerciante, this, progressBarFoto, imageViewLogo, true);
                            }
                            else if(requestCode == REQUEST_GALERIA_IMGBUTTONFOTO) {//ResquetCode do ImgButtonFoto da Galeria
                                FirebaseUtil.enviarFotoStorageComerciante(imagem, comerciante, this, progressBarComercio, imageViewFoto, true);
                            }
                        }
                        catch (Exception e){//Erro na Imagem
                            ToastUtil.toastCustom(this, "Erro Bitmap: "+e.getMessage(), false);
                        }
                        //Configurando Imagem [Fim]
                    }
                    catch (Exception ex){
                        ex.printStackTrace();
                    }

                }else {
                    ToastUtil.toastCustom(this,"É preciso adicionar uma Imagem!", false);
                }
            }
    }

    //Validar os campos do perfil
    private boolean validarCampos() {
        boolean campoInvalido = false;
        ArrayList<Boolean> campos = new ArrayList<>();

        definirErrorText();

        campos.add(ValidacaoTil.verificarCNPJ(tilCNPJComerciante));
        campos.add(ValidacaoTil.verificarTahPreenchido(tilNomeLoja));
        campos.add(ValidacaoTil.verificarTahPreenchido(tilLocal));
        if(!tilSenha.getEditText().getText().toString().equals("Password"))//Caso queira alterar a senha
        {
            campos.add(ValidacaoTil.verificarEhSenhaCadastro(tilSenha));
            campos.add(ValidacaoTil.verificarConfirmacaoSenha(tilSenha, tilConfirmacaoSenha));
        }

        for (Boolean valido:campos) {
            if(!valido){
                campoInvalido = true;
            }
        }


        return !campoInvalido;
    }

    //Evento de clique do botão Salvar
    private View.OnClickListener listenerSalvar = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(validarCampos()) {//Verifica se os dados são válidos
                comerciante.setCNPJ(ValidacaoDados.leCNPJ(tilCNPJComerciante.getEditText().getText().toString()));
                comerciante.setNomeLoja(tilNomeLoja.getEditText().getText().toString());
                comerciante.setLocal(tilLocal.getEditText().getText().toString());

                if(!tilSenha.getEditText().getText().toString().equals("Password"))//Caso queira alterar a senha
                comerciante.setSenha(tilSenha.getEditText().getText().toString());

                FirebaseUtil.atualizarDadosDoComerciante(PerfilActivity.this,  comerciante,false, true, progressBarSalvar, btnSalvar);
            }
        }
    };

    //Evento de clique do imgButtonLogo
    private View.OnClickListener listenerLogo = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //Verificar as Permissões
            if(PermissaoUtil.validarPermissoes(permissoesNecessarias, PerfilActivity.this, REQUEST_PERMISSION_IMGBUTTONLOGO)){

                Intent galeria = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if(galeria.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(galeria, REQUEST_GALERIA_IMGBUTTONLOGO);
                }
            }
        }
    };

    //Evento de clique do imgButtonFoto
    private View.OnClickListener listenerFoto = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ToastUtil.toastCustom(PerfilActivity.this, "Buu", true);

            //Verificar as Permissões
            if(PermissaoUtil.validarPermissoes(permissoesNecessarias, PerfilActivity.this, REQUEST_PERMISSION_IMGBUTTONFOTO)){

                Intent galeria = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if(galeria.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(galeria, REQUEST_GALERIA_IMGBUTTONFOTO);
                }
            }
        }
    };

    private View.OnClickListener listenerLocalizacao = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ToastUtil.toastLoad(PerfilActivity.this, "Carregando Localização...", true);
            startGettingLocations();
        }
    };


    private View.OnClickListener listenerPlano = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if(comerciante.getDataPagamento().equals(Comerciante.DATA_NULL)){//Comerciante ainda não efetivado
                ToastUtil.toastCustom(PerfilActivity.this, "Cadastro ainda não efetivado por nossa equipe. Se tiver alguma dúvida, consulte o suporte.", false);
            }else{
                CharSequence optios[];

                if(comerciante.isPago()) {
                    optios = new CharSequence[]{"Alterar Plano"};
                }
                else{//Caso o plano precise ser pago

                    optios = new CharSequence[]{"Pagar","Alterar Plano"};
                }

                AlertaUtil.alertaErro("Pagamento","Para alterar dados de seu plano, por favor realize essa operação no site. Em caso de dúvida, consulte o suporte", PerfilActivity.this);
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        //Recupera o Comerciante Logado
        recuperarComercianteLogado();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(eventListenerRecuperarComerciante!=null)
            referenceUser.removeEventListener(eventListenerRecuperarComerciante);
    }

    @Override
    protected void onDestroy() {
        if(referenceUser !=null)
            referenceUser.removeEventListener(eventListenerRecuperarComerciante);
        super.onDestroy();
    }


    //Localização
    @Override
    public void onLocationChanged(Location location) {
        comerciante.setLongitude(location.getLongitude());
        comerciante.setLatitude(location.getLatitude());
        FirebaseUtil.atualizarDadosDoComerciante(this, comerciante, true, false, null, null);
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
