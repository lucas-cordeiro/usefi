package loureiro.enzo.usefi.fragment;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.URLUtil;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.List;

import loureiro.enzo.usefi.activity.UsuarioActivity;
import loureiro.enzo.usefi.myInterface.IFragment;
import loureiro.enzo.usefi.model.Comerciante;
import loureiro.enzo.usefi.model.Usuario;
import loureiro.enzo.usefi.model.Wifi;
import loureiro.enzo.usefi.R;
import loureiro.enzo.usefi.util.AnimacaoUtil;
import loureiro.enzo.usefi.util.BancoDadosUtil;
import loureiro.enzo.usefi.util.Base64Util;
import loureiro.enzo.usefi.util.ConfiguracaoFirebase;
import loureiro.enzo.usefi.util.FirebaseUtil;
import loureiro.enzo.usefi.util.ToastUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class ComercianteInfoFragment extends Fragment implements IFragment {

    private ImageView imgBanner;
    private ProgressBar progressBarBanner, progressBarToolbar, progressBarFab;
    private ConstraintLayout constraintLayoutBalao;
    private FloatingActionButton fab;
    public WebView webView;
    public String url;

    private boolean balao = false, home = true;

    private Comerciante comerciante;

    private static final String TAG="COMERCIANTE_INFO_FRAGMENT";

    public ComercianteInfoFragment() {
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
        View view = inflater.inflate(R.layout.fragment_comerciante_info, container, false);

        setToolbarTitle();

        imgBanner = view.findViewById(R.id.imgBannerComercianteInfo);

        constraintLayoutBalao = view.findViewById(R.id.constraintLayoutBalao);

        progressBarToolbar = view.findViewById(R.id.progressBarToolbar);
        progressBarBanner = view.findViewById(R.id.progressBarBannerComercianteInfo);
        progressBarFab = view.findViewById(R.id.progressBarFabComercianteInfo);

        fab = view.findViewById(R.id.fabBaixarDadosRede);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(balao)
                    esconderBalao();

                baixarDadosRede();
            }
        });

        webView = view.findViewById(R.id.webViewBanner);

        atribuirDadosComerciante();

        return view;
    }


    private void baixarDadosRede() {
        AnimacaoUtil.progressBar(progressBarFab, fab, true);

        FirebaseMessaging.getInstance().subscribeToTopic(comerciante.getId().replace("=","")).addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    FirebaseAuth firebaseAuth = ConfiguracaoFirebase.getFirebaseAuth();
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    final String id = Base64Util.dadoToBase64(user.getEmail());
                    DatabaseReference referenceTopico = ConfiguracaoFirebase.getReferenceFirebase().child(Usuario.USUARIOS).child(id).child(Usuario.TOPICOS).child(comerciante.getId().replace("=",""));
                    referenceTopico.setValue(true).addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                               DatabaseReference referenceRedes = ConfiguracaoFirebase.getReferenceFirebase().child(Usuario.USUARIOS).child(id).child(Usuario.REDES_BAIXADAS).child(comerciante.getId());
                               referenceRedes.setValue(true).addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                                   @Override
                                   public void onComplete(@NonNull Task<Void> task) {
                                       if(task.isSuccessful()){
                                           Wifi wifi = new Wifi(comerciante.getWifiNome(),"", comerciante.getWifiSenha(), comerciante.getId());
                                           if(BancoDadosUtil.gravarWifi(getActivity(), wifi))
                                               ToastUtil.toastCustom(getActivity(), "Dados baixados!", true);
                                       }else{
                                           ToastUtil.toastCustom(getActivity(), "Error: "+task.getException().getMessage(), false);
                                       }
                                   }
                               });
                            }
                            else {
                                ToastUtil.toastCustom(getActivity(), "Error: "+task.getException().getMessage(), false);
                            }
                        }
                    });
                }else{
                    ToastUtil.toastCustom(getActivity(), "Error: "+task.getException().getMessage(), false);
                }

                AnimacaoUtil.progressBar(progressBarFab, fab, false);
            }
        });
    }


    private void atribuirDadosComerciante() {
        setToolbarTitle();
    }

    @Override
    public void onStart() {
        super.onStart();
        setToolbarTitle();
        verificarWifisCadastrados();
        carregarWeb();

    }

    private void verificarWifisCadastrados() {
        List<Wifi> wifisCadastrados = BancoDadosUtil.lerWifis(getActivity());

        boolean mostrar = true;
        for(Wifi wifi : wifisCadastrados){
            if(wifi.getIdComerciante().equals(comerciante.getId())){
                mostrar = false;
                break;
            }
        }

        if(wifisCadastrados.size()<1 && mostrar){
            mostrarBalao();
        }
    }

    private void mostrarBalao(){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                balao = true;
                long tempo = 1000;
                constraintLayoutBalao.setVisibility(View.INVISIBLE);
                constraintLayoutBalao.setAnimation(AnimacaoUtil.mostrarView(tempo));
            }
        }, 500);
    }

    private void esconderBalao(){
        balao = false;
        long tempo = 1000;
        constraintLayoutBalao.setAnimation(AnimacaoUtil.desaparecerView(tempo));
    }

    private void carregarWeb(){
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(false);

        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        AnimacaoUtil.progressBar(progressBarBanner, webView, true);
                    }
                }, 500);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                webView.setVisibility(View.INVISIBLE);
                AnimacaoUtil.progressBar(progressBarBanner, webView, false);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if( URLUtil.isNetworkUrl(url) ) {
                    return false;
                }

                AnimacaoUtil.progressBar(progressBarBanner, webView, true);
                if (appInstalledOrNot(url)) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity( intent );
                } else {
                    ToastUtil.toastCustom(getActivity(), "Falha ao tentar abrir link", false);
                }
                AnimacaoUtil.progressBar(progressBarBanner, webView, false);
                return true;
            }
        });
    }

    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = getActivity().getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }

        return false;
    }


    public void setComerciante(Comerciante comerciante) {
        this.comerciante = comerciante;
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
        UsuarioActivity activity = ((UsuarioActivity) getActivity());
        activity.setToolbarTitle("");
        LinearLayout linearLayoutToolbar = activity.getLinearLayoutToolbar();
        ViewGroup.LayoutParams params = linearLayoutToolbar.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        linearLayoutToolbar.setLayoutParams(params);
        activity.getTitleToolbar().setText(comerciante.getNomeLoja());
        activity.getSubTitleToolbar().setText(comerciante.getLocal());
        url = comerciante.getUrlBanner();
        FirebaseUtil.recuperarImagemStorage(getActivity(), Uri.parse(comerciante.getUrlLogo()), progressBarToolbar, activity.getImgToolbar(), activity.getImgToolbar());
    }
}
