package loureiro.enzo.usefi.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.Date;

import loureiro.enzo.usefi.fragment.ClientesFragment;
import loureiro.enzo.usefi.fragment.DashboardFragment;
import loureiro.enzo.usefi.fragment.NotificacaoFragment;
import loureiro.enzo.usefi.fragment.SuporteFragment;
import loureiro.enzo.usefi.helper.Others;
import loureiro.enzo.usefi.model.Comerciante;
import loureiro.enzo.usefi.R;
import loureiro.enzo.usefi.util.Base64Util;
import loureiro.enzo.usefi.util.ConfiguracaoFirebase;
import loureiro.enzo.usefi.util.EquipeUseFiUtil;
import loureiro.enzo.usefi.util.FirebaseUtil;
import loureiro.enzo.usefi.util.SharedPreferencesUtil;

public class MainActivity extends AppCompatActivity {

    Comerciante comerciante;
    private static final String TAG = "MAIN_ACTIVITY";

    private DatabaseReference referenceUser;
    private ValueEventListener eventListenerRecuperarComerciante;

    private Fragment fragmentAtual;
    private int indexAtual;

    public Date date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ToolBar [Início]
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        //Toolbar [Fim]
    }


    //Método para configurar o Bottom Navigation View
    private void configurarBottomNavigationView() {
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bnve);

        //Configurar a animação
        bottomNavigationViewEx.enableAnimation(false);
        bottomNavigationViewEx.enableItemShiftingMode(true);
        bottomNavigationViewEx.enableShiftingMode(false);
        bottomNavigationViewEx.setTextVisibility(true);

        //Habilitar a navegação
        navegacaoBNVE(bottomNavigationViewEx);
    }

    //Configura o fragmentPadrao para quando a aplicação iniciar
    private void fragmentPadrao(Fragment fragment, int index){
        Log.i(TAG, "fragmentPadrao");
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.viewPager,fragment).commit();

        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bnve);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(index);
        menuItem.setChecked(true);
    }

    //Método dos eventos de clique do Bottom Navigation View
    private void navegacaoBNVE(BottomNavigationViewEx viewEx){
        viewEx.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                switch (item.getItemId()){
                    case R.id.ic_dashboard:
                        Fragment dashboard = new DashboardFragment();
                        ((DashboardFragment) dashboard).setComerciante( MainActivity.this.comerciante);
                        ((DashboardFragment) dashboard).dateAtual = date;
                        fragmentAtual = dashboard;
                        indexAtual = 0;
                        fragmentTransaction.replace(R.id.viewPager, dashboard).commit();
                        return true;

                    case R.id.ic_clientes:
                        ClientesFragment clientes = new ClientesFragment();
                        clientes.comerciante = MainActivity.this.comerciante;
                        clientes.dateAtual = date;
                        fragmentAtual = clientes;
                        indexAtual = 1;
                        fragmentTransaction.replace(R.id.viewPager, clientes).commit();
                        return true;

                    case R.id.ic_notificacao:
                        NotificacaoFragment notificacao = new NotificacaoFragment();
                        notificacao.comerciante = MainActivity.this.comerciante;
                        notificacao.dateAtual = date;
                        fragmentAtual = notificacao;
                        indexAtual = 2;
                        fragmentTransaction.replace(R.id.viewPager, notificacao).commit();
                        return true;

                    case R.id.ic_suporte:
                        Fragment suporte = new SuporteFragment();
                        fragmentAtual = suporte;
                        indexAtual = 3;
                        fragmentTransaction.replace(R.id.viewPager, suporte).commit();
                        return true;
                }


                return false;
            }
        });
    }

    private void recuperarComercianteLogado() {


        FirebaseAuth firebaseAuth = ConfiguracaoFirebase.getFirebaseAuth();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        String id = Base64Util.dadoToBase64(user.getEmail());

        referenceUser = ConfiguracaoFirebase.getReferenceFirebase().child("Comerciantes").child(id);

        eventListenerRecuperarComerciante = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                new Thread() {
                    @Override
                    public void run() {
                        comerciante = dataSnapshot.getValue(Comerciante.class);
                        comerciante.setId(dataSnapshot.getKey());
                        SharedPreferencesUtil.gravarValor(getApplicationContext(), EquipeUseFiUtil.CONTA, Comerciante.COMERCIANTES);

                        if (date == null) {
                            date = Others.getNTPDate();
                            Log.i("Others_Data", "date: " + date.toString());
                        }

                        if (!comerciante.getDataPagamento().equals(Comerciante.DATA_NULL)) {
                            FirebaseUtil.verificarPlanoComerciante(comerciante, false, MainActivity.this, date);
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //Configurar Bottom Navigation View
                                configurarBottomNavigationView();

                                //Seta fragment Padrão
                                if (fragmentAtual == null) {
                                    DashboardFragment fragment = new DashboardFragment();
                                    fragment.dateAtual = date;
                                    fragment.setComerciante(MainActivity.this.comerciante);
                                    fragmentAtual = fragment;
                                    indexAtual = 0;
                                }

                                fragmentPadrao(fragmentAtual, indexAtual);
                            }
                        });


                    }
                }.start();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        referenceUser.addListenerForSingleValueEvent(eventListenerRecuperarComerciante);
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
                Intent intent = new Intent(MainActivity.this, PerfilActivity.class);
                intent.putExtra("date", date);
                startActivity(intent);
                break;

            case R.id.menu_sair://Sair
                FirebaseUtil.deslogarConta(MainActivity.this);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarComercianteLogado();
    }

    @Override
    protected void onStop() {
        if(eventListenerRecuperarComerciante!=null)
            referenceUser.removeEventListener(eventListenerRecuperarComerciante);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public Comerciante getComerciante() {
        return comerciante;
    }
}
