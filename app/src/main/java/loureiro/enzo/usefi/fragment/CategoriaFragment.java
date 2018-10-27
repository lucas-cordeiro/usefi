package loureiro.enzo.usefi.fragment;


import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import loureiro.enzo.usefi.activity.UsuarioActivity;
import loureiro.enzo.usefi.adapter.AdapterComerciantes;
import loureiro.enzo.usefi.adapter.AdapterWifis;
import loureiro.enzo.usefi.broadcast.ConexaoWifiBroadcast;
import loureiro.enzo.usefi.broadcast.VerificarWifiOff;
import loureiro.enzo.usefi.clickListener.RecyclerItemClickListener;
import loureiro.enzo.usefi.helper.Others;
import loureiro.enzo.usefi.model.Categoria;
import loureiro.enzo.usefi.model.Comerciante;
import loureiro.enzo.usefi.model.Conexao;
import loureiro.enzo.usefi.model.Pagina;
import loureiro.enzo.usefi.model.PositionRecyclerView;
import loureiro.enzo.usefi.model.Usuario;
import loureiro.enzo.usefi.R;
import loureiro.enzo.usefi.model.Wifi;
import loureiro.enzo.usefi.myInterface.IFragment;
import loureiro.enzo.usefi.service.ConexaoWifi;
import loureiro.enzo.usefi.service.JobConexaoWifi;
import loureiro.enzo.usefi.util.AlarmUtil;
import loureiro.enzo.usefi.util.AlertaUtil;
import loureiro.enzo.usefi.util.AnimacaoUtil;
import loureiro.enzo.usefi.util.BancoDadosUtil;
import loureiro.enzo.usefi.util.Base64Util;
import loureiro.enzo.usefi.util.ConfiguracaoFirebase;
import loureiro.enzo.usefi.util.DateUtil;
import loureiro.enzo.usefi.util.PermissaoUtil;
import loureiro.enzo.usefi.util.SharedPreferencesUtil;
import loureiro.enzo.usefi.util.ToastUtil;

import static loureiro.enzo.usefi.service.JobConexaoWifi.DATA_ULTIMA_CONEXAO;

/**
 * A simple {@link Fragment} subclass.
 */
public class CategoriaFragment extends Fragment implements IFragment, LocationListener {

    private static final long TEMPO = 400;
    private RecyclerView recyclerView;
    public int currentVisiblePosition;
    private double raio;
    private Usuario usuario;
    private List<Comerciante> comerciantesOrdem = new ArrayList<>();
    private List<Long> comerciantesConexoes = new ArrayList<>();
    private List<Double> comerciantesDistancias = new ArrayList<>();
    private AdapterComerciantes adapter;
    public Categoria categoria;

    private DatabaseReference referenceComercios;
    private ValueEventListener valueEventListenerComercios;

    private int pagina;
    private FloatingActionButton fabLeft, fabRight;
    private int itensPorPagina = 2;
    private TextView txtPagina;
    private CardView cardPagina;
    private boolean leftVisible = false, rigthVisible = false;


    private FloatingActionButton fab, fabFiltro;
    private ProgressBar progressBarFab;

    private SwipeRefreshLayout swipeRefreshLayout;
    private WifiManager wifiManager;
    private Wifi wifi;
    private List<ScanResult> results = new ArrayList<>();
    private AdapterWifis adapterWifis;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String acao = intent.getStringExtra(ACAO);
            AnimacaoUtil.progressBar(progressBarFab, fab, false);

            if (acao.equals(FALHA)) {
                ToastUtil.toastCustom(getActivity(), intent.getStringExtra(MENSAGEM), false);
            } else if (acao.equals(SUCESSO)) {
                ToastUtil.toastCustom(getActivity(), "Sucesso ao se conectar com a rede!", true);
                ((UsuarioActivity) getActivity()).setRecarregar(true);
            }

        }
    }, broadcastReceiverScanWifi = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {

            results = wifiManager.getScanResults();
            verificarConexao();
            getActivity().unregisterReceiver(this);
        }
    };


    public final static String TAG = "CATEGORIA_FRAGMENT", CONECTAR_WIFI = "CONECTAR_WIFI", ACAO = "ACAO", FALHA = "FALHA", SUCESSO = "SUCESSO", MENSAGEM = "MENSAGEM";

    private final int REQUEST_PERMISSION_LOCATION = 102, REQUEST_PERMISSION_BATTERY = 103;
    private String[] permissoes = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    public CategoriaFragment() {
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
        View view = inflater.inflate(R.layout.fragment_categoria, container, false);


        Bundle arg = getArguments();
        if (arg != null) {
            if (arg.containsKey(Categoria.CATEGORIA))
                categoria = (Categoria) arg.getSerializable(Categoria.CATEGORIA);
            Log.i(TAG, "Categoria " + categoria.getNome());
        }
        usuario = ((UsuarioActivity) getActivity()).getUsuario();

        wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);


        progressBarFab = view.findViewById(R.id.progressBarFabConectar);
        fab = view.findViewById(R.id.fabConectarWifi);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnimacaoUtil.progressBar(progressBarFab, fab, true);
                //Verificar as Permissões
                if (PermissaoUtil.validarPermissoes(permissoes, CategoriaFragment.this, REQUEST_PERMISSION_LOCATION)) {
                    solicitarLocalizacao();
                }
            }
        });

        fabFiltro = view.findViewById(R.id.fabFiltro);
        fabFiltro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FiltroFragment fragment = new FiltroFragment();
                fragment.setCategoria(categoria);
                ((UsuarioActivity) getActivity()).abrirFragment(fragment, fragment);
            }
        });

        if (!Others.isOnline(getActivity()))
            ToastUtil.toastCustom(getActivity(), "Sem conexão com a Internet!", false);

        swipeRefreshLayout = view.findViewById(R.id.swipeCategoria);
        recyclerView = view.findViewById(R.id.recyclerViewCategoria);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                recuperarComercios();
            }
        });

        //Recycler View
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        //Evento de Click
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                UsuarioActivity activity = ((UsuarioActivity) getActivity());
                ComercianteInfoFragment fragment = new ComercianteInfoFragment();
                fragment.setComerciante(comerciantesOrdem.get(position));
                activity.abrirFragment(fragment, fragment);
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        }));
        //Scroll
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int position = ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
                swipeRefreshLayout.setEnabled(position == 0);
                Log.i("onScrolled", "Posição: "+position);
            }
        });

        cardPagina = view.findViewById(R.id.cardPaginaCategoria);
        fabLeft = view.findViewById(R.id.fabLeft);
        fabLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (leftVisible) {
                    fabLeft.startAnimation(AnimacaoUtil.desaparecerView(TEMPO));
                    leftVisible = false;

                    if (rigthVisible) {
                        fabRight.startAnimation(AnimacaoUtil.desaparecerView(TEMPO));
                        rigthVisible = false;
                    }

                    cardPagina.startAnimation(AnimacaoUtil.desaparecerView(TEMPO));
                    pagina -= 1;
                    recuperarComercios();
                }
            }
        });

        fabRight = view.findViewById(R.id.fabRigth);
        fabRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rigthVisible) {

                    fabRight.startAnimation(AnimacaoUtil.desaparecerView(TEMPO));
                    rigthVisible = false;

                    if (leftVisible) {
                        fabLeft.startAnimation(AnimacaoUtil.desaparecerView(TEMPO));
                        leftVisible = false;
                    }
                    cardPagina.startAnimation(AnimacaoUtil.desaparecerView(TEMPO));
                    pagina += 1;
                    recuperarComercios();
                }
            }
        });

        txtPagina = view.findViewById(R.id.txtPaginaCategoria);
        txtPagina.setText(String.valueOf(pagina));

        return view;
    }

    private void solicitarLocalizacao() {
        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if(lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            /*long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;// Distance in meters
            long MIN_TIME_BW_UPDATES = 1000 * 10;// Time in milliseconds

            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ToastUtil.toastLoad(getActivity(), "É necessário aceitar as permissões!", true);
                AnimacaoUtil.progressBar(progressBarFab,fab, false );
                return;
            }
            lm.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, CategoriaFragment.this);*/
            verificaIgnoringBatteryOptimizations();
        }
        else{
            AlertaUtil.solicitarAtivacaoWifi(getActivity());
        }
    }

    private void recuperarComercios() {
        if (Others.isOnline(getActivity()) && categoria!=null) {
            swipeRefreshLayout.setRefreshing(true);
            recyclerView.startAnimation(AnimacaoUtil.desaparecerView(TEMPO));
            comerciantesOrdem.clear();
            comerciantesConexoes.clear();
            comerciantesDistancias.clear();
            String tipo;

            if (categoria.getNome().equals(Comerciante.MAIS_VISITADOS)) {
                recuperarComerciosMaisVisitados();
                tipo = Comerciante.MAIS_VISITADOS;
            } else if (categoria.getNome().equals(Comerciante.VISITADOS_RECENTEMENTE)) {
                recuperarComerciosVisitadosRecentemente();
                tipo = Comerciante.VISITADOS_RECENTEMENTE;
            } else if (categoria.getNome().equals(Comerciante.POPULAR)) {
                recuperarComerciosPopular();
                tipo = Comerciante.POPULAR;
            } else if (categoria.getNome().equals(Comerciante.TODOS)||categoria.getNome().equals(FiltroFragment.INICIO)) {
                recuperarComerciosTodos();
                tipo = Comerciante.TODOS;
            } else if (categoria.getNome().equals(Comerciante.NOVOS)) {
                recuperarComerciosNovos();
                tipo = Comerciante.NOVOS;
            } else if (categoria.getNome().equals(Usuario.PROXIMOS)) {
                recuperarComerciosProximos();
                tipo = Usuario.PROXIMOS;
            } else {
                recuperarComerciosCategoria();
                tipo = Categoria.CATEGORIA;
            }

            Log.i("sssss", "Tipo: " + tipo);
        } else {
            if (categoria == null) {
                CategoriasFragment categoriasFragment = new CategoriasFragment();
                ((UsuarioActivity) getActivity()).abrirFragment(categoriasFragment, categoriasFragment);
            }
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    public void recuperarComerciosCategoria(){
        referenceComercios = ConfiguracaoFirebase.getReferenceFirebase().child(Comerciante.COMERCIANTES);
        valueEventListenerComercios = referenceComercios.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Comerciante> comerciantes = new ArrayList<>();

                for(DataSnapshot dadosComerciante: dataSnapshot.getChildren()){

                    if( dadosComerciante.child(Comerciante.CONEXOES).getValue()!=null && dadosComerciante.child(Comerciante.CATEGORIAS).getValue()!=null) {
                        boolean adicionar = false;

                        for(DataSnapshot categoria: dadosComerciante.child(Comerciante.CATEGORIAS).getChildren()){
                            //Log.i(TAG, "Categoria Comércio: "+categoria.getValue().toString()+" Categoria: "+CategoriaFragment.this.categoria);
                            adicionar = categoria.getValue().toString().equals(CategoriaFragment.this.categoria.getId());
                            if(adicionar)
                                break;
                        }

                        if(adicionar) {
                            long countConexoes = 0;

                            for(DataSnapshot conexao :  dadosComerciante.child(Comerciante.CONEXOES).getChildren()){
                                countConexoes += Integer.valueOf(conexao.getValue().toString());
                            }

                            comerciantesConexoes.add(countConexoes);
                            Comerciante comerciante = dadosComerciante.getValue(Comerciante.class);
                            comerciante.setId(dadosComerciante.getKey());
                            comerciante.setConexoesComerciante(countConexoes);
                            comerciantes.add(comerciante);
                        }
                    }
                }

                Collections.sort(comerciantesConexoes);
                Collections.reverse(comerciantesConexoes);

                comerciantesOrdem = new ArrayList<>();

                for (Long conexoes : comerciantesConexoes){
                    for(Comerciante comerciante : comerciantes){
                        if(conexoes == comerciante.getConexoesComerciante()) {
                            boolean adicionar = true;
                            for(Comerciante comercianteOrdem : comerciantesOrdem){
                                if(comerciante.getCNPJ().equals(comercianteOrdem.getCNPJ())){
                                    adicionar = false;
                                    break;
                                }
                            }
                            if(adicionar){
                                comerciantesOrdem.add(comerciante);
                                break;
                            }
                        }
                    }
                }

                atribuirComercios(this);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                ToastUtil.toastCustom(getActivity(), "Error: "+databaseError.getMessage(), false);
                swipeRefreshLayout.setRefreshing(false);
                recyclerView.startAnimation(AnimacaoUtil.mostrarView(TEMPO));
            }
        });
    }

    private void recuperarComerciosMaisVisitados() {
        fabFiltro.setVisibility(View.GONE);
        FirebaseAuth firebaseAuth = ConfiguracaoFirebase.getFirebaseAuth();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        String id = Base64Util.dadoToBase64(user.getEmail());
        DatabaseReference referenceIdComercios = ConfiguracaoFirebase.getReferenceFirebase().child(Usuario.USUARIOS).child(id).child(Usuario.VISITAS);
        referenceIdComercios.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final List<Comerciante> comerciantesUsuario = new ArrayList<>();

                for (DataSnapshot dado : dataSnapshot.getChildren()){
                    Comerciante comerciante = new Comerciante();
                    comerciante.setConexoesComerciante(Integer.valueOf(dado.getValue().toString()));
                    comerciantesConexoes.add(comerciante.getConexoesComerciante());
                    comerciante.setId(dado.getKey());
                    comerciantesUsuario.add(comerciante);
                }

                referenceComercios = ConfiguracaoFirebase.getReferenceFirebase().child(Comerciante.COMERCIANTES);
                valueEventListenerComercios = referenceComercios.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        List<Comerciante> comerciantes = new ArrayList<>();

                        for(DataSnapshot dadosComerciate : dataSnapshot.getChildren()){
                            Comerciante comercianteDados = dadosComerciate.getValue(Comerciante.class);
                            comercianteDados.setId(dadosComerciate.getKey());

                            for(Comerciante comerciante : comerciantesUsuario){
                                if(comerciante.getId().equals(comercianteDados.getId())){
                                    comercianteDados.setConexoesComerciante(comerciante.getConexoesComerciante());
                                    comerciantes.add(comercianteDados);
                                    break;
                                }
                            }
                        }


                        Collections.sort(comerciantesConexoes);
                        Collections.reverse(comerciantesConexoes);

                        for (Long conexoes : comerciantesConexoes){
                            for(Comerciante comerciante : comerciantes){
                                if(conexoes == comerciante.getConexoesComerciante()) {
                                    boolean adicionar = true;
                                    for(Comerciante comercianteOrdem : comerciantesOrdem){
                                        if(comerciante.getCNPJ().equals(comercianteOrdem.getCNPJ())){
                                            adicionar = false;
                                            break;
                                        }
                                    }
                                    if(adicionar){
                                        comerciantesOrdem.add(comerciante);
                                        break;
                                    }
                                }
                            }
                        }


                        atribuirComercios(this);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        ToastUtil.toastCustom(getActivity(), "Error: "+databaseError.getMessage(), false);
                        swipeRefreshLayout.setRefreshing(false);
                        recyclerView.startAnimation(AnimacaoUtil.mostrarView(TEMPO));
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                ToastUtil.toastCustom(getActivity(), "Error: "+databaseError.getMessage(), false);
                swipeRefreshLayout.setRefreshing(false);
                recyclerView.startAnimation(AnimacaoUtil.mostrarView(TEMPO));
            }
        });
    }

    private void recuperarComerciosVisitadosRecentemente() {
        fabFiltro.setVisibility(View.GONE);
        FirebaseAuth firebaseAuth = ConfiguracaoFirebase.getFirebaseAuth();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        String id = Base64Util.dadoToBase64(user.getEmail());
        DatabaseReference referenceIdComercios = ConfiguracaoFirebase.getReferenceFirebase().child("Usuarios").child(id).child(Usuario.VISITAS_RECENTES);
        referenceIdComercios.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final List<Conexao> conexoes = new ArrayList<>();

                for (DataSnapshot dado : dataSnapshot.getChildren()){
                    Conexao conexao = new Conexao();
                    String data = dado.getValue().toString().replace("-","").replace(":","");
                    comerciantesConexoes.add(Long.valueOf(data));
                    conexao.setData(data);
                    conexao.setIdComerciante(dado.getKey());
                    conexoes.add(conexao);
                }

                referenceComercios = ConfiguracaoFirebase.getReferenceFirebase().child(Comerciante.COMERCIANTES);
                valueEventListenerComercios = referenceComercios.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        List<Comerciante> comerciantes = new ArrayList<>();

                        for(DataSnapshot dadosComerciate : dataSnapshot.getChildren()){
                            Comerciante comercianteDados = dadosComerciate.getValue(Comerciante.class);
                            comercianteDados.setId(dadosComerciate.getKey());

                            for(Conexao conexao : conexoes){
                                if(conexao.getIdComerciante().equals(comercianteDados.getId())){
                                    comerciantes.add(comercianteDados);
                                    break;
                                }
                            }
                        }

                        Collections.sort(comerciantesConexoes);
                        Collections.reverse(comerciantesConexoes);

                        for (Long data : comerciantesConexoes){
                            for(Conexao conexao : conexoes){
                                if(Long.valueOf(conexao.getData()).equals(data) ) {
                                    for(Comerciante comerciante : comerciantes){
                                        if(comerciante.getId().equals(conexao.getIdComerciante())){
                                            comerciantesOrdem.add(comerciante);
                                            break;
                                        }
                                    }
                                }
                            }
                        }

                        atribuirComercios(this);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        ToastUtil.toastCustom(getActivity(), "Error: "+databaseError.getMessage(), false);
                        swipeRefreshLayout.setRefreshing(false);
                        recyclerView.startAnimation(AnimacaoUtil.mostrarView(TEMPO));
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                ToastUtil.toastCustom(getActivity(), "Error: "+databaseError.getMessage(), false);
                swipeRefreshLayout.setRefreshing(false);
                recyclerView.startAnimation(AnimacaoUtil.mostrarView(TEMPO));
            }
        });
    }

    private void recuperarComerciosTodos(){
        referenceComercios = ConfiguracaoFirebase.getReferenceFirebase().child(Comerciante.COMERCIANTES);
        valueEventListenerComercios = referenceComercios.orderByChild("nomeLoja").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dadosComerciante: dataSnapshot.getChildren()){

                    Comerciante comerciante = dadosComerciante.getValue(Comerciante.class);
                    comerciantesOrdem.add(comerciante);
                    comerciante.setId(dadosComerciante.getKey());
                }

                atribuirComercios(this);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                ToastUtil.toastCustom(getActivity(), "Error: "+databaseError.getMessage(), false);
                swipeRefreshLayout.setRefreshing(false);
                recyclerView.startAnimation(AnimacaoUtil.mostrarView(TEMPO));
            }
        });
    }

    private void recuperarComerciosPopular() {
        referenceComercios = ConfiguracaoFirebase.getReferenceFirebase().child(Comerciante.COMERCIANTES);
        valueEventListenerComercios = referenceComercios.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Comerciante> comerciantes = new ArrayList<>();

                for(DataSnapshot dadosComerciante: dataSnapshot.getChildren()){

                    if( dadosComerciante.child(Comerciante.CONEXOES).getValue()!=null) {

                        long countConexoes = 0;

                        for(DataSnapshot conexao :  dadosComerciante.child(Comerciante.CONEXOES).getChildren()){
                            countConexoes += Integer.valueOf(conexao.getValue().toString());
                        }

                        comerciantesConexoes.add(countConexoes);
                        Comerciante comerciante = dadosComerciante.getValue(Comerciante.class);
                        comerciante.setId(dadosComerciante.getKey());
                        comerciante.setConexoesComerciante(countConexoes);
                        comerciantes.add(comerciante);
                    }
                }

                Collections.sort(comerciantesConexoes);
                Collections.reverse(comerciantesConexoes);

                comerciantesOrdem = new ArrayList<>();

                for (Long conexoes : comerciantesConexoes){
                    for(Comerciante comerciante : comerciantes){
                        if(conexoes == comerciante.getConexoesComerciante()) {
                            boolean adicionar = true;
                            for(Comerciante comercianteOrdem : comerciantesOrdem){
                                if(comerciante.getCNPJ().equals(comercianteOrdem.getCNPJ())){
                                    adicionar = false;
                                    break;
                                }
                            }
                            if(adicionar){
                                comerciantesOrdem.add(comerciante);
                                break;
                            }
                        }
                    }
                }

                atribuirComercios(this);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                ToastUtil.toastCustom(getActivity(), "Error: "+databaseError.getMessage(), false);
                swipeRefreshLayout.setRefreshing(false);
                recyclerView.startAnimation(AnimacaoUtil.mostrarView(TEMPO));
            }
        });
    }

    private void recuperarComerciosNovos(){
        referenceComercios = ConfiguracaoFirebase.getReferenceFirebase().child(Comerciante.COMERCIANTES);
        valueEventListenerComercios = referenceComercios.orderByChild("dataInscricao").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dadosComerciante: dataSnapshot.getChildren()){

                    Comerciante comerciante = dadosComerciante.getValue(Comerciante.class);
                    comerciantesOrdem.add(comerciante);
                    comerciante.setId(dadosComerciante.getKey());
                }

                Others.invertUsingCollectionsReverse(comerciantesOrdem);

               atribuirComercios(this);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                ToastUtil.toastCustom(getActivity(), "Error: "+databaseError.getMessage(), false);
                swipeRefreshLayout.setRefreshing(false);
                recyclerView.startAnimation(AnimacaoUtil.mostrarView(TEMPO));
            }
        });
    }

    private void recuperarComerciosProximos() {
        fabFiltro.setVisibility(View.GONE);
        referenceComercios = ConfiguracaoFirebase.getReferenceFirebase().child(Comerciante.COMERCIANTES);
        valueEventListenerComercios = referenceComercios.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Comerciante> comerciantes = new ArrayList<>();

                for(DataSnapshot dadosComerciante: dataSnapshot.getChildren()){

                    Comerciante comerciante = dadosComerciante.getValue(Comerciante.class);

                    double distance = Others.meterDistanceBetweenPoints(usuario.getLatitude(), usuario.getLongitude(), comerciante.getLatitude(), comerciante.getLongitude());
                    distance /= 1000;
                    String distacia = String.format("%.2f", distance);
                    if(distance<=raio || distacia.equals("NaN")) {
                        comerciante.setId(dadosComerciante.getKey());
                        comerciante.setDistance(distance);
                        comerciantes.add(comerciante);
                        comerciantesDistancias.add(distance);
                    }
                }

                Collections.sort(comerciantesDistancias);

                comerciantesOrdem = new ArrayList<>();

                for (Double distancia : comerciantesDistancias){
                    for(Comerciante comerciante : comerciantes){
                        if(distancia == comerciante.getDistance()) {
                            boolean adicionar = true;
                            for(Comerciante comercianteOrdem : comerciantesOrdem){
                                if(comerciante.getCNPJ().equals(comercianteOrdem.getCNPJ())){
                                    adicionar = false;
                                    break;
                                }
                            }
                            if(adicionar){
                                comerciantesOrdem.add(comerciante);
                                break;
                            }
                        }
                    }
                }

                atribuirComercios(this);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                ToastUtil.toastCustom(getActivity(), "Error: "+databaseError.getMessage(), false);
                swipeRefreshLayout.setRefreshing(false);
                recyclerView.startAnimation(AnimacaoUtil.mostrarView(TEMPO));
            }
        });
    }

    private void atribuirComercios(ValueEventListener valueEventListener) {

        /*
        //Exibir por Página
        List<Comerciante> comerciantes = new ArrayList<>();
        for (int i = (pagina - 1) * itensPorPagina; i < (pagina * itensPorPagina) && i < comerciantesOrdem.size() && i > -1; i++) {
            Comerciante comerciante = comerciantesOrdem.get(i);
            comerciantes.add(comerciante);
        }

        if (pagina > 1) {
            if(!leftVisible) {
                fabLeft.startAnimation(AnimacaoUtil.mostrarView(TEMPO));
                leftVisible = true;
            }
        }

        if (comerciantesOrdem.size() > itensPorPagina * pagina) {
               if(!rigthVisible){
                   fabRight.startAnimation(AnimacaoUtil.mostrarView(TEMPO));
                   rigthVisible = true;
               }
        }

        txtPagina.setText(String.valueOf(pagina));
        cardPagina.startAnimation(AnimacaoUtil.mostrarView(TEMPO));*/

        adapter = new AdapterComerciantes(comerciantesOrdem, getActivity(), usuario);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        recyclerView.startAnimation(AnimacaoUtil.mostrarView(TEMPO));
        swipeRefreshLayout.setRefreshing(false);
        referenceComercios.removeEventListener(valueEventListener);
        //Log.i(TAG, "Position: " + currentVisiblePosition);
        recyclerView.scrollToPosition(currentVisiblePosition);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i("PERMISSION", "requestCode: "+requestCode);

        if(requestCode == REQUEST_PERMISSION_LOCATION){
            boolean aceita = true;

            for(int permissaoResultado : grantResults){
                if(permissaoResultado == PackageManager.PERMISSION_DENIED){
                    AlertaUtil.alertaErro("Permissão Negada","É necessário aceitar a permissão para uso da função!",getContext());
                    AnimacaoUtil.progressBar(progressBarFab, fab, false);
                    aceita = false;
                    break;
                }
            }

            if (aceita) {
                solicitarLocalizacao();
            }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.i("ACTIVITY_RESULT", "requestCode: " + requestCode);
            if (requestCode == REQUEST_PERMISSION_BATTERY) {

                PowerManager pm = (PowerManager) getActivity().getApplicationContext().getSystemService(Context.POWER_SERVICE);
                boolean ignorado = pm.isIgnoringBatteryOptimizations(getActivity().getApplicationContext().getPackageName());

                if (ignorado)
                    scanWifis();
                else{
                    AlertaUtil.alertaErro("Exceção não definida!","É necessário alterar a configuração de otimização de bateria sobre o aplicativo para seu funcionamento correto!",getContext());
                    AnimacaoUtil.progressBar(progressBarFab, fab, false);
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        setToolbarTitle();
        recuperarComercios();
        getActivity().registerReceiver(broadcastReceiver, new IntentFilter(CONECTAR_WIFI));
        currentVisiblePosition = ((UsuarioActivity) getActivity()).getPosition(TAG + "_" + categoria);
        pagina = ((UsuarioActivity) getActivity()).getPagina(TAG + "_" + categoria);

    }

    @Override
    public void onStop() {
        super.onStop();
        if(valueEventListenerComercios!=null)
            referenceComercios.removeEventListener(valueEventListenerComercios);
        int position = ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        ((UsuarioActivity)getActivity()).addPosition(new PositionRecyclerView(TAG+"_"+categoria, position));
        ((UsuarioActivity)getActivity()).addPagina(new Pagina(TAG+"_"+categoria, pagina));
        getActivity().unregisterReceiver(broadcastReceiver);
    }

    private void verificaIgnoringBatteryOptimizations(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            PowerManager pm = (PowerManager) getActivity().getApplicationContext().getSystemService(Context.POWER_SERVICE);
            boolean ignorado = pm.isIgnoringBatteryOptimizations(getActivity().getApplicationContext().getPackageName());
            if(!ignorado) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Uso de Bateria");
                builder.setMessage("Adicione o aplicativo às exceções de otimização da bateria. Defina o aplicativo UseFi como: Não Optimizar");
                builder.setCancelable(false);
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                        startActivityForResult(intent, REQUEST_PERMISSION_BATTERY);
                    }
                });
                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AnimacaoUtil.progressBar(progressBarFab, fab, false);
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }else
                scanWifis();

        }else
            scanWifis();
    }

    private void scanWifis(){
        if (!wifiManager.isWifiEnabled())
        {
            wifiManager.setWifiEnabled(true);
        }

        Intent broadcast = new Intent(getActivity(), ConexaoWifiBroadcast.class);
        AlarmUtil.cancel(getActivity().getApplicationContext(), broadcast, ConexaoWifiBroadcast.REQUEST_CODE);

        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(getActivity().getApplicationContext()));
        dispatcher.cancel(JobConexaoWifi.TAG);

        SharedPreferencesUtil.gravarValor(getActivity(), ConexaoWifi.COUNT, -1);

        results.clear();
        wifiManager.startScan();

        getActivity().registerReceiver(broadcastReceiverScanWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    private void verificarConexao() {
        List<Wifi> wifisCadastrados = BancoDadosUtil.lerWifis(getActivity(), false);
        String ssid = Others.getWifiSSID(getActivity().getApplicationContext());

        if (!ssid.equals("")) {
            boolean redeConectada = false;

            for (Wifi wifi : wifisCadastrados) {
                if (ssid.equals("\"" + wifi.getNome() + "\"")) {
                    redeConectada = true;
                    break;
                }
            }

            if (redeConectada) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setCancelable(false);
                        builder.setTitle("Wi-fi");
                        builder.setMessage("Você já está conectado ao Wi-fi de um comerciante, deseja desconectar?");
                        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AnimacaoUtil.progressBar(progressBarFab, fab, false);
                            }
                        });
                        builder.setPositiveButton("Desconectar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, final int which) {
                                new Thread(){
                                    @Override
                                    public void run() {
                                        wifiManager.removeNetwork(wifiManager.getConnectionInfo().getNetworkId());
                                        exibirWifisDisponiveis();
                                    }
                                }.start();
                            }
                        });

                        builder.create().show();
                    }
                });
            } else
                exibirWifisDisponiveis();

        } else
            exibirWifisDisponiveis();

    }

    private void exibirWifisDisponiveis() {
        try {
            new Thread() {
                @Override
                public void run() {

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AnimacaoUtil.progressBar(progressBarFab, fab, true);
                        }
                    });

                    final List<Wifi> wifis = new ArrayList<>();

                    List<Wifi> wifisCadastrados = BancoDadosUtil.lerWifis(getActivity(), false);
                    List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();

                    for (Wifi wifi : wifisCadastrados) {

                        for (ScanResult result : results) {

                            if (wifi.getNome().equals(result.SSID)) {
                                boolean adicionar = true;
                                for (Wifi wifiAdicionado : wifis) {
                                    if (wifiAdicionado.getNome().equals(wifi.getNome())) {
                                        adicionar = false;
                                        break;
                                    }
                                }
                                if (adicionar)
                                    for (WifiConfiguration item : list) {
                                        if (item.SSID.equals("\"" + wifi.getNome() + "\"")) {
                                            wifiManager.removeNetwork(item.networkId);
                                            break;
                                        }
                                    }

                                wifi.setBssid(result.BSSID);
                                if (adicionar) {
                                    wifis.add(wifi);
                                }
                                break;
                            }
                        }
                    }
                    /*
                    for(ScanResult result :results){
                        Wifi wifi = new Wifi();
                        wifi.setNome(result.SSID);
                        wifi.setSenha(result.SSID);
                        wifi.setBssid(result.BSSID);
                        wifis.add(wifi);
                    }*/


                    if (wifis.size() < 1) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AnimacaoUtil.progressBar(progressBarFab, fab, false);
                                ToastUtil.toastCustom(getActivity(), "Não há redes baixadas disponíveis!", false);
                            }
                        });
                        return;
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Senha para validar as edições
                            final Dialog dialog = new Dialog(getActivity());
                            dialog.setContentView(R.layout.dialog_recycleview);
                            TextView title = dialog.findViewById(R.id.txtTitle);
                            title.setText("Wi-fi");
                            TextView msg = dialog.findViewById(R.id.txtMsg);
                            msg.setText("Redes de Wi-fi disponíveis:");
                            dialog.setCancelable(false);

                            RecyclerView recyclerView = dialog.findViewById(R.id.recyclerViewDialog);
                            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
                            recyclerView.setLayoutManager(layoutManager);
                            recyclerView.setHasFixedSize(true);
                            recyclerView.setItemAnimator(new DefaultItemAnimator());
                            adapterWifis = new AdapterWifis(wifis);
                            recyclerView.setAdapter(adapterWifis);
                            recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                                @Override
                                public void onItemClick(View view, int position) {
                                    wifi = wifis.get(position);
                                    wifi.setNome("\"" + wifi.getNome() + "\"");
                                    wifi.setSenha("\"" + wifi.getSenha() + "\"");
                                    conectarComWifi();
                                    dialog.dismiss();
                                }

                                @Override
                                public void onLongItemClick(View view, int position) {

                                }

                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                }
                            }));


                            Button cancell = dialog.findViewById(R.id.btnCancelarDialog);
                            cancell.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();
                                    ToastUtil.toastCustom(getActivity(), "Conexão cancelada!", false);
                                    AnimacaoUtil.progressBar(progressBarFab, fab, false);
                                }
                            });
                            dialog.show();
                        }
                    });


                }
            }.start();
        }
        catch (Exception e){
            ToastUtil.toastCustom(getActivity(), e.getMessage(), false);
            AnimacaoUtil.progressBar(progressBarFab, fab, false);
        }
    }

    private void conectarComWifi() {

        SharedPreferencesUtil.gravarValor(getActivity(), Wifi.WIFI_NOME, wifi.getNome());
        SharedPreferencesUtil.gravarValor(getActivity(), Wifi.WIFI_SENHA, wifi.getSenha());
        SharedPreferencesUtil.gravarValor(getActivity(), Wifi.WIFI_BSSID, wifi.getBssid());
        SharedPreferencesUtil.gravarValor(getActivity(), Wifi.WIFI_ID_COMERCIANTE, wifi.getIdComerciante());

        final Context context = getActivity().getApplicationContext();
        new Thread(){

            @Override
            public void run() {

                try {
                    SharedPreferencesUtil.gravarValor(context, VerificarWifiOff.CONEXAO, VerificarWifiOff.CONECTANDO);

                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    wifiManager.disconnect();
                    boolean conectou = Others.wifiConnection(context, wifi.getNome(), wifi.getSenha());
                    wifiManager.reconnect();
                    //conectou = true;


                    Log.i(TAG, "EnableNetwork: " + conectou);

                    Log.i(TAG, "Wifi: " + wifi.getNome());
                    int count = 0;
                    while (count < 5) {
                        Log.d(TAG, getClass().getSimpleName() + " executando... " + count);
                        count++;
                        Thread.sleep(1000);
                    }

                    if (conectou) {

                        if(wifiManager.getConnectionInfo().getSSID().equals(wifi.getNome())) {

                            FirebaseAuth firebaseAuth = ConfiguracaoFirebase.getFirebaseAuth();
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            String id = Base64Util.dadoToBase64(user.getEmail());

                            Date date = Others.getNTPDate();
                            SharedPreferencesUtil.gravarValor(context, DATA_ULTIMA_CONEXAO, DateUtil.dataDateToDataCompletaSegundos(date));

                            final HashMap<String, Object> usuarioMap = new HashMap<>();
                            usuarioMap.put("ultimaVisita", DateUtil.dataDateToDataCompleta(date));
                            usuarioMap.put("ultimaConexao", DateUtil.dataDateToDataCompleta(date));
                            //wifi.setIdComerciante("ZW56b2xvdXJlaXJvQHlhaG9vLmNvbS5icg==");
                            wifi.setId(String.valueOf(wifiManager.getConnectionInfo().getNetworkId()));


                            final DatabaseReference referenceComerciante = ConfiguracaoFirebase.getReferenceFirebase().child("Comerciantes").child(wifi.getIdComerciante()).child("Usuarios").child(id);
                            referenceComerciante.updateChildren(usuarioMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {
                                        sucesso();
                                    } else
                                        falha(task.getException().getMessage());
                                }
                            });

                        }else{
                            falha("Erro SSID\nNão foi possível se conectar. Verifique se os dados da rede estão atualizados!");
                        }
                    } else {
                        falha("Erro conexão\nNão foi possível se conectar. Verifique se os dados da rede estão atualizados!");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    falha(e.getMessage());
                }
            }
            private void sucesso() {

                SharedPreferencesUtil.gravarValor(context, Wifi.ID, wifi.getId());
                SharedPreferencesUtil.gravarValor(context, Wifi.WIFI_ID_COMERCIANTE, wifi.getIdComerciante());

                FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
                Job conexao = dispatcher.newJobBuilder()
                        .setService(JobConexaoWifi.class)//JobConexaoWifi é o JobService
                        .setTag(JobConexaoWifi.TAG)//JobConexaoWifi.TAG é a tag que identifica o job
                        .setRecurring(true)//O job só irá executar mais de uma vez
                        .setLifetime(Lifetime.FOREVER)
                        .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)
                        .setTrigger(Trigger.executionWindow(0, 10)) //O job tem de começar dentro do tempo definido
                        .setReplaceCurrent(true)// Não sobrepor um job que tenha a mesma tag
                        .setConstraints(
                                Constraint.ON_UNMETERED_NETWORK//Só é executado em conexões Wi-fi
                        )
                        .build();

                dispatcher.mustSchedule(conexao);

                ToastUtil.toastCustom(getActivity(), "Sucesso ao se conectar com a rede!", true);

                getActivity().finish();
            }

            private void falha(String texto){
                List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();

                for(WifiConfiguration i : list){
                    if(i.SSID!=null && i.SSID.equals(wifi.getNome())){
                        wifiManager.removeNetwork(i.networkId);
                        break;
                    }
                }

                Intent intent = new Intent(CONECTAR_WIFI);
                intent.putExtra(ACAO, FALHA);
                intent.putExtra(MENSAGEM, texto);
                context.sendBroadcast(intent);
            }
        }.start();

    }

    public void setRaio(double raio) {
        this.raio = raio;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

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
        if(categoria!=null) {
            if (!categoria.getNome().equals(Comerciante.TODOS) && !categoria.getNome().equals(Comerciante.NOVOS) && !categoria.getNome().equals(Comerciante.POPULAR)) {
                ((UsuarioActivity) getActivity()).setToolbarTitle(categoria.getNome());
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        verificaIgnoringBatteryOptimizations();
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
}
