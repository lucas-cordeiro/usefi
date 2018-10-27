package loureiro.enzo.usefi.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import loureiro.enzo.usefi.activity.UsuarioActivity;
import loureiro.enzo.usefi.adapter.AdapterPromocoes;
import loureiro.enzo.usefi.myInterface.IFragment;
import loureiro.enzo.usefi.model.Notificacao;
import loureiro.enzo.usefi.model.PositionRecyclerView;
import loureiro.enzo.usefi.model.Usuario;
import loureiro.enzo.usefi.R;
import loureiro.enzo.usefi.util.Base64Util;
import loureiro.enzo.usefi.util.ConfiguracaoFirebase;
import loureiro.enzo.usefi.util.ToastUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class PromocoesFragment extends Fragment implements IFragment{


    private int menuItem = 3;
    private static final String TAG = "PROMOCOES_FRAGMENT";

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipe;
    private List<Notificacao> notificacoes = new ArrayList<>();
    public int currentVisiblePosition;
    private AdapterPromocoes adapter;

    private DatabaseReference referencePromocoes;
    private ValueEventListener valueEventListenerPromocoes;

    public PromocoesFragment() {
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
        View view = inflater.inflate(R.layout.fragment_promocoes, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewPromocoes);
        swipe = view.findViewById(R.id.swipePromocoes);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                recuperarPromocoes();
            }
        });

        adapter = new AdapterPromocoes(notificacoes, getActivity());
        currentVisiblePosition = ((UsuarioActivity) getActivity()).getPosition(TAG);

        //Recycler View
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        //recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        return view;
    }


    private void recuperarPromocoes() {
        swipe.setRefreshing(true);
        new Thread(){
            @Override
            public void run() {
                FirebaseAuth firebaseAuth = ConfiguracaoFirebase.getFirebaseAuth();
                FirebaseUser user = firebaseAuth.getCurrentUser();
                String id = Base64Util.dadoToBase64(user.getEmail());
                referencePromocoes = ConfiguracaoFirebase.getReferenceFirebase().child(Usuario.USUARIOS).child(id).child(Usuario.NOTIFICACOES);
                valueEventListenerPromocoes = referencePromocoes.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        notificacoes.clear();

                        for(DataSnapshot dado : dataSnapshot.getChildren()){
                            Notificacao notificacao = dado.getValue(Notificacao.class);
                            notificacao.setId(dado.getKey());
                            notificacoes.add(notificacao);
                        }

                        if(notificacoes.size()<1)
                            semPromocoes();

                        adapter.notifyDataSetChanged();
                        swipe.setRefreshing(false);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }.start();
    }

    private void semPromocoes(){
        ToastUtil.toastCustom(getActivity(), "Por enquanto não há promoções disponíveis para você.", true);
    }

    @Override
    public void onStart() {
        super.onStart();
        setToolbarTitle();
        recuperarPromocoes();
    }


    @Override
    public void onStop() {
        super.onStop();
        if (valueEventListenerPromocoes != null)
            referencePromocoes.removeEventListener(valueEventListenerPromocoes);
        int position = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition();
        Log.i(TAG, "onStop: " + position);
        ((UsuarioActivity) getActivity()).addPosition(new PositionRecyclerView(TAG, position));
    }


    @Override
    public String getTAG() {
        return TAG;
    }

    @Override
    public int getMenuItem() {
        return menuItem;
    }

    @Override
    public void setToolbarTitle() {
        ((UsuarioActivity) getActivity()).setToolbarTitle("Promoções");
    }
}
