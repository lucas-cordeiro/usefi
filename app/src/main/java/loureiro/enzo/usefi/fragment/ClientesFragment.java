package loureiro.enzo.usefi.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import loureiro.enzo.usefi.activity.MainActivity;
import loureiro.enzo.usefi.adapter.AdapterClientes;
import loureiro.enzo.usefi.helper.Others;
import loureiro.enzo.usefi.model.Cliente;
import loureiro.enzo.usefi.model.Comerciante;
import loureiro.enzo.usefi.R;
import loureiro.enzo.usefi.util.AnimacaoUtil;
import loureiro.enzo.usefi.util.Base64Util;
import loureiro.enzo.usefi.util.ConfiguracaoFirebase;
import loureiro.enzo.usefi.util.DateUtil;
import loureiro.enzo.usefi.util.FirebaseUtil;
import loureiro.enzo.usefi.util.PlanoUtil;
import loureiro.enzo.usefi.util.ToastUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class ClientesFragment extends Fragment {

    private DatabaseReference clientesRef = ConfiguracaoFirebase.getReferenceFirebase(), usuariosRef = ConfiguracaoFirebase.getReferenceFirebase();
    private ValueEventListener valueEventListenerClientes, valueEventListenerUsuarios;

    private RecyclerView recyclerView;
    private AdapterClientes adapter;
    List<Cliente> clientes = new ArrayList<>();
    public Date dateAtual;

    private ProgressBar progressBar;

    public Comerciante comerciante;

    private static final String TAG = "CLIENTES_FRAGMENT";

    public ClientesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_clientes, container, false);
        recyclerView = view.findViewById(R.id.recyclerClientes);
        progressBar = view.findViewById(R.id.progressBarRecyclerViewClientes);

        //Recycler View
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        //Recuperar a Data Atual e configurar o Adapter
        adapter = new AdapterClientes(clientes, dateAtual, getActivity());
        recyclerView.setAdapter(adapter);

        comerciante = ((MainActivity) getActivity()).getComerciante();

        recuperarDadosClientes();
        //RecyclerView [Fim]

        return view;
    }


    private void recuperarDadosClientes(){

        AnimacaoUtil.progressBar(progressBar, recyclerView, true);

        if(FirebaseUtil.verificarPlanoComerciante(comerciante, false, getActivity(), dateAtual) && !comerciante.getPlano().equals(PlanoUtil.BASIC)) {

            FirebaseAuth firebaseAuth = ConfiguracaoFirebase.getFirebaseAuth();
            FirebaseUser user = firebaseAuth.getCurrentUser();
            String id = Base64Util.dadoToBase64(user.getEmail());

            clientesRef = clientesRef.child("Comerciantes").child(id).child("Usuarios");
            valueEventListenerClientes = clientesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    clientes.clear();
                    for (DataSnapshot dados : dataSnapshot.getChildren()) {
                        Cliente cliente = dados.getValue(Cliente.class);
                        cliente.setId(dados.getKey());
                        cliente.setUltimaVisita(DateUtil.dataFirebaseToDataNormal(cliente.getUltimaVisita()));

                        clientes.add(cliente);
                        Log.i(TAG, "Última visita: " + cliente.getUltimaVisita());
                    }

                    adapter.notifyDataSetChanged();

                    if (clientes.size() == 0) {
                        ToastUtil.toastCustom(getActivity(), "Esses dados são apenas para demonstração. Regularize seu plano indo nas informações sobre seu Perfil!", false);
                        dadosExemplo();
                    }
                    else
                        AnimacaoUtil.progressBar(progressBar, recyclerView, false);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        else {
            if (comerciante.getPlano().equals(PlanoUtil.BASIC))
                ToastUtil.toastCustom(getActivity(), " Seu plano não possui a função de Clientes. Atualize para o Plano Standard ou Premium!\n Dados de demonstração.", false);
            else
                ToastUtil.toastCustom(getActivity(), "Esses dados são apenas para demonstração. Regularize seu plano indo nas informações sobre seu Perfil!\n Dados de demonstração.", false);
            dadosExemplo();
        }
    }

    private void dadosExemplo(){
        valueEventListenerUsuarios = usuariosRef.child("Usuarios").limitToFirst(4).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {

                new Thread(){
                    @Override
                    public void run() {

                        for(DataSnapshot dado : dataSnapshot.getChildren()){
                            Log.i(TAG, "dado: "+dado.toString());

                            Cliente cliente = dado.getValue(Cliente.class);
                            cliente.setUltimaVisita(DateUtil.dataDateToDataNormal(dateAtual));
                            clientes.add(cliente);
                        }

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                                AnimacaoUtil.progressBar(progressBar, recyclerView, false);
                            }
                        });

                    }
                }.start();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (valueEventListenerClientes != null)
            clientesRef.removeEventListener(valueEventListenerClientes);

        if (valueEventListenerUsuarios != null)
            usuariosRef.removeEventListener(valueEventListenerUsuarios);

    }
}
