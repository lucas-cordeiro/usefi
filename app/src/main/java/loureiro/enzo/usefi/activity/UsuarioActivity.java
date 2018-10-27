package loureiro.enzo.usefi.activity;

import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import loureiro.enzo.usefi.fragment.FiltroFragment;
import loureiro.enzo.usefi.broadcast.BaixarRedes;
import loureiro.enzo.usefi.fragment.CategoriaFragment;
import loureiro.enzo.usefi.fragment.CategoriasFragment;
import loureiro.enzo.usefi.fragment.ComercianteInfoFragment;
import loureiro.enzo.usefi.fragment.MaisVisitadosFragment;
import loureiro.enzo.usefi.fragment.PerfilUsuarioFragment;
import loureiro.enzo.usefi.fragment.PromocoesFragment;
import loureiro.enzo.usefi.fragment.ProximoUsuarioFragment;
import loureiro.enzo.usefi.fragment.VisitadosRecentementeFragment;
import loureiro.enzo.usefi.helper.Others;
import loureiro.enzo.usefi.model.Categoria;
import loureiro.enzo.usefi.model.Wifi;
import loureiro.enzo.usefi.myInterface.IFragment;
import loureiro.enzo.usefi.model.Comerciante;
import loureiro.enzo.usefi.model.Pagina;
import loureiro.enzo.usefi.model.PositionRecyclerView;
import loureiro.enzo.usefi.model.Usuario;
import loureiro.enzo.usefi.R;
import loureiro.enzo.usefi.service.JobConexaoWifi;
import loureiro.enzo.usefi.util.AnimacaoUtil;
import loureiro.enzo.usefi.util.BancoDadosUtil;
import loureiro.enzo.usefi.util.Base64Util;
import loureiro.enzo.usefi.util.ConfiguracaoFirebase;
import loureiro.enzo.usefi.util.EquipeUseFiUtil;
import loureiro.enzo.usefi.util.FirebaseUtil;
import loureiro.enzo.usefi.util.SharedPreferencesUtil;
import loureiro.enzo.usefi.util.ToastUtil;

public class UsuarioActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private FrameLayout frameLayout;
    private NavigationView navigationView;

    private Toolbar toolbar;
    private TextView titleToolbar, subTitleToolbar;
    private ImageView imgToolbar;
    private LinearLayout linearLayoutToolbar;

    private List<Categoria> categorias;

    private List<Fragment> fragments = new ArrayList<>();
    private List<IFragment> iFragments = new ArrayList<>();
    private List<PositionRecyclerView> positionRecyclerViews = new ArrayList<>();
    private List<Pagina> paginas = new ArrayList<>();

    public int indexAtual;
    private boolean primeiroFrame = true;

    public Usuario usuario;
    private DatabaseReference referenceUser;
    private ValueEventListener valueEventListenerUsuario;
    private boolean recarregar = false;
    private boolean voltar = false;

    private boolean notificacaoComerciante = false;

    private static final String TAG = "USUARIO_ACTIVITY";
    public static final String ERROR = "ERROR";
    private static boolean AppOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario);
        FirebaseApp.initializeApp(this);
        AppOpen = true;

        frameLayout = findViewById(R.id.frameContainer);

        toolbar = findViewById(R.id.toolbar_usuario);
        linearLayoutToolbar = findViewById(R.id.linearToolbar);
        titleToolbar  = findViewById(R.id.titleToolbar);
        subTitleToolbar  = findViewById(R.id.subTitleToolbar);
        imgToolbar = findViewById(R.id.imgToolbar);
        toolbar.setElevation(0);
        setSupportActionBar(toolbar);
        setToolbarTitle("UseFi");

        drawer =  findViewById(R.id.drawer_layout);
        drawer.startAnimation(AnimacaoUtil.desaparecerView(0));
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if(getIntent()!=null) {
            notificacaoComerciante = getIntent().getStringExtra(Comerciante.COMERCIANTES) != null;
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
                if(voltarFragment())//Verifica se há fragments para voltar
                    super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.usuario, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        /*
        if(id == R.id.action_config){//Configurações
            ToastUtil.toastCustom(UsuarioActivity.this, "Configuração", true);
        }else*/ if(id == R.id.action_sair){//Sair
            try {
                List<Wifi> wifis = BancoDadosUtil.lerWifis(getApplicationContext());
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                for (WifiConfiguration i : wifiManager.getConfiguredNetworks()) {
                    for (Wifi wifi : wifis) {
                        if (i.SSID != null && i.SSID.equals("\"" + wifi.getNome() + "\"")) {
                            Log.i(TAG, "remove: " + i.SSID);
                            wifiManager.removeNetwork(i.networkId);
                        }
                    }
                }
                FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(getApplicationContext()));
                dispatcher.cancel(JobConexaoWifi.TAG);

            }
            catch (Exception e){
                Log.i(TAG, "Error: "+e.getMessage());
            }
            FirebaseUtil.deslogarConta(UsuarioActivity.this);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_categorias) {//Categorias

            CategoriasFragment fragment = new CategoriasFragment();
            abrirFragment(fragment, fragment);

        }else if(id == R.id.nav_mais_visitados){//Mais visitados

            MaisVisitadosFragment fragment = new MaisVisitadosFragment();
            abrirFragment(fragment, fragment);

        }else if(id == R.id.nav_visitados_recentemente){//Visitados recentemente

            VisitadosRecentementeFragment fragment = new VisitadosRecentementeFragment();
            abrirFragment(fragment, fragment);
        }else if(id == R.id.nav_promocoes){//Promoções

            PromocoesFragment fragment = new PromocoesFragment();
            abrirFragment(fragment, fragment);

        }else if(id == R.id.nav_proximo){//Próximos ao usuário

            ProximoUsuarioFragment fragment = new ProximoUsuarioFragment();
            abrirFragment(fragment, fragment);
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarUsuario();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(valueEventListenerUsuario!=null)
        referenceUser.removeEventListener(valueEventListenerUsuario);
        SharedPreferencesUtil.gravarValor(UsuarioActivity.this, "Fragment", iFragments.get(indexAtual).getTAG());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppOpen = false;
    }

    //Recupera o Usuario Logado
    public void recuperarUsuario() {
        Log.i(TAG, "recuperarUsuario");
        if(Others.isOnline(this)){
            new Thread(){
                @Override
                public void run() {
                    FirebaseAuth firebaseAuth = ConfiguracaoFirebase.getFirebaseAuth();
                    final FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        String id = Base64Util.dadoToBase64(user.getEmail());
                        referenceUser = ConfiguracaoFirebase.getReferenceFirebase().child("Usuarios").child(id);
                        valueEventListenerUsuario = referenceUser.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() != null) {
                                    Log.i(TAG, dataSnapshot.getValue().toString());
                                    usuario = dataSnapshot.getValue(Usuario.class);
                                    usuario.setId(dataSnapshot.getKey());

                                    if (usuario != null) {
                                        StorageReference referenceImagem = ConfiguracaoFirebase.getStorageReference().child(Usuario.USUARIOS).child(usuario.getId()).child("perfil.png");
                                        referenceImagem.getDownloadUrl().addOnCompleteListener(UsuarioActivity.this, new OnCompleteListener<Uri>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Uri> task) {
                                                if(task.isSuccessful()){
                                                    usuario.setImgUrl(task.getResult().toString());
                                                }

                                                SharedPreferencesUtil.gravarValor(UsuarioActivity.this, EquipeUseFiUtil.CONTA, Usuario.USUARIOS);
                                                SharedPreferencesUtil.gravarValor(UsuarioActivity.this, Usuario.NOME, usuario.getNome());
                                                SharedPreferencesUtil.gravarValor(UsuarioActivity.this, Usuario.ID, usuario.getId());
                                                SharedPreferencesUtil.gravarValor(UsuarioActivity.this, Usuario.GENERO, usuario.getGenero());
                                                SharedPreferencesUtil.gravarValor(UsuarioActivity.this, Usuario.IDADE, usuario.getIdade());

                                                if(getIntent().getStringExtra(ERROR)!=null){
                                                    DatabaseReference referenceError = ConfiguracaoFirebase.getReferenceFirebase().child(ERROR).child(Usuario.USUARIOS).child(usuario.getId()).push();
                                                    referenceError.setValue(getIntent().getStringExtra(ERROR));
                                                }

                                                if(dataSnapshot.child(Usuario.TOPICOS).getValue()!=null) {

                                                    for (DataSnapshot topico : dataSnapshot.child(Usuario.TOPICOS).getChildren()) {
                                                        String idComerciante = topico.getKey();
                                                        FirebaseMessaging.getInstance().subscribeToTopic(idComerciante.replace("=",""));
                                                    }


                                                }

                                                new Thread(){
                                                    @Override
                                                    public void run() {
                                                        if(dataSnapshot.child(Usuario.REDES_BAIXADAS).getValue()!=null){

                                                            final List<String>  comerciantes = new ArrayList<>();

                                                            for(DataSnapshot visita : dataSnapshot.child(Usuario.REDES_BAIXADAS).getChildren()){
                                                                comerciantes.add(visita.getKey());
                                                            }

                                                            final String[] comerciantesArray = new String[comerciantes.size()];
                                                            int count = 0;
                                                            for(String dado : comerciantes){
                                                                comerciantesArray[count] = dado;
                                                                count++;
                                                            }

                                                            DatabaseReference referenceComerciantes = ConfiguracaoFirebase.getReferenceFirebase().child(Comerciante.COMERCIANTES);
                                                            referenceComerciantes.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                    for(DataSnapshot dado : dataSnapshot.getChildren()){
                                                                        Comerciante comerciante = dado.getValue(Comerciante.class);
                                                                        comerciante.setId(dado.getKey());
                                                                        for(String id : comerciantesArray){
                                                                            if(id.equals(dado.getKey())){
                                                                                Wifi wifi = new Wifi(comerciante.getWifiNome(),"", comerciante.getWifiSenha(), comerciante.getId());
                                                                                BancoDadosUtil.gravarWifi(UsuarioActivity.this, wifi);
                                                                                break;
                                                                            }
                                                                        }
                                                                    }


                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                }
                                                            });
                                                        }
                                                    }
                                                }.start();

                                                definirDadosUsuario();
                                                carregarApp();
                                            }
                                        });

                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                    }
                }
            }.start();
        }else{
            usuario = new Usuario();
            usuario.setNome(SharedPreferencesUtil.lerValor(this, Usuario.NOME));
            Log.i(TAG,usuario.getNome());
            definirDadosUsuario();
            carregarApp();

        }
    }


    private void carregarApp() {
        Log.i(TAG, "carregarApp");
        if(fragments.size()<1 || recarregar){
            if(!recarregar)
                drawer.startAnimation(AnimacaoUtil.mostrarView(500));

            if(notificacaoComerciante){
                ToastUtil.toastLoad(this, "Carregando informações...", false);
                String idComerciante = getIntent().getStringExtra(Comerciante.COMERCIANTES);
                DatabaseReference referenceComerciante = ConfiguracaoFirebase.getReferenceFirebase().child(Comerciante.COMERCIANTES).child(idComerciante);
                referenceComerciante.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Comerciante comerciante = dataSnapshot.getValue(Comerciante.class);
                        ComercianteInfoFragment fragment = new ComercianteInfoFragment();
                        fragment.setComerciante(comerciante);
                        CategoriasFragment categoriasFragment = new CategoriasFragment();
                        fragments.add(categoriasFragment);
                        iFragments.add(categoriasFragment);
                        abrirFragment(fragment, fragment);
                        notificacaoComerciante = false;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }else{
                abrirApp();
            }
            recarregar = false;
        }else{
            if(SharedPreferencesUtil.contemValor(UsuarioActivity.this, "Fragment")){
                String fragment = SharedPreferencesUtil.lerValor(UsuarioActivity.this, "Fragment");
                Log.i(TAG, fragment);
                if(fragment.equals(CategoriasFragment.TAG))
                abrirApp();
            }
        }
    }

    //Atribui os valores do Usuario Logado
    private void definirDadosUsuario() {

        UsuarioActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        TextView txtNome = findViewById(R.id.txtNomeUsuarioNav);
                        ImageView imgFoto = findViewById(R.id.imgFotoUsuarioNav);
                        ImageButton imgEdit = findViewById(R.id.imgButtonEditNav);
                        ProgressBar progressBar = findViewById(R.id.progressBarUsuarioNav);

                        txtNome.setText(usuario.getNome());
                        if(usuario.getImgUrl()!=null)
                        FirebaseUtil.recuperarImagemStorage(UsuarioActivity.this, Uri.parse(usuario.getImgUrl()), progressBar, imgFoto, imgFoto);
                        imgEdit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                PerfilUsuarioFragment fragment = new PerfilUsuarioFragment();
                                abrirFragment(fragment, fragment);
                            }
                        });
                    }
                }, 1000);
            }
        });
    }

    //Navegação dos Fragments [Início]

    private void abrirApp() {
        fragments.clear();
        iFragments.clear();
        if (Others.isOnline(UsuarioActivity.this)) {
            CategoriasFragment fragment = new CategoriasFragment();
            abrirFragment(fragment, fragment);
        } else {
            CategoriaFragment fragment = new CategoriaFragment();
            abrirFragment(fragment, fragment);
        }
    }

    public void abrirFragment(final IFragment iFragment, Fragment fragment) {

        if (Others.isOnline(UsuarioActivity.this)) {

            voltar = true;
            if (verificaFragment(fragment)) {
                Log.i(TAG, "verificaFragment");
                removeFragment(fragment);

                if (iFragment.getTAG().equals(CategoriasFragment.TAG)) {
                    fragment = new CategoriasFragment();
                } else if (iFragment.getTAG().equals(FiltroFragment.TAG)) {
                    voltar = false;
                }

            } else if (!iFragment.getTAG().equals(CategoriaFragment.TAG))
                voltar = false;

            fragments.add(fragment);
            iFragments.add(iFragment);

            FragmentManager fragmentManager = getSupportFragmentManager();
            int count = fragmentManager.getBackStackEntryCount()-1;
            if(count>=0) {
                Log.i(TAG, "Count: " + count);
                FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(count);
                Log.i(TAG, "Name: " + backStackEntry.getName());
                int id = backStackEntry.getId();
                Log.i(TAG, "Id: " + id);
                fragmentManager.popBackStackImmediate(id, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

            if (voltar)
                fragmentTransaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
            else
                fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);

            fragmentTransaction.replace(frameLayout.getId(), fragment, iFragment.getTAG());
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            fragmentTransaction.addToBackStack(iFragment.getTAG());
            fragmentTransaction.commit();

            /*
            FragmentManager fragmentManager = getSupportFragmentManager();
            int count = fragmentManager.getBackStackEntryCount()-1;
            if(count>=0) {
                Log.i(TAG, "Count: " + count);
                FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(count);
                Log.i(TAG, "Name: " + backStackEntry.getName());
                int id = backStackEntry.getId();
                Log.i(TAG, "Id: " + id);
                fragmentManager.popBackStackImmediate(id, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }*/

            indexAtual = getIndexFragment(fragment);
            navigationView.getMenu().getItem(iFragment.getMenuItem()).setChecked(true);

            if (drawer.isDrawerOpen(GravityCompat.START))
                drawer.closeDrawer(GravityCompat.START);



        }else if(primeiroFrame) {
            primeiroFrame = false;
            fragments.add(fragment);
            iFragments.add(iFragment);

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);

            fragmentTransaction.replace(frameLayout.getId(), fragment, iFragment.getTAG());
            //fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            //fragmentTransaction.addToBackStack(iFragment.getTAG());
            fragmentTransaction.commit();
        }
        else {
            ToastUtil.toastCustom(UsuarioActivity.this, "Conecte-se com a Internet para poder utilizar outras funções do aplicativo!", false);
        }
    }


    public boolean verificaFragment(Fragment fragment){
        boolean possui = false;

        for(Fragment item : fragments){

            if(fragment.getClass().equals(item.getClass())){
                possui = true;
                break;
            }
        }

        return possui;
    }

    private int getIndexFragment(Fragment fragment){
        int index = 0;
        for(Fragment item : fragments){

            if(item.getClass().equals(fragment.getClass())){
                break;
            }

            index++;
        }

        return index;
    }

    private boolean voltarFragment(){
        if(Others.isOnline(this)) {
            if (fragments.size() > 1) {
                removeFragment(fragments.get(fragments.size()-1));
                Fragment fragment = fragments.get(fragments.size()-1);
                IFragment iFragment = iFragments.get(fragments.size()-1);
                Log.i(TAG, "voltarFragment: " + fragment.getClass());
                abrirFragment(iFragment, fragment);
                return false;
            } else {
                return true;
            }
        }else{
            if(fragments.size()<=1)
                return true;

            fragments.clear();
            iFragments.clear();

            CategoriaFragment fragment = new CategoriaFragment();
            fragments.add(fragment);
            iFragments.add(fragment);

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
            fragmentTransaction.replace(frameLayout.getId(), fragment, fragment.getTAG());
            fragmentTransaction.commit();

            return false;
        }
    }

    private void removeFragment(Fragment fragment){
        Log.i(TAG, "removeFragment: "+fragment.getClass());
        int index = getIndexFragment(fragment);
        //FragmentManager fm = getSupportFragmentManager();
        //fm.popBackStack(iFragments.get(index).getTAG(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fragments.remove(index);
        iFragments.remove(index);

    }
    //Navegação dos Fragments [Fim]


    public void setToolbarTitle(String title) {

        ViewGroup.LayoutParams params = linearLayoutToolbar.getLayoutParams();
        if(params.width != 0) {
            params.width = 0;
            linearLayoutToolbar.setLayoutParams(params);
            titleToolbar.setText("");
            subTitleToolbar.setText("");
            imgToolbar.setImageBitmap(null);
        }

        getSupportActionBar().setTitle("");
        getSupportActionBar().setTitle(title);

        Log.i("ASDAS", getSupportActionBar().getTitle().toString());
    }

    public LinearLayout getLinearLayoutToolbar() {
        return linearLayoutToolbar;
    }

    public TextView getTitleToolbar() {
        return titleToolbar;
    }

    public TextView getSubTitleToolbar() {
        return subTitleToolbar;
    }

    public ImageView getImgToolbar() {
        return imgToolbar;
    }

    public void setRecarregar(boolean recarregar) {
        this.recarregar = recarregar;
    }


    //Posições das categorias
    public void addPosition(PositionRecyclerView position){
        if(containPosition(position)){
            Log.i("CATEGORIA", " SET TAG: "+position.getTAG()+"\n Position: "+position.getPosition());
            positionRecyclerViews.set(getPositionIndex(position), position);
        }else{
            Log.i("CATEGORIA", " ADD TAG: "+position.getTAG()+"\n Position: "+position.getPosition());
            positionRecyclerViews.add(position);
        }
    }

    public int getPosition(String tag) {
        int position = 0;
        for (PositionRecyclerView positionRecyclerView : positionRecyclerViews) {
            if(tag.equals(positionRecyclerView.getTAG())){
                position = positionRecyclerView.getPosition();
            }
        }
        return position;
    }

    private boolean containPosition(PositionRecyclerView position){
        boolean contain=false;

        for(PositionRecyclerView positionRecyclerView : positionRecyclerViews){
            if(positionRecyclerView.getTAG().equals(position.getTAG())){
                contain = true;
                break;
            }
        }

        return contain;
    }

    private int getPositionIndex(PositionRecyclerView position){
        int index = 0;
        for (PositionRecyclerView positionRecyclerView : positionRecyclerViews) {
            if(position.getTAG().equals(positionRecyclerView.getTAG())){
                break;
            }
            index++;
        }
        return index;
    }


    //Posições das páginas
    public void addPagina(Pagina pag){
        if(containPagina(pag)){
            Log.i("PAGINA", " SET TAG: "+pag.getTAG()+"\n Página: "+pag.getPagina());
            paginas.set(getPaginaIndex(pag), pag);
        }else{
            Log.i("PAGINA", " ADD TAG: "+pag.getTAG()+"\n Página: "+pag.getPagina());
            paginas.add(pag);
        }
    }

    public int getPagina(String tag) {
        int position = 1;
        for (Pagina pagina : paginas) {
            if(tag.equals(pagina.getTAG())){
                position = pagina.getPagina();
            }
        }
        return position;
    }

    private boolean containPagina(Pagina pag){
        boolean contain=false;

        for(Pagina pagina : paginas){
            if(pagina.getTAG().equals(pag.getTAG())){
                contain = true;
                break;
            }
        }

        return contain;
    }

    private int getPaginaIndex(Pagina pag){
        int index = 0;
        for (Pagina pagina : paginas) {
            if(pag.getTAG().equals(pagina.getTAG())){
                break;
            }
            index++;
        }
        return index;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public static boolean isAppOpen() {
        return AppOpen;
    }

    public List<Categoria> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<Categoria> categorias) {
        this.categorias = categorias;
    }
}
