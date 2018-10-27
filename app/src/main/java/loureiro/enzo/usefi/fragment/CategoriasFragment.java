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
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
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
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItem;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import loureiro.enzo.usefi.activity.UsuarioActivity;
import loureiro.enzo.usefi.adapter.AdapterString;
import loureiro.enzo.usefi.adapter.AdapterWifis;
import loureiro.enzo.usefi.broadcast.ConexaoWifiBroadcast;
import loureiro.enzo.usefi.broadcast.VerificarWifiOff;
import loureiro.enzo.usefi.model.Categoria;
import loureiro.enzo.usefi.model.Usuario;
import loureiro.enzo.usefi.service.ConexaoWifi;
import loureiro.enzo.usefi.clickListener.RecyclerItemClickListener;
import loureiro.enzo.usefi.helper.Others;
import loureiro.enzo.usefi.myInterface.IFragment;
import loureiro.enzo.usefi.model.Comerciante;
import loureiro.enzo.usefi.model.Wifi;
import loureiro.enzo.usefi.R;
import loureiro.enzo.usefi.service.JobConexaoWifi;
import loureiro.enzo.usefi.util.AlarmUtil;
import loureiro.enzo.usefi.util.AlertaUtil;
import loureiro.enzo.usefi.util.AnimacaoUtil;
import loureiro.enzo.usefi.util.BancoDadosUtil;
import loureiro.enzo.usefi.util.Base64Util;
import loureiro.enzo.usefi.util.ConfiguracaoFirebase;
import loureiro.enzo.usefi.util.DateUtil;
import loureiro.enzo.usefi.util.EquipeUseFiUtil;
import loureiro.enzo.usefi.util.PermissaoUtil;
import loureiro.enzo.usefi.util.SharedPreferencesUtil;
import loureiro.enzo.usefi.util.ToastUtil;

import static loureiro.enzo.usefi.service.JobConexaoWifi.DATA_ULTIMA_CONEXAO;

/**
 * A simple {@link Fragment} subclass.
 */
public class CategoriasFragment extends Fragment implements IFragment{

    private ViewPager viewPager;
    private SmartTabLayout smartTabLayout;

    private FragmentPagerItems items;
    private FragmentPagerItemAdapter adapter;
    private DatabaseReference referenceCategorias;
    private ValueEventListener valueEventListenerCategorias;

    public static final String TAG = "CATEGORIAS_FRAGMENT",CATEGORIA="CATEGORIA";
    private static final int itemMenu = 0;

    public CategoriasFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        Animation animation = super.onCreateAnimation(transit, enter, nextAnim);

        if (animation == null && nextAnim != 0) {
            animation = AnimationUtils.loadAnimation(getActivity(), nextAnim);
        }

        if (animation != null && getView()!=null) {
            getView().setLayerType(View.LAYER_TYPE_HARDWARE, null);

            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                public void onAnimationEnd(Animation animation) {
                    if(getView()!=null)
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
        Log.i(TAG, "onCreateView");

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_categorias, container, false);

        viewPager = view.findViewById(R.id.viewPager);
        viewPager.setAdapter(null);
        smartTabLayout = view.findViewById(R.id.viewPagerTab);
        smartTabLayout.setViewPager(viewPager);
        smartTabLayout.setOnTabClickListener(new SmartTabLayout.OnTabClickListener() {
            @Override
            public void onTabClicked(int position) {
                SharedPreferencesUtil.gravarValor(getActivity(),CATEGORIA , position);
            }
        });
        smartTabLayout.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                SharedPreferencesUtil.gravarValor(getActivity(),CATEGORIA , position);
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        items = new FragmentPagerItems(getContext());
        adapter = new FragmentPagerItemAdapter(getFragmentManager(), items);

        recuperarCategorias();

        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
        recuperarCategorias();
        setToolbarTitle();
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.i(TAG, "onStop");
        super.onStop();
        if(valueEventListenerCategorias!=null)
            referenceCategorias.removeEventListener(valueEventListenerCategorias);

        items.clear();
        adapter.notifyDataSetChanged();
        viewPager.removeAllViews();
        smartTabLayout.removeAllViews();
    }

    private void recuperarCategorias() {
        Log.i(TAG, "recuperarCategorias");

        referenceCategorias = ConfiguracaoFirebase.getReferenceFirebase().child(EquipeUseFiUtil.UseFi).child(Comerciante.CATEGORIAS);
        valueEventListenerCategorias = referenceCategorias.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                items.clear();
                viewPager.setAdapter(null);
                smartTabLayout.setViewPager(viewPager);

                List<Categoria> categorias = new ArrayList<>();
                for (DataSnapshot dado : dataSnapshot.getChildren()) {
                    Categoria categoria = new Categoria();
                    categoria.setNome(dado.getValue().toString());
                    categoria.setId(dado.getKey());
                    boolean adicionar = true;
                    switch (categoria.getId()) {
                        case Comerciante.TODOS_ID:
                            Comerciante.TODOS = categoria.getNome();
                            break;
                        case Comerciante.POPULAR_ID:
                            Comerciante.POPULAR = categoria.getNome();
                            break;
                        case Comerciante.NOVOS_ID:
                            Comerciante.NOVOS = categoria.getNome();
                            break;
                            default:
                                adicionar = false;
                                break;
                    }

                    categorias.add(categoria);

                    if(adicionar) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(Categoria.CATEGORIA, categoria);
                        items.add(FragmentPagerItem.of(categoria.getNome(), CategoriaFragment.class, bundle));
                        Log.i(TAG, "Nome: " + categoria.getNome()+" Id: "+categoria.getId());
                    }
                    //Log.i(TAG, "Nome: " + categoria.getNome()+" Id: "+categoria.getId());
                }
                ((UsuarioActivity)getActivity()).setCategorias(categorias);
                adapter.notifyDataSetChanged();
                viewPager.setAdapter(adapter);
                /*
                int pagina = 0;

                if(SharedPreferencesUtil.contemValor(getActivity(), CATEGORIA))
                    pagina = SharedPreferencesUtil.lerValorInt(getActivity(), CATEGORIA);
                viewPager.setCurrentItem(pagina);*/
                smartTabLayout.setViewPager(viewPager);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public String getTAG() {
        return TAG;
    }

    @Override
    public int getMenuItem() {
        return itemMenu;
    }

    @Override
    public void setToolbarTitle() {
        ((UsuarioActivity)getActivity()).setToolbarTitle("Início");
    }

    public static CategoriasFragment newInstance(){
        CategoriasFragment fragment = new CategoriasFragment();
        return fragment;
    }
}
