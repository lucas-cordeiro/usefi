package loureiro.enzo.usefi.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import loureiro.enzo.usefi.activity.MainActivity;
import loureiro.enzo.usefi.activity.EscolhaLoginActivity;
import loureiro.enzo.usefi.activity.UsuarioActivity;
import loureiro.enzo.usefi.rnum.EnumPlanos;
import loureiro.enzo.usefi.helper.Others;
import loureiro.enzo.usefi.model.Comerciante;
import loureiro.enzo.usefi.model.Notificacao;
import loureiro.enzo.usefi.model.Usuario;
import loureiro.enzo.usefi.R;

public class FirebaseUtil {

    private static final String TAG = "FIREBASE_UTIL";
    private static int tentativas = 0;

    //Verica se há uma conta Logada no aplicativo
    public static boolean verificaContaLogada(){
        Log.d(TAG, "verificaContaLogada");
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        boolean contaLogada = user!=null;
        if(contaLogada) {
            if(user.getEmail()!=null)
            user.reload();
            else
                return false;

            Log.d(TAG, "email: "+user.getEmail());
        }


        return contaLogada;
    }

    public static void login(final Activity activity, final String login, final String senha, final ProgressBar progressBar, final View view, final String tipo) {
        AnimacaoUtil.progressBar(progressBar, view, true);
        new Thread() {
            @Override
            public void run() {
                if (Others.isOnline(activity)) {
                    final FirebaseAuth firebaseAuth = ConfiguracaoFirebase.getFirebaseAuth();
                    firebaseAuth.signInWithEmailAndPassword(login, senha).addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                String id = Base64Util.dadoToBase64(user.getEmail());

                                DatabaseReference referenceUser = ConfiguracaoFirebase.getReferenceFirebase().child(tipo).child(id);
                                referenceUser.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.getValue() != null) {
                                            SharedPreferencesUtil.gravarValor(activity, Usuario.LOGIN, login);
                                            SharedPreferencesUtil.gravarValor(activity, Usuario.SENHA, senha);
                                            if (tipo.equals(Usuario.USUARIOS))
                                                loginUsuarioSucesso(activity, progressBar, view);
                                            else if (tipo.equals(Comerciante.COMERCIANTES))
                                                loginComercianteSucesso(activity);
                                        } else {
                                            falha("Erro ao tentar encontrar usuário!", activity, progressBar, view);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            } else {
                                if (task.getException().getClass().getSimpleName().equals("FirebaseAuthInvalidCredentialsException")) {
                                    falha("Senha Inválida!", activity, progressBar, view);

                                    //Recuperar Senha
                                    tentativas++;
                                    if (tentativas >= 3)
                                        AlertaUtil.alertaSolicitarNovaSenha(activity, login);

                                } else if (task.getException().getClass().getSimpleName().equals("FirebaseAuthInvalidUserException")) {
                                    falha("Login Inválido!", activity, progressBar, view);

                                } else {
                                    falha("Erro: " + task.getException().getClass().getSimpleName(), activity, progressBar, view);
                                }
                            }
                        }
                    });
                } else {
                    if (SharedPreferencesUtil.contemValor(activity, EquipeUseFiUtil.CONTA)) {
                        String conta = SharedPreferencesUtil.lerValor(activity, EquipeUseFiUtil.CONTA);
                        if (conta.equals(Usuario.USUARIOS))
                            loginUsuario(activity, login, senha, progressBar, view);
                    } else {
                        if(tipo.equals(Usuario.USUARIOS))
                        falha("É necessário possuir um login salvo!", activity, progressBar, view);
                        else if(tipo.equals(Comerciante.COMERCIANTES))
                            falha( "Sem Conexão, tente novamente mais tarde!", activity, progressBar, view);
                    }
                }
            }
        }.start();
    }


    //Faz logout de uma conta logada
    public static void deslogarConta(Activity activity) {
        try {
            FirebaseAuth firebaseAuth = ConfiguracaoFirebase.getFirebaseAuth();
            firebaseAuth.signOut();
            SharedPreferencesUtil.excluirValor(activity, EquipeUseFiUtil.CONTA);
            SharedPreferencesUtil.excluirValor(activity,  Usuario.NOME);
            SharedPreferencesUtil.excluirValor(activity,  Usuario.ID);
            SharedPreferencesUtil.excluirValor(activity,  Usuario.GENERO);
            SharedPreferencesUtil.excluirValor(activity,  Usuario.IDADE);
            activity.startActivity(new Intent(activity, EscolhaLoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            activity.finish();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    //Identificar Conta
    public static void identificarConta(final Activity activity, final ProgressBar progressBar, final View view){
        Log.d(TAG, "identificarConta");
        AnimacaoUtil.progressBar(progressBar, view, true);
        String conta = SharedPreferencesUtil.lerValor(activity, EquipeUseFiUtil.CONTA);
        Log.d(TAG, "conta: "+conta);
        if(conta.equals(Usuario.USUARIOS))
            loginUsuario(activity, null, null, progressBar, view);
        else if(conta.equals(Comerciante.COMERCIANTES))
            loginComerciante(activity, null, null, progressBar, view);
    }


    //Comerciante [Início]

    //Logar com um Comerciante
    public static void loginComerciante(final Activity activity, final String login, final String senha, final ProgressBar progressBar, final View view) {

        AnimacaoUtil.progressBar(progressBar, view, true);
        Log.i(TAG, "Login Comerciante");

        new Thread() {
            @Override
            public void run() {
                if (Others.isOnline(activity)) {
                    final FirebaseAuth firebaseAuth = ConfiguracaoFirebase.getFirebaseAuth();
                    if(firebaseAuth.getCurrentUser()!=null){
                        Log.i(TAG, "Login Comerciante Logado");
                        String email = firebaseAuth.getCurrentUser().getEmail();
                        firebaseAuth.signOut();
                        firebaseAuth.signInWithEmailAndPassword(email, SharedPreferencesUtil.lerValor(activity, Comerciante.SENHA)).addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = firebaseAuth.getCurrentUser();

                                    //Recuperando os dados do Comerciante
                                    String id = Base64Util.dadoToBase64(user.getEmail());
                                    DatabaseReference referenceUser = ConfiguracaoFirebase.getReferenceFirebase().child("Comerciantes").child(id);
                                    referenceUser.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            Comerciante comerciante = dataSnapshot.getValue(Comerciante.class);
                                            Log.i(TAG, "Login Comerciante: Encontrado");

                                            activity.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    loginComercianteSucesso(activity);
                                                }
                                            });

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            falha("Erro: " + databaseError.getMessage(), activity, progressBar, view);
                                        }
                                    });
                                } else {//Erro no Login
                                    if (task.getException().getClass().getSimpleName().equals("FirebaseAuthInvalidCredentialsException")) {
                                        falha("Senha Inválida!", activity, progressBar, view);

                                        //Recuperar Senha
                                        tentativas++;
                                        if (tentativas >= 3)
                                            AlertaUtil.alertaSolicitarNovaSenha(activity, login);

                                    } else if (task.getException().getClass().getSimpleName().equals("FirebaseAuthInvalidUserException")) {
                                        falha("Login Inválido!", activity, progressBar, view);

                                    } else {
                                        falha("Erro: " + task.getException().getClass().getSimpleName(), activity, progressBar, view);
                                    }
                                }
                            }
                        });
                    }

                } else {
                    falha("Sem conexão, tente novamente!", activity, progressBar, view);
                }
            }
        }.start();
    }

    //Caso o login do comerciante ocorra com sucesso
    private static void loginComercianteSucesso(Activity activity){
        Intent intent = new Intent(activity, MainActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }

    //Verifica o pagamento do plano de um comerciante
    public static boolean verificarPlanoComerciante(Comerciante comerciante, boolean exibirAlerta, Activity activity, Date date){
        boolean pago;

        if (comerciante.getDataPagamento().equals(Comerciante.DATA_NULL)) {
            if(exibirAlerta)
            AlertaUtil.alertaErro("Cadastro", "O seu cadastro ainda não foi efetuado, nossa equipe entrará em contato por meio do e-mail informado no cadastro. Enquanto isso, o aplicativo terá apenas as funções básicas.", activity);
            comerciante.setPago(false);
            return false;
        } else {

            pago = DateUtil.comparaData(DateUtil.dataNormalToDate(comerciante.getDataPagamento()), date);
            comerciante.setPago(pago);
            //atualizarDadosDoComerciante(activity, comerciante, false, null, null);

            if(exibirAlerta){
                if (pago)
                    ToastUtil.toastCustom(activity, "CNPJ: " + comerciante.getCNPJ() + ", seu plano está em Dia", true);
            }
        }
        return  pago;
    }

    //Atualizar dados do Comerciante
    public static void atualizarDadosDoComerciante(final Activity activity, final Comerciante comerciante, final boolean aviso, final boolean fecharActivity, final ProgressBar progressBar, final View view) {
        if (progressBar != null)
            AnimacaoUtil.progressBar(progressBar, view, true);

        final FirebaseAuth firebaseAuth = ConfiguracaoFirebase.getFirebaseAuth();

        //Senha para validar as edições
        final Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.dialog_password_input_layout);
        TextView title = dialog.findViewById(R.id.txtTitle);
        title.setText("Senha");
        TextView msg = dialog.findViewById(R.id.txtMsg);
        msg.setText("Informe a sua senha atual:");
        final TextInputLayout tilSenha = dialog.findViewById(R.id.tilCampo);
        tilSenha.setHint("Senha");
        Button cancell = dialog.findViewById(R.id.btnCancelarDialog);
        cancell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                AnimacaoUtil.progressBar(progressBar, view, false);
            }
        });

        Button enviar = dialog.findViewById(R.id.btnEnviarDialog);
        enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                String senha = tilSenha.getEditText().getText().toString();
                if(comerciante.getSenha() == null)
                    comerciante.setSenha(senha);

                //Recupera a credencial desta conta para poder realizar alterações dos dados
                AuthCredential credential = EmailAuthProvider.getCredential(comerciante.getLogin(), senha);
                firebaseAuth.getCurrentUser().reauthenticate(credential).addOnFailureListener(activity, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {//Falha na autentificação
                        if (aviso)
                            falha("Erro ao confirmar a sua Conta!\nTente novamente.", activity, progressBar, view);
                    }
                })
                        .addOnSuccessListener(activity, new OnSuccessListener<Void>() {//Sucesso na autentificação
                            @Override
                            public void onSuccess(Void aVoid) {
                                if(aviso)
                                    ToastUtil.toastLoad(activity, "Alterando Dados...", true);

                                new Thread(){
                                    @Override
                                    public void run() {
                                        firebaseAuth.getCurrentUser().updatePassword(comerciante.getSenha()).addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull final Task<Void> task) {

                                                if (task.isSuccessful()) {
                                                    DatabaseReference referenceCNPJCadastrados = ConfiguracaoFirebase.getReferenceFirebase();
                                                    referenceCNPJCadastrados.child("Comerciantes").addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                            boolean cadastrado = false;

                                                            for (DataSnapshot dado : dataSnapshot.getChildren()) {

                                                                Comerciante comercianteDados = dado.getValue(Comerciante.class);

                                                                if(comercianteDados.getCNPJ().equals(comerciante.getCNPJ()) && !comerciante.getLogin().equals(comerciante.getLogin())) {
                                                                    cadastrado = true;
                                                                    break;
                                                                }
                                                            }

                                                            Log.i(TAG, "Depois for, cadastrado: "+cadastrado);

                                                            if(cadastrado) {

                                                                if (aviso)
                                                                    falha("CNPJ já cadastrado!", activity, progressBar, view);

                                                            }else{
                                                                DatabaseReference referenceUser = ConfiguracaoFirebase.getReferenceFirebase().child("Comerciantes").child(comerciante.getId());

                                                                HashMap<String, Object> comercianteMap = new HashMap<>();
                                                                comercianteMap.put("cnpj",comerciante.getCNPJ());
                                                                comercianteMap.put("local", comerciante.getLocal());
                                                                comercianteMap.put("senha", comerciante.getSenha());
                                                                comercianteMap.put("dataPagamento", DateUtil.dataNormalToDataFirebase(comerciante.getDataPagamento()));
                                                                comercianteMap.put("nomeLoja", comerciante.getNomeLoja());
                                                                comercianteMap.put("pago", comerciante.isPago());
                                                                comercianteMap.put("plano", comerciante.getPlano());
                                                                comercianteMap.put("urlLogo",comerciante.getUrlLogo());
                                                                comercianteMap.put("urlFotoComercio", comerciante.getUrlFotoComercio());
                                                                comercianteMap.put("urlBanner", comerciante.getUrlBanner());
                                                                comercianteMap.put("latitude", comerciante.getLatitude());
                                                                comercianteMap.put("longitude", comerciante.getLongitude());

                                                                referenceUser.updateChildren(comercianteMap).addOnFailureListener(activity, new OnFailureListener() {//Erro ao atualizar no Banco de Dados
                                                                    @Override
                                                                    public void onFailure(@NonNull final Exception e) {

                                                                        falha(e.getMessage(), activity, progressBar, view);

                                                                    }
                                                                }).addOnSuccessListener(activity, new OnSuccessListener<Void>() {//Sucesso ao atualizar no Banco de Dados
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {

                                                                        activity.runOnUiThread(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                if (aviso)
                                                                                    ToastUtil.toastCustom(activity, "Sucesso ao Atualizar!", true);
                                                                                AnimacaoUtil.progressBar(progressBar, view, false);
                                                                                if(fecharActivity)
                                                                                    activity.finish();
                                                                            }
                                                                        });
                                                                    }
                                                                });
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                                            if (aviso)
                                                                falha("Erro com sua Solicitação!", activity, progressBar, view);
                                                        }
                                                    });
                                                }
                                                else {
                                                    if (aviso)
                                                        falha("Erro com sua Solicitação!", activity, progressBar, view);

                                                }
                                            }
                                        });
                                    }
                                }.start();
                            }
                        });
            }
        });
        dialog.show();

    }

    //Atualiza o pagamento do comerciante
    public static void atualizarPagamentoDoComerciante(final Activity activity, final Comerciante comerciante, final boolean aviso, final boolean fecharAcitivity, final ProgressBar progressBar, final View view) {
        new Thread(){
            @Override
            public void run() {
                DatabaseReference referenceUser = ConfiguracaoFirebase.getReferenceFirebase().child("Comerciantes").child(comerciante.getId());

                HashMap<String, Object> comercianteMap = new HashMap<>();
                comercianteMap.put("cnpj",comerciante.getCNPJ());
                comercianteMap.put("senha", comerciante.getSenha());
                comercianteMap.put("dataPagamento", DateUtil.dataNormalToDataFirebase(comerciante.getDataPagamento()));

                //Notificações
                EnumPlanos plano = PlanoUtil.stringToEnumPlano(comerciante.getPlano());
                if(plano != EnumPlanos.Basic) {
                    comercianteMap.put("dataNotificacoes", DateUtil.dataNormalToDataFirebase(DateUtil.dataDateToDataNormal(Others.getNTPDate())));

                    int notificacoes;
                    if(plano == EnumPlanos.Standard)
                        notificacoes = PlanoUtil.PUSH_STANDARD;
                    else
                        notificacoes = PlanoUtil.PUSH_PREMIUM;

                    comercianteMap.put("notificacoes", notificacoes);

                }
                comercianteMap.put("nomeLoja", comerciante.getNomeLoja());
                comercianteMap.put("pago", comerciante.isPago());
                comercianteMap.put("plano", comerciante.getPlano());
                comercianteMap.put("urlLogo",comerciante.getUrlLogo());

                referenceUser.updateChildren(comercianteMap).addOnFailureListener(activity, new OnFailureListener() {//Erro ao atualizar no Banco de Dados
                    @Override
                    public void onFailure(@NonNull final Exception e) {

                        falha(e.getMessage(), activity, progressBar, view);

                    }
                }).addOnSuccessListener(activity, new OnSuccessListener<Void>() {//Sucesso ao atualizar no Banco de Dados
                    @Override
                    public void onSuccess(Void aVoid) {

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (aviso)
                                    ToastUtil.toastCustom(activity, "Sucesso ao Atualizar!", true);
                                AnimacaoUtil.progressBar(progressBar, view, false);
                                if(fecharAcitivity)
                                    activity.finish();
                            }
                        });
                    }
                });
            }
        }.start();
    }

    //Envia a logo de um Comerciante para o Storage
    public static void enviarImagemStorageComerciante(final Bitmap imagem, final Comerciante comerciante, final Activity activity, final ProgressBar progressBar, final View view, boolean load){

        if(load)
            ToastUtil.toastLoad(activity, "Enviando imagem...", false);
        new Thread() {
            @Override
            public void run() {
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.PNG, 70, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    final StorageReference referenceImagem = ConfiguracaoFirebase.getStorageReference().child("Comerciantes").child(comerciante.getId()).child("logo.png");

                    UploadTask uploadTask = referenceImagem.putBytes(dadosImagem);
                    uploadTask.addOnFailureListener(activity, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            falha("Não foi possível enviar a Logo!\nErro Storage: " +e.getMessage(), activity, progressBar, view);

                        }
                    }).addOnSuccessListener(activity, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            referenceImagem.getDownloadUrl().addOnFailureListener(activity, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    falha("Não foi possível enviar a Logo!\nErro Storage: " +e.getMessage(), activity, progressBar, view);
                                }
                            }).addOnSuccessListener(activity, new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    Uri url = uri;
                                    comerciante.setUrlLogo(url.toString());
                                    atualizarDadosDoComerciante(activity, comerciante,true,false, progressBar, view);
                                    if(progressBar!=null) {
                                        AnimacaoUtil.progressBar(progressBar, view, false);
                                    };
                                }
                            });

                        }
                    });

                } catch (Exception ex) {
                    falha(ex.getMessage(), activity, progressBar, view);
                }
            }
        }.start();
    }

    //Envia a foto de um Comerciante para o Storage
    public static void enviarFotoStorageComerciante(final Bitmap imagem, final Comerciante comerciante, final Activity activity, final ProgressBar progressBar, final View view, boolean load){

        if(load)
            ToastUtil.toastLoad(activity, "Enviando imagem...", false);
        new Thread() {
            @Override
            public void run() {
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.PNG, 70, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    final StorageReference referenceImagem = ConfiguracaoFirebase.getStorageReference().child("Comerciantes").child(comerciante.getId()).child("comercio.png");

                    UploadTask uploadTask = referenceImagem.putBytes(dadosImagem);
                    uploadTask.addOnFailureListener(activity, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            falha("Não foi possível enviar a Foto!\nErro Storage: " +e.getMessage(), activity, progressBar, view);

                        }
                    }).addOnSuccessListener(activity, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            referenceImagem.getDownloadUrl().addOnFailureListener(activity, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    falha("Não foi possível enviar a Foto!\nErro Storage: " +e.getMessage(), activity, progressBar, view);
                                }
                            }).addOnSuccessListener(activity, new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    Uri url = uri;
                                    comerciante.setUrlFotoComercio(url.toString());
                                    atualizarDadosDoComerciante(activity, comerciante,true,false, progressBar, view);
                                    if(progressBar!=null) {
                                        AnimacaoUtil.progressBar(progressBar, view, false);
                                    };
                                }
                            });

                        }
                    });

                } catch (Exception ex) {
                    falha(ex.getMessage(), activity, progressBar, view);
                }
            }
        }.start();
    }

    //Envia um Banner de um Comerciante para o Storage
    public static void enviarBannerStorageComerciante(final Bitmap imagem, final Comerciante comerciante, final Activity activity, final ProgressBar progressBar, final View view, boolean load){
        AnimacaoUtil.progressBar(progressBar, view, true);
        if(load)
            ToastUtil.toastLoad(activity, "Enviando banner...", false);
        new Thread() {
            @Override
            public void run() {
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.PNG, 70, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    final StorageReference referenceImagem = ConfiguracaoFirebase.getStorageReference().child("Comerciantes").child(comerciante.getId()).child("banner.png");

                    UploadTask uploadTask = referenceImagem.putBytes(dadosImagem);
                    uploadTask.addOnFailureListener(activity, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            falha("Não foi possível enviar o Banner!\nErro Storage: " +e.getMessage(), activity, progressBar, view);

                        }
                    }).addOnSuccessListener(activity, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            referenceImagem.getDownloadUrl().addOnFailureListener(activity, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    falha("Não foi possível enviar o Banner!\nErro Storage: " +e.getMessage(), activity, progressBar, view);
                                }
                            }).addOnSuccessListener(activity, new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    Uri url = uri;
                                    comerciante.setUrlBanner(url.toString());
                                    atualizarDadosDoComerciante(activity, comerciante,true,false, progressBar, view);
                                }
                            });

                        }
                    });

                } catch (Exception ex) {
                    falha(ex.getMessage(), activity, progressBar, view);
                }
            }
        }.start();
    }

    //Enviar Notificação Comerciante
    public static void enviarNotificacao(final Activity activity, final Notificacao notificacao, final Date date, final ProgressBar progressBar, final View view){

        AnimacaoUtil.progressBar(progressBar, view, true);

        //Senha para validar as edições
        final Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.dialog_password_input_layout);
        TextView title = dialog.findViewById(R.id.txtTitle);
        title.setText("Senha");
        TextView msg = dialog.findViewById(R.id.txtMsg);
        msg.setText("Informe a sua senha atual:");
        final TextInputLayout tilSenha = dialog.findViewById(R.id.tilCampo);
        tilSenha.setHint("Senha");
        Button cancell = dialog.findViewById(R.id.btnCancelarDialog);
        cancell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setCancelable(false);

        Button enviar = dialog.findViewById(R.id.btnEnviarDialog);
        enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                FirebaseAuth firebaseAuth = ConfiguracaoFirebase.getFirebaseAuth();
                FirebaseUser user  = firebaseAuth.getCurrentUser();
                final String id = Base64Util.dadoToBase64(user.getEmail());
                String senha = tilSenha.getEditText().getText().toString();

                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), senha);
                firebaseAuth.getCurrentUser().reauthenticate(credential).addOnFailureListener(activity, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        falha("Erro ao confirmar a sua Conta!\nTente novamente.", activity, progressBar, view);
                    }
                }).addOnSuccessListener(activity, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        new Thread(){
                            @Override
                            public void run() {

                                final DatabaseReference referenceUser = ConfiguracaoFirebase.getReferenceFirebase().child("Comerciantes").child(id);
                                referenceUser.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        final Comerciante comerciante = dataSnapshot.getValue(Comerciante.class);
                                        comerciante.setId(dataSnapshot.getKey());
                                        Log.i(TAG, "Login Comerciante: Encontrado");

                                        EnumPlanos plano = PlanoUtil.stringToEnumPlano(comerciante.getPlano());
                                        if(plano == EnumPlanos.Basic){
                                            falha("Seu plano não possui a função de Notificações!", activity, progressBar, view);
                                        }else {
                                            comerciante.setPago(verificarPlanoComerciante(comerciante, false, null, date));
                                            if(comerciante.isPago()){
                                                Date dateNotificacoes = DateUtil.dataNormalToDate(comerciante.getDataNotificacoes());

                                                Calendar calendar = Calendar.getInstance();
                                                calendar.setTime(dateNotificacoes);
                                                calendar.add(Calendar.DAY_OF_MONTH, 7 );

                                                dateNotificacoes = calendar.getTime();
                                                if(!DateUtil.comparaData(dateNotificacoes)){

                                                    comerciante.setDataNotificacoes(DateUtil.dataDateToDataNormal(calendar.getTime()));

                                                    if(plano == EnumPlanos.Standard)
                                                        comerciante.setNotificacoes(PlanoUtil.PUSH_STANDARD);
                                                    else
                                                        comerciante.setNotificacoes(PlanoUtil.PUSH_PREMIUM);
                                                }

                                                comerciante.setNotificacoes(comerciante.getNotificacoes()-1);

                                                if(comerciante.getNotificacoes()>=0){

                                                    if(notificacao.getUrlImagen()!=null) {//Enviar a imagem
                                                        try {
                                                            Bitmap imagem = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), Uri.parse(notificacao.getUrlImagen()));

                                                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                                            imagem.compress(Bitmap.CompressFormat.PNG, 70, baos);
                                                            byte[] dadosImagem = baos.toByteArray();

                                                            DatabaseReference reference = ConfiguracaoFirebase.getReferenceFirebase().push();
                                                            String[] push = reference.toString().split("/");
                                                            String id = push[push.length-1];

                                                            final StorageReference referenceImagem = ConfiguracaoFirebase.getStorageReference().child("Comerciantes").child(comerciante.getId()).child(id+".png");

                                                            UploadTask uploadTask = referenceImagem.putBytes(dadosImagem);
                                                            uploadTask.addOnFailureListener(activity, new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    falha("Não foi possível enviar a Imagem!\nErro Storage: " + e.getMessage(), activity, progressBar, view);

                                                                }
                                                            }).addOnSuccessListener(activity, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                                @Override
                                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                                                    referenceImagem.getDownloadUrl().addOnFailureListener(activity, new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            falha("Não foi possível enviar a Imagem!\nErro Storage: " + e.getMessage(), activity, progressBar, view);
                                                                        }
                                                                    }).addOnSuccessListener(activity, new OnSuccessListener<Uri>() {
                                                                        @Override
                                                                        public void onSuccess(Uri uri) {

                                                                            Uri url = uri;
                                                                            notificacao.setUrlImagen(url.toString());

                                                                            //Enviar a Nova notificação
                                                                            enviarDadosNotificacao(activity, comerciante, notificacao, progressBar, view);
                                                                        }
                                                                    });

                                                                }
                                                            });

                                                        } catch (IOException e) {
                                                            e.printStackTrace();
                                                            falha("Erro Bitmap: " + e.getMessage(), activity, progressBar, view);
                                                        }

                                                    }else{//Notificação sem imagem
                                                        notificacao.setUrlImagen("false");

                                                        //Enviar a Nova notificação
                                                        enviarDadosNotificacao(activity, comerciante, notificacao, progressBar, view);
                                                    }

                                                }else {
                                                    falha("Limite de notificações semanais atingido!", activity, progressBar, view);
                                                }

                                            }
                                            else{
                                                falha("Efetue o pagamento do plano para poder enviar notificações!",activity, progressBar, view);
                                            }
                                        }

                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        falha("Erro: " + databaseError.getMessage(), activity, progressBar, view);
                                    }
                                });
                            }
                        }.start();
                    }
                });

                dialog.dismiss();
            }
        });
        dialog.show();

    }

    //Enviar os dados para o Firebase da Nova notificação
    private static void enviarDadosNotificacao(final Activity activity, final Comerciante comerciante, Notificacao notificacao, final ProgressBar progressBar, final View view){

        notificacao.setMensagem(notificacao.getMensagem().replace("\\","'\'"));
        notificacao.setComercianteId(notificacao.getComercianteId().replace("=",""));
        final DatabaseReference referenceUser = ConfiguracaoFirebase.getReferenceFirebase().child("Comerciantes").child(comerciante.getId());
        referenceUser.child("Notificacao").setValue(notificacao).addOnSuccessListener(activity, new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                HashMap<String, Object> comercianteMap = new HashMap<>();
                comercianteMap.put("notificacoes",comerciante.getNotificacoes());
                comercianteMap.put("dataNotificacoes", comerciante.getDataNotificacoes());

                //Atualizar Notificações comerciante
                referenceUser.updateChildren(comercianteMap).addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull final Task<Void> task) {

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (task.isSuccessful())
                                    ToastUtil.toastCustom(activity, "Sucesso ao Enviar Notificação!", true);
                                else
                                    falha(task.getException().getMessage(), activity, progressBar, view);
                                AnimacaoUtil.progressBar(progressBar, view, false);
                            }
                        });
                    }
                });
            }
        }).addOnFailureListener(activity, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                falha(e.getMessage(), activity, progressBar, view);
            }
        });
    }

    //Comerciante [Fim]


    //Usuário [Início]
    //Cadastrar um Usuario
    public static void cadastrarUsuario(final Activity activity, final Usuario usuario, final ProgressBar progressBar, final View view){
        AnimacaoUtil.progressBar(progressBar,view,true);
        new Thread(){
            @Override
            public void run() {
                if(Others.isOnline(activity)){
                    final FirebaseAuth firebaseAuth = ConfiguracaoFirebase.getFirebaseAuth();

                    firebaseAuth.createUserWithEmailAndPassword(usuario.getLogin(), usuario.getSenha()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull final Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                final FirebaseUser user = firebaseAuth.getCurrentUser();
                                final String id = Base64Util.dadoToBase64(usuario.getLogin());
                                usuario.setId(id);

                                try {
                                    Bitmap imagem = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), Uri.parse(usuario.getImgUrl()));

                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    imagem.compress(Bitmap.CompressFormat.PNG, 70, baos);
                                    byte[] dadosImagem = baos.toByteArray();

                                    final StorageReference referenceImagem = ConfiguracaoFirebase.getStorageReference().child("Usuarios").child(usuario.getId()).child("perfil.png");
                                    UploadTask uploadTask = referenceImagem.putBytes(dadosImagem);
                                    uploadTask.addOnFailureListener(activity, new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            falha("Erro: "+e.getMessage(), activity, progressBar, view);
                                            user.delete();
                                        }
                                    }).addOnSuccessListener(activity, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            referenceImagem.getDownloadUrl().addOnFailureListener(activity, new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    falha("Erro: "+e.getMessage(), activity, progressBar, view);
                                                    user.delete();
                                                }
                                            }).addOnSuccessListener(activity, new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    usuario.setImgUrl(uri.toString());
                                                    usuario.setIdade(DateUtil.dataNormalToDataFirebase(usuario.getIdade()));

                                                    DatabaseReference referenceUser = ConfiguracaoFirebase.getReferenceFirebase().child("Usuarios").child(id);
                                                    referenceUser.setValue(usuario).addOnFailureListener(activity, new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            falha("Erro: "+e.getMessage(), activity, progressBar, view);
                                                            firebaseAuth.getCurrentUser().delete();
                                                        }
                                                    }).addOnSuccessListener(activity, new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            firebaseAuth.signOut();
                                                            ToastUtil.toastCustom(activity,"Sucesso ao criar conta!", true);
                                                            AnimacaoUtil.progressBar(progressBar,view,false);
                                                            cadastrarUsuarioSucesso(activity);
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });

                                } catch (IOException e) {
                                    e.printStackTrace();
                                    falha("Erro: "+e.getMessage(), activity, progressBar, view);
                                    user.delete();
                                }

                            } else {

                                if (task.getException().getClass().getSimpleName().equals("FirebaseAuthUserCollisionException"))
                                    falha("Já há uma conta com este E-mail!\nPor favor, realize o Login", activity, progressBar, view);

                                else
                                    falha("Erro: " + task.getException().getClass().getSimpleName() + "\nMensagem: " + task.getException().getMessage(), activity, progressBar, view);
                            }

                        }
                    });
                }else {

                    falha("Sem conexão, tente novamente", activity, progressBar, view);
                }
            }
        }.start();
    }

    //Sucesso ao cadastrar um Usuário
    private static void cadastrarUsuarioSucesso(final Activity activity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.startActivity(new Intent(activity, EscolhaLoginActivity.class));
                activity.finish();
            }
        });
    }

    //Logar com um Usuario
    public static void loginUsuario(final Activity activity, final String login, final String senha, final ProgressBar progressBar, final View view) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AnimacaoUtil.progressBar(progressBar, view, true);
            }
        });

        new Thread() {
            @Override
            public void run() {
                if (Others.isOnline(activity)) {
                    final FirebaseAuth firebaseAuth = ConfiguracaoFirebase.getFirebaseAuth();
                    final String email = firebaseAuth.getCurrentUser().getEmail();
                    firebaseAuth.signInWithEmailAndPassword(email, SharedPreferencesUtil.lerValor(activity, Usuario.SENHA)).addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                loginUsuarioSucesso(activity, progressBar, view);
                            } else {
                                ToastUtil.toastCustom(activity, "Falha ao tentar realizar login", false);
                                firebaseAuth.signOut();
                            }
                        }
                    });

                } else {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (SharedPreferencesUtil.contemValor(activity, Usuario.LOGIN)) {
                                boolean logar = true;
                                if(SharedPreferencesUtil.contemValor(activity, Usuario.MANTER_LOGADO)){
                                    logar = SharedPreferencesUtil.lerValorBoolean(activity, Usuario.MANTER_LOGADO);
                                }else{
                                    SharedPreferencesUtil.gravarValor(activity,  Usuario.MANTER_LOGADO, true);
                                }

                                if(logar){
                                    AnimacaoUtil.progressBar(progressBar, view, false);
                                    activity.startActivity(new Intent(activity, UsuarioActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                    activity.finish();
                                }else{
                                    String loginCorreto = SharedPreferencesUtil.lerValor(activity, Usuario.LOGIN);
                                    String senhaCorreta = SharedPreferencesUtil.lerValor(activity, Usuario.SENHA);
                                    if (login.equals(loginCorreto) && senha.equals(senhaCorreta)) {
                                        AnimacaoUtil.progressBar(progressBar, view, false);
                                        activity.startActivity(new Intent(activity, UsuarioActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                        activity.finish();
                                    } else {
                                        ToastUtil.toastCustom(activity, "Dados Inválidos!", false);
                                    }
                                }
                            } else {
                                ToastUtil.toastCustom(activity, "Login ainda não efetuado. Sem conexão com a Internet!", false);
                            }

                            AnimacaoUtil.progressBar(progressBar, view, false);
                        }
                    });
                }
            }
        }.start();
    }

    //Sucesso logar com Usuario
    private static void loginUsuarioSucesso(final Activity activity, final ProgressBar progressBar, final View view){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.startActivity(new Intent(activity, UsuarioActivity.class));
                activity.finish();
            }
        });
    }

    //Atualizar os dados do Usuario
    public  static  void atualizarDadosUsuario(final Activity activity, final Fragment fragment, final Usuario usuario, final boolean aviso, final ProgressBar progressBar, final View view[]){
        if(fragment!=null && fragment.isVisible() && !((UsuarioActivity)activity).verificaFragment(fragment)){
            AnimacaoUtil.progressBar(progressBar, view, true);

            final FirebaseAuth firebaseAuth = ConfiguracaoFirebase.getFirebaseAuth();

            //Senha para validar as edições
            final Dialog dialog = new Dialog(activity);
            dialog.setContentView(R.layout.dialog_password_input_layout);
            dialog.setCancelable(false);
            TextView title = dialog.findViewById(R.id.txtTitle);
            title.setText("Senha");
            TextView msg = dialog.findViewById(R.id.txtMsg);
            msg.setText("Informe a sua senha atual:");
            final TextInputLayout tilSenha = dialog.findViewById(R.id.tilCampo);
            tilSenha.setHint("Senha");
            Button cancell = dialog.findViewById(R.id.btnCancelarDialog);
            cancell.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AnimacaoUtil.progressBar(progressBar, view, false);
                    dialog.dismiss();
                }
            });
            Button enviar = dialog.findViewById(R.id.btnEnviarDialog);
            enviar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    dialog.dismiss();
                    String senha = tilSenha.getEditText().getText().toString();

                    if(usuario.getSenha()==null)
                        usuario.setSenha(senha);

                    if(!((UsuarioActivity)activity).verificaFragment(fragment)){
                        //Recupera a credencial desta conta para poder realizar alterações dos dados
                        AuthCredential credential = EmailAuthProvider.getCredential(usuario.getLogin(), senha);
                        firebaseAuth.getCurrentUser().reauthenticate(credential).addOnFailureListener(activity, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                if (aviso)
                                    falha("Erro ao confirmar a sua Conta!\nTente novamente.", activity, progressBar, view);
                            }
                        }).addOnSuccessListener(activity, new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                new Thread(){
                                    @Override
                                    public void run() {

                                        if(fragment!=null && fragment.isVisible() && !((UsuarioActivity)activity).verificaFragment(fragment)){
                                           firebaseAuth.getCurrentUser().updatePassword(usuario.getSenha()).addOnFailureListener(activity, new OnFailureListener() {
                                               @Override
                                               public void onFailure(@NonNull Exception e) {
                                                   if (aviso)
                                                       falha("Erro com sua Solicitação!", activity, progressBar, view);
                                               }
                                           }).addOnSuccessListener(activity, new OnSuccessListener<Void>() {
                                               @Override
                                               public void onSuccess(Void aVoid) {
                                                   if(usuario.getImgUrl()!=null){
                                                       try {
                                                           Bitmap imagem = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), Uri.parse(usuario.getImgUrl()));
                                                           ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                                           imagem.compress(Bitmap.CompressFormat.PNG, 70, baos);
                                                           byte[] dadosImagem = baos.toByteArray();

                                                           final StorageReference referenceImagem = ConfiguracaoFirebase.getStorageReference().child(Usuario.USUARIOS).child(usuario.getId()).child("perfil.png");
                                                           final UploadTask uploadTask = referenceImagem.putBytes(dadosImagem);
                                                           uploadTask.addOnFailureListener(activity, new OnFailureListener() {
                                                               @Override
                                                               public void onFailure(@NonNull Exception e) {
                                                                   falha("Error: "+e.getMessage(), activity, progressBar, view);
                                                               }
                                                           }).addOnSuccessListener(activity, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                               @Override
                                                               public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                                   referenceImagem.getDownloadUrl().addOnFailureListener(activity, new OnFailureListener() {
                                                                       @Override
                                                                       public void onFailure(@NonNull Exception e) {
                                                                           falha("Error: "+e.getMessage(), activity, progressBar, view);
                                                                       }
                                                                   }).addOnSuccessListener(activity, new OnSuccessListener<Uri>() {
                                                                       @Override
                                                                       public void onSuccess(Uri uri) {
                                                                           usuario.setImgUrl(uri.toString());
                                                                           salvarDadosUsuario(activity, fragment, usuario,progressBar, view);
                                                                       }
                                                                   });
                                                               }
                                                           });

                                                       } catch (IOException e) {
                                                           e.printStackTrace();
                                                       }
                                                   }else{
                                                       salvarDadosUsuario(activity, fragment, usuario, progressBar, view);
                                                   }
                                               }
                                           });
                                       }
                                    }
                                }.start();
                            }
                        });
                    }
                }
            });

            dialog.show();
        }
    }

    private static void salvarDadosUsuario(final Activity activity, final Fragment fragment, Usuario usuario, final ProgressBar progressBar, final View[] view){
        Log.i(TAG, "salvarDadosUsuario");

        DatabaseReference referenceUser = ConfiguracaoFirebase.getReferenceFirebase().child("Usuarios").child(usuario.getId());
        HashMap<String, Object> usuarioMap = new HashMap<>();
        usuarioMap.put("nome",  usuario.getNome());
        usuarioMap.put("idade", DateUtil.dataNormalToDataFirebase(usuario.getIdade()));
        usuarioMap.put("genero",usuario.getGenero());
        if(usuario.getLatitude()>0)
            usuarioMap.put("latitude", usuario.getLatitude());
        if(usuario.getLongitude()>0)
            usuarioMap.put("longitude", usuario.getLongitude());
        if(usuario.getImgUrl()!=null)
            usuarioMap.put("imgUrl", usuario.getImgUrl());
        if(usuario.getLocalizacao()!=null)
            usuarioMap.put("localizacao",usuario.getLocalizacao());

        if(fragment!=null && fragment.isVisible() && !((UsuarioActivity)activity).verificaFragment(fragment)){
            referenceUser.updateChildren(usuarioMap).addOnFailureListener(activity, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if(fragment!=null && fragment.isVisible() && !((UsuarioActivity)activity).verificaFragment(fragment))
                        falha("Erro com sua Solicitação!", activity, progressBar, view);
                }
            }).addOnSuccessListener(activity, new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    if(fragment!=null && fragment.isVisible() &&!((UsuarioActivity)activity).verificaFragment(fragment))
                        ToastUtil.toastCustom(activity, "Sucesso ao Atualizar!", true);
                    if(fragment!=null && fragment.isVisible() && !((UsuarioActivity)activity).verificaFragment(fragment)){
                        ((UsuarioActivity) activity).recuperarUsuario();
                        ((UsuarioActivity) activity).setRecarregar(true);
                    }
                    AnimacaoUtil.progressBar(progressBar, view, false);
                }
            });
        }
    }

    public static void atualizarLocalizacao(final FragmentActivity activity, final Fragment fragment, final Usuario usuario, final ProgressBar progressBar, final View[] view) {
        AnimacaoUtil.progressBar(progressBar, view, true);
        if(fragment!=null && fragment.isVisible() && !((UsuarioActivity)activity).verificaFragment(fragment)){
            final FirebaseAuth firebaseAuth = ConfiguracaoFirebase.getFirebaseAuth();

            //Senha para validar as edições
            final Dialog dialog = new Dialog(activity);
            dialog.setContentView(R.layout.dialog_password_input_layout);
            dialog.setCancelable(false);
            TextView title = dialog.findViewById(R.id.txtTitle);
            title.setText("Senha");
            TextView msg = dialog.findViewById(R.id.txtMsg);
            msg.setText("Informe a sua senha atual:");
            final TextInputLayout tilSenha = dialog.findViewById(R.id.tilCampo);
            tilSenha.setHint("Senha");
            Button cancell = dialog.findViewById(R.id.btnCancelarDialog);
            cancell.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AnimacaoUtil.progressBar(progressBar, view, false);
                    dialog.dismiss();
                }
            });
            Button enviar = dialog.findViewById(R.id.btnEnviarDialog);
            enviar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    String senha = tilSenha.getEditText().getText().toString();

                    if (usuario.getSenha() == null)
                        usuario.setSenha(senha);

                    if(fragment!=null && fragment.isVisible() && !((UsuarioActivity)activity).verificaFragment(fragment)){
                        //Recupera a credencial desta conta para poder realizar alterações dos dados
                        AuthCredential credential = EmailAuthProvider.getCredential(usuario.getLogin(), senha);
                        firebaseAuth.getCurrentUser().reauthenticate(credential).addOnFailureListener(activity, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                falha("Erro ao confirmar a sua Conta!\nTente novamente.", activity, progressBar, view);
                            }
                        }).addOnSuccessListener(activity, new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                new Thread() {
                                    @Override
                                    public void run() {

                                        firebaseAuth.getCurrentUser().updatePassword(usuario.getSenha()).addOnFailureListener(activity, new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                falha("Erro com sua Solicitação!", activity, progressBar, view);
                                            }
                                        }).addOnSuccessListener(activity, new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.i(TAG, "atualizarLocalizacao");

                                                DatabaseReference referenceUser = ConfiguracaoFirebase.getReferenceFirebase().child("Usuarios").child(usuario.getId());
                                                HashMap<String, Object> usuarioMap = new HashMap<>();
                                                if (usuario.getLatitude() != 0)
                                                    usuarioMap.put("latitude", usuario.getLatitude());

                                                if (usuario.getLongitude() != 0)
                                                    usuarioMap.put("longitude", usuario.getLongitude());

                                                referenceUser.updateChildren(usuarioMap).addOnFailureListener(activity, new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        falha("Erro com sua Solicitação!", activity, progressBar, view);
                                                    }
                                                }).addOnSuccessListener(activity, new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        ToastUtil.toastCustom(activity, "Sucesso ao Atualizar!", true);
                                                        AnimacaoUtil.progressBar(progressBar, view, false);
                                                    }
                                                });
                                            }
                                        });
                                    }
                                }.start();
                            }
                        });
                    }
                }
            });
            dialog.show();
        }
    }


    //Usuário [Fim]


    //Recupera uma imagem do Storage
    public static void recuperarImagemStorage(final Activity activity, Uri url, final ProgressBar progressBar, final View view, ImageView imageView){
        AnimacaoUtil.progressBar(progressBar, view, true);
        try {
            Glide.with(activity)
                    .load(url)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {//Erro
                            AnimacaoUtil.progressBar(progressBar, view, false);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {//Sucesso
                            AnimacaoUtil.progressBar(progressBar, view, false);
                            return false;
                        }
                    })
                    .into(imageView);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    //Falha firebase
    private static void falha(final String msg, final Activity activity, final ProgressBar progressBar, final View view){
       activity.runOnUiThread(new Runnable() {
           @Override
           public void run() {
               ToastUtil.toastCustom(activity, msg, false);
               Log.i(TAG, "Erro: " + msg);
               AnimacaoUtil.progressBar(progressBar, view, false);
           }
       });
    }

    private static void falha(final String msg, final Activity activity, final ProgressBar progressBar, final View[] view){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtil.toastCustom(activity, msg, false);
                Log.i(TAG, "Erro: " + msg);
                AnimacaoUtil.progressBar(progressBar, view, false);
            }
        });
    }


}
