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
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import loureiro.enzo.usefi.activity.MainActivity;
import loureiro.enzo.usefi.activity.UsuarioActivity;
import loureiro.enzo.usefi.adapter.AdapterClientes;
import loureiro.enzo.usefi.adapter.AdapterHistorico;
import loureiro.enzo.usefi.broadcast.ConexaoWifiBroadcast;
import loureiro.enzo.usefi.broadcast.DataAtualDashboardBroadcast;
import loureiro.enzo.usefi.helper.Others;
import loureiro.enzo.usefi.model.Cliente;
import loureiro.enzo.usefi.model.Comerciante;
import loureiro.enzo.usefi.model.Conexao;
import loureiro.enzo.usefi.model.Usuario;
import loureiro.enzo.usefi.R;
import loureiro.enzo.usefi.util.AlarmUtil;
import loureiro.enzo.usefi.util.AlertaUtil;
import loureiro.enzo.usefi.util.AnimacaoUtil;
import loureiro.enzo.usefi.util.Base64Util;
import loureiro.enzo.usefi.util.ConfiguracaoFirebase;
import loureiro.enzo.usefi.util.DateUtil;
import loureiro.enzo.usefi.util.EquipeUseFiUtil;
import loureiro.enzo.usefi.util.FirebaseUtil;
import loureiro.enzo.usefi.util.PermissaoUtil;
import loureiro.enzo.usefi.util.ToastUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class DashboardFragment extends Fragment {

    private DatabaseReference clientesRef = ConfiguracaoFirebase.getReferenceFirebase();
    private ValueEventListener valueEventListenerClientes;

    private DatabaseReference conexoesRef;
    private ValueEventListener valueEventListenerConexoes;

    private List<Conexao> conexoes = new ArrayList<>();
    private List<Cliente> clientes = new ArrayList<>();
    private List<Cliente> clientesBroadCast = new ArrayList<>();
    private boolean possuiClientes;

    private PieChart pieChartGenero, pieChartIdade;
    private LineChart lineChartConexoes;
    private BarChart barChartPlataforma;

    private RecyclerView recyclerViewHistoricoConexoes;
    private AdapterHistorico adapter;

    private ProgressBar progressBarConexoes, progressBarHistorico, progressBarPieChart, progressBarBarChartIdade, progressBarLineChart, progressBarBarChart, progressBarFab;

    private boolean atualizar=true, atualizarOutros= true, atualzarConexoes = true;
    private ConstraintLayout cardConexoes, cardHistorico;
    private Switch tempoReal;
    public Date dateAtual;
    private TextView txtQuantConexoes;

    private ArrayList<ILineDataSet> dataSets;

    private boolean iniciado = false, broadcastAtivo;

    private FloatingActionButton fab, fabAtualizar;
    private final int REQUEST_PERMISSION_IMGBUTTONBANNER = 100;
    private final int REQUEST_GALERIA_IMGBUTTONBANNER = 200;

    public static final String TAG = "DASHBOARD_FRAGMENT", TAG_CONEXOES = "RECEIVER_CONEXOES";

    private Comerciante comerciante;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive");
            if (tempoReal.isChecked() || atualzarConexoes || txtQuantConexoes.getText().toString().equals("")) {
                int quantConexoes = 0;
                for (Cliente cliente : clientesBroadCast) {
                    if (cliente.getUltimaConexao() != null) {
                        Date dateUltimaConexao = DateUtil.dataCompletaToDate(cliente.getUltimaConexao());
                        long diferenca = dateAtual.getTime() - dateUltimaConexao.getTime();
                        Log.i(TAG, "Data atual: " + dateAtual.toString() + " | DataUltimaConexao: " + dateUltimaConexao.toString() + "\nDiferença: " + diferenca + " Valor: " + 2 * DateUtil.ONE_MINUTE_IN_MILLIS);
                        if (diferenca <= (DateUtil.ONE_MINUTE_IN_MILLIS * (EquipeUseFiUtil.TEMPO_CONEXAO_WIFI)) + DateUtil.ONE_MINUTE_IN_MILLIS / 2)
                            quantConexoes++;
                    }
                }


                AnimacaoUtil.progressBar(progressBarConexoes, txtQuantConexoes, false);
                AnimacaoUtil.progressBar(progressBarHistorico, recyclerViewHistoricoConexoes, false);
                txtQuantConexoes.setText(String.valueOf(quantConexoes));
                adapter.setDateAtual(dateAtual);
                adapter.notifyDataSetChanged();
                atualzarConexoes = false;

                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep((DateUtil.ONE_MINUTE_IN_MILLIS * EquipeUseFiUtil.TEMPO_CONEXAO_WIFI));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        dateAtual = Others.getNTPDate();
                        if(tempoReal.isChecked()) {
                            Intent broadcast = new Intent(getActivity().getApplicationContext(), DataAtualDashboardBroadcast.class);
                            AlarmUtil.cancel(getActivity().getApplicationContext(), broadcast, DataAtualDashboardBroadcast.REQUEST_CODE);
                            AlarmUtil.schedule(getActivity().getApplicationContext(), broadcast, System.currentTimeMillis(), DataAtualDashboardBroadcast.REQUEST_CODE);
                        }
                        Log.i(TAG, "dateAtual: " + dateAtual.toString());
                    }
                }.start();
            }
        }
    };

    public DashboardFragment() {
        // Required empty public constructor
        Log.i(TAG, "DashboardFragment");
    }

    public Comerciante getComerciante() {
        return comerciante;
    }

    public void setComerciante(Comerciante comerciante) {
        Log.i(TAG, "setComerciante");
        this.comerciante = comerciante;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        pieChartIdade = view.findViewById(R.id.pieChartIdade);
        pieChartGenero = view.findViewById(R.id.pieChart);
        lineChartConexoes = view.findViewById(R.id.lineChart);
        barChartPlataforma = view.findViewById(R.id.barChart);


        progressBarHistorico = view.findViewById(R.id.progressBarHistoricoConexoes);
        progressBarConexoes = view.findViewById(R.id.progressBarConexoes);
        progressBarPieChart = view.findViewById(R.id.progressBarPieChar);
        progressBarBarChartIdade = view.findViewById(R.id.progressBarPieCharIdade);
        progressBarLineChart = view.findViewById(R.id.progressBarLineChar);
        progressBarBarChart = view.findViewById(R.id.progressBarBarChar);
        progressBarFab = view.findViewById(R.id.progressBarFab);

        recyclerViewHistoricoConexoes = view.findViewById(R.id.reclycerViewHistorico);

        cardConexoes = view.findViewById(R.id.constraitConexoes);
        cardHistorico = view.findViewById(R.id.constraintHistorico);
        tempoReal = view.findViewById(R.id.switchTempoRealQuantConexoes);
        txtQuantConexoes = view.findViewById(R.id.txtQuantConexoes);

        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnimacaoUtil.progressBar(progressBarFab, fab, true);
                /*//Banner Imagem
                //Verificar as Permissões
                if(PermissaoUtil.validarPermissoes(permissoesNecessarias, DashboardFragment.this, REQUEST_PERMISSION_IMGBUTTONBANNER)){

                    Intent galeria = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    if(galeria.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivityForResult(galeria, REQUEST_GALERIA_IMGBUTTONBANNER);
                    }
                }*/

                //Banner Link
                final Dialog dialog = new Dialog(getActivity());
                dialog.setContentView(R.layout.dialog_text_input_layout);
                dialog.setCancelable(false);
                TextView title = dialog.findViewById(R.id.txtTitle);
                title.setText("Banner");
                TextView msg = dialog.findViewById(R.id.txtMsg);
                msg.setText("Informe o novo link para seu Banner:");
                final TextInputLayout tilLink = dialog.findViewById(R.id.tilCampo);
                tilLink.setHint("Link");
                Button cancell = dialog.findViewById(R.id.btnCancelarDialog);
                cancell.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AnimacaoUtil.progressBar(progressBarFab, fab, false);
                        dialog.dismiss();
                    }
                });
                Button enviar = dialog.findViewById(R.id.btnEnviarDialog);
                enviar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        String link = tilLink.getEditText().getText().toString();
                        DatabaseReference referenceBanner = ConfiguracaoFirebase.getReferenceFirebase().child(Comerciante.COMERCIANTES).child(comerciante.getId());
                        HashMap<String , Object> comercianteMap = new HashMap<>();
                        comercianteMap.put(Comerciante.BANNER, link);
                        referenceBanner.updateChildren(comercianteMap).addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                AnimacaoUtil.progressBar(progressBarFab, fab, false);
                                if(task.isSuccessful()){
                                    ToastUtil.toastCustom(getActivity(), "Sucesso ao atualizar link", true);
                                }
                                else
                                    ToastUtil.toastCustom(getActivity(), "Erro: "+task.getException().getMessage(), true);
                            }
                        });
                    }
                });
                dialog.show();
            }
        });
        fabAtualizar = view.findViewById(R.id.fabAtualizarDashBoard);
        fabAtualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    fabAtualizar.setEnabled(false);
                    clientesRef.removeEventListener(valueEventListenerClientes);
                    conexoesRef.removeEventListener(valueEventListenerConexoes);
                    AnimacaoUtil.progressBar(progressBarConexoes, txtQuantConexoes, true);
                    AnimacaoUtil.progressBar(progressBarHistorico, recyclerViewHistoricoConexoes, true);
                    AnimacaoUtil.progressBar(progressBarBarChartIdade, pieChartIdade, true);
                    AnimacaoUtil.progressBar(progressBarPieChart, pieChartGenero, true);
                    AnimacaoUtil.progressBar(progressBarLineChart, lineChartConexoes, true);
                    AnimacaoUtil.progressBar(progressBarBarChart, barChartPlataforma, true);
                    iniciado = true;
                    atualizar = true;
                    atualizarOutros = true;
                    atualzarConexoes = true;
                    broadcastAtivo = false;
                    recuperarDadosClientes();
                    recuperarDadosOutros();
                }catch (Exception e){
                    Log.i(TAG, "fabAtualizar: "+e.getMessage());
                }
            }
        });

        tempoReal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                clientesRef.removeEventListener(valueEventListenerClientes);
                conexoesRef.removeEventListener(valueEventListenerConexoes);

                if(isChecked){
                    //txtQuantConexoes.setText("");
                    AnimacaoUtil.progressBar(progressBarConexoes, txtQuantConexoes, true);
                    AnimacaoUtil.progressBar(progressBarHistorico, recyclerViewHistoricoConexoes, true);
                    broadcastAtivo = false;
                    recuperarDadosClientes();
                    recuperarDadosOutros();
                }else{
                    broadcastAtivo = true;
                }
            }
        });

        //Recycler View
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerViewHistoricoConexoes.setLayoutManager(layoutManager);
        recyclerViewHistoricoConexoes.setHasFixedSize(true);
        recyclerViewHistoricoConexoes.setItemAnimator(new DefaultItemAnimator());
        adapter = new AdapterHistorico(clientes, dateAtual, getActivity());
        recyclerViewHistoricoConexoes.setAdapter(adapter);

        comerciante = ((MainActivity) getActivity()).getComerciante();

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_PERMISSION_IMGBUTTONBANNER){//RequestCode do ImgButonLogo
            for(int permissaoResultado : grantResults){
                if(permissaoResultado == PackageManager.PERMISSION_DENIED) {
                    AlertaUtil.alertaErro("Permissão Negada","É necessário aceitar as permissões para uso do Aplicativo!",getContext());
                }else {
                    Intent galeria = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    if(galeria.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivityForResult(galeria, REQUEST_GALERIA_IMGBUTTONBANNER);
                    }
                }
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_GALERIA_IMGBUTTONBANNER) {//ResquetCode do ImgButton da Galeria

            if(resultCode == getActivity().RESULT_OK){
                try{
                    //Configurando Imagem [Início]
                    Bitmap imagem = null;
                    try {
                        Uri localImagem = data.getData();
                        imagem = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), localImagem);

                        FirebaseUtil.enviarBannerStorageComerciante(imagem, comerciante, getActivity(), progressBarFab, fab, false);
                    }
                    catch (Exception e){//Erro na Imagem
                        ToastUtil.toastCustom(getActivity(), "Erro Bitmap: "+e.getMessage(), false);
                    }
                    //Configurando Imagem [Fim]
                }
                catch (Exception ex){
                    ex.printStackTrace();
                }

            }else {
                ToastUtil.toastCustom(getActivity(),"É preciso adicionar um Banner!", false);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    //Recupera os dados dos Clientes do Firebase
    private void recuperarDadosClientes(){
        FirebaseAuth firebaseAuth = ConfiguracaoFirebase.getFirebaseAuth();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        String id = Base64Util.dadoToBase64(user.getEmail());

        clientesRef = ConfiguracaoFirebase.getReferenceFirebase().child(Comerciante.COMERCIANTES).child(id).child(Usuario.USUARIOS);
        Log.i(TAG, "clientesRef: "+clientesRef.toString());
        valueEventListenerClientes = clientesRef.orderByChild(Cliente.ULTIMA_CONEXAO).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                clientes.clear();
                for(DataSnapshot dados : dataSnapshot.getChildren()) {

                    Cliente cliente = dados.getValue(Cliente.class);
                    cliente.setId(dados.getKey());
                    cliente.setUltimaVisita(DateUtil.dataFirebaseToDataNormal(cliente.getUltimaVisita()));

                    clientes.add(cliente);
                }

                if(!broadcastAtivo || tempoReal.isChecked()) {
                    Log.i(TAG, "Broadcast enviado");
                    Intent broadcast = new Intent(getActivity().getApplicationContext(), DataAtualDashboardBroadcast.class);
                    AlarmUtil.cancel(getActivity().getApplicationContext(), broadcast, DataAtualDashboardBroadcast.REQUEST_CODE);
                    AlarmUtil.schedule(getActivity().getApplicationContext(), broadcast, System.currentTimeMillis(), DataAtualDashboardBroadcast.REQUEST_CODE);
                    broadcastAtivo = true;
                }

                possuiClientes = clientes.size()>0;
                Others.invertUsingCollectionsReverse(clientes);
                clientesBroadCast = clientes;
                Log.i(TAG, "Comerciante: "+comerciante.isPago() +" data: "+comerciante.getDataPagamento());
                dadosGraficosClientes();
                if(!tempoReal.isChecked())
                    clientesRef.removeEventListener(this);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void recuperarDadosOutros() {
        FirebaseAuth firebaseAuth = ConfiguracaoFirebase.getFirebaseAuth();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        String id = Base64Util.dadoToBase64(user.getEmail());

        conexoesRef = ConfiguracaoFirebase.getReferenceFirebase().child("Comerciantes").child(id).child("Conexoes");
        valueEventListenerConexoes = conexoesRef.limitToLast(4).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                conexoes.clear();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(dateAtual);
                int mes = calendar.get(Calendar.MONTH)+1;
                Log.i(TAG, "Mês: "+mes);
                boolean mesAtual = false;
                for(DataSnapshot dado : dataSnapshot.getChildren()){
                    Conexao conexao = new Conexao();
                    conexao.setMes(dado.getKey());
                    conexao.setQuant(Integer.parseInt(dado.getValue().toString()));
                    if(Integer.valueOf(conexao.getMes().substring(4))==mes)
                        mesAtual = true;
                    conexoes.add(conexao);
                }

                if(!mesAtual){
                    Conexao conexao = new Conexao();
                    conexao.setQuant(0);
                    String mesConexao = "";
                    if(mes<10)
                        mesConexao+="0"+mes;
                    else
                        mesConexao+=mes;

                    mesConexao+=calendar.get(Calendar.YEAR);
                    conexao.setMes(mesConexao);
                    conexoes.add(conexao);
                }

                if(atualizarOutros && conexoes.size()>0) {
                    atualizarOutros = false;
                    dadosGraficosOutros();
                }else if( tempoReal.isChecked()){
                    AnimacaoUtil.progressBar(progressBarLineChart, lineChartConexoes, true);
                    dadosGraficosOutros();
                }

                conexoesRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //Atribui dados de Clientes aos Gráficos
    private void dadosGraficosClientes() {

        if (!possuiClientes)
            ToastUtil.toastCustom(getActivity(), "Esses dados são apenas para demonstração. Regularize seu plano indo nas informações sobre seu Perfil!", false);
        else if (!comerciante.isPago()) {
            Log.i(TAG, "!comerciante.isPago()");
            possuiClientes = false;
            ToastUtil.toastCustom(getActivity(), "Esses dados são apenas para demonstração. Regularize seu plano no site!", false);
        }

        if(atualizar){
            try {
                graficoGenero();
            }catch (Exception e){
                Log.i(TAG, "Genero: "+e.getMessage());
            }
            try {
                graficoIdade();
            }catch (Exception e){
                Log.i(TAG, "Idade: "+e.getMessage());
            }
            try {
                graficoPlataforma();
            }catch (Exception e){
                Log.i(TAG, "Plataforma: "+e.getMessage());
            }

            atualizar = false;
        }
    }

    private void graficoIdade() {

        //Define como porcentagem
        pieChartIdade.setUsePercentValues(false);

        //Descrição
        Description description = new Description();
        description.setText("Idade dos Clientes");
        description.setTextSize(10f);
        description.setTextAlign(Paint.Align.RIGHT);

        pieChartIdade.getDescription().setEnabled(false);
        //pieChart.setDescription(description);

        //pieChartIdade.setExtraOffsets(5,6,5,5);
        pieChartIdade.setDragDecelerationFrictionCoef(0.99f);
        pieChartIdade.setEntryLabelTextSize(0f);

        Legend l = pieChartIdade.getLegend();
        l.setFormSize(10f); // set the size of the legend forms/shapes
        l.setForm(Legend.LegendForm.CIRCLE); // set what type of form/shape should be used
        l.setPosition(Legend.LegendPosition.BELOW_CHART_LEFT);
        //l.setTypeface(...);
        l.setTextSize(14f);
        l.setTextColor(Color.BLACK);
        l.setXEntrySpace(10f); // set the space between the legend entries on the x-axis
        l.setYEntrySpace(5f); // set the space between the legend entries on the y-axis
        l.setWordWrapEnabled(true);

        //Hole = circulo no meio
        pieChartIdade.setDrawHoleEnabled(true);
        pieChartIdade.setHoleColor(Color.WHITE);
        pieChartIdade.setHoleRadius(40f);

        pieChartIdade.setTransparentCircleRadius(49f);

        //Texto no Centro
        /*pieChartGenero.setCenterText("Gênero");
        pieChartGenero.setCenterTextSize(20);*/

        //Animação
        pieChartIdade.animateY(1000, Easing.EasingOption.EaseInOutCubic);

        ArrayList<PieEntry> valores = new ArrayList<>();

        int quantA = 0,//10-14
                quantB = 0,//15-19
                quantC = 0,//20-25
                quantD = 0,//26-31
                quantE = 0,//32-41
                quantF = 0,//42-50
                quantG = 0,//51-59
                quantH = 0;//60+

        for(Cliente cliente : clientes){
            int idade = DateUtil.getIdade(dateAtual, DateUtil.dataNormalToDate(cliente.getIdade()));
            if(idade<=14)
                quantA++;
            else if(idade<=19)
                quantB++;
            else if(idade <=25)
                quantC++;
            else if(idade <= 31)
                quantD++;
            else if(idade <=41)
                quantE++;
            else if(idade <= 50)
                quantF++;
            else if(idade <= 59)
                quantG++;
            else
                quantH++;
        }

        if(!possuiClientes){
          quantA = 5;
          quantB = 25;
          quantC = 20;
          quantD = 15;
          quantE = 13;
          quantF = 10;
          quantG = 7;
          quantH = 11;
        }


        if(quantA!=0)
        valores.add(new PieEntry(quantA, "10 à 14"));
        if(quantB!=0)
        valores.add(new PieEntry(quantB, "15 à 19"));
        if(quantC!=0)
        valores.add(new PieEntry(quantC, "20 à 25"));
        if(quantD!=0)
        valores.add(new PieEntry(quantD, "26 à 31"));
        if(quantE!=0)
        valores.add(new PieEntry(quantE, "32 à 41"));
        if(quantF!=0)
        valores.add(new PieEntry(quantF, "42 à 50"));
        if(quantG!=0)
        valores.add(new PieEntry(quantG, "51 à 59"));
        if(quantH!=0)
        valores.add(new PieEntry(quantH, "60+"));

        PieDataSet dataSet = new PieDataSet(valores, null);


        //colors to dataset
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(getResources().getColor(R.color.colorCinzaRipple));
        colors.add(getResources().getColor(R.color.colorCinza));
        colors.add(getResources().getColor(R.color.colorCinzaPressed));
        colors.add(getResources().getColor(R.color.colorVermelho));
        colors.add(getResources().getColor(R.color.colorVermelhoPressed));
        colors.add(getResources().getColor(R.color.colorAzulRipple));
        colors.add(getResources().getColor(R.color.colorAzul));
        colors.add(getResources().getColor(R.color.colorAzulPressed));


        dataSet.setColors(colors);
        dataSet.setHighlightEnabled(true);
        dataSet.setSliceSpace(2f); //Espaço entre as marcações/cores
        dataSet.setSelectionShift(5f); //Tamanho da seleção
        //dataSet.setColors(ColorTemplate.MATERIAL_COLORS); //Cores do gráfico*/

        PieData pieData = new PieData(dataSet);
        pieData.setValueTextSize(18f); //Texto dentro do Gráfico
        pieData.setValueTextColor(Color.WHITE);


        //pieChartIdade.getLegend().setEnabled(false);
        pieChartIdade.setData(pieData);


        AnimacaoUtil.progressBar(progressBarBarChartIdade, pieChartIdade, false);
    }

    private void graficoGenero() {

        //Define como porcentagem
        pieChartGenero.setUsePercentValues(true);

        //Descrição
        Description description = new Description();
        description.setText("Gênero dos Clientes");
        description.setTextSize(10f);
        description.setTextAlign(Paint.Align.RIGHT);

        pieChartGenero.getDescription().setEnabled(false);
        //pieChart.setDescription(description);

        pieChartGenero.setExtraOffsets(5,6,5,5);

        pieChartGenero.setDragDecelerationFrictionCoef(0.99f);

        //Hole = circulo no meio
        pieChartGenero.setDrawHoleEnabled(true);
        pieChartGenero.setHoleColor(Color.WHITE);
        pieChartGenero.setHoleRadius(40f);

        pieChartGenero.setTransparentCircleRadius(49f);

        //Texto no Centro
        /*pieChartGenero.setCenterText("Gênero");
        pieChartGenero.setCenterTextSize(20);*/

        //Animação
        pieChartGenero.animateY(1000, Easing.EasingOption.EaseInOutCubic);

        ArrayList<PieEntry> valores = new ArrayList<>();

        int quantM = 0, quantF = 0, quantO =0;

        for(Cliente cliente : clientes){
            if(cliente.getGenero().equals(Cliente.MASCULINO))
                quantM++;
            else if(cliente.getGenero().equals(Cliente.FEMINO))
                quantF++;
            else
                quantO++;
        }

        if(!possuiClientes){
            quantF = 10;
            quantM = 12;
            quantO = 5;
        }

        if(quantM!=0)
        valores.add(new PieEntry(quantM, "Masculino"));
        if(quantF!=0)
        valores.add(new PieEntry(quantF, "Feminino"));
        if(quantO!=0)
        valores.add(new PieEntry(quantO, "Outro"));

        PieDataSet dataSet = new PieDataSet(valores, null);


        //colors to dataset
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(getResources().getColor(R.color.color3));
        colors.add(getResources().getColor(R.color.color2));
        colors.add(getResources().getColor(R.color.color1));


        dataSet.setColors(colors);
        dataSet.setHighlightEnabled(true);
        dataSet.setSliceSpace(3f); //Espaço entre as marcações/cores
        dataSet.setSelectionShift(5f); //Tamanho da seleção
        //dataSet.setColors(ColorTemplate.MATERIAL_COLORS); //Cores do gráfico*/

        PieData pieData = new PieData(dataSet);
        pieData.setValueTextSize(18f); //Texto dentro do Gráfico
        pieData.setValueTextColor(Color.WHITE);

        pieChartGenero.getLegend().setEnabled(false);
        pieChartGenero.setData(pieData);

        AnimacaoUtil.progressBar(progressBarPieChart, pieChartGenero, false);
    }

    private void graficoPlataforma() {

        ArrayList<BarEntry> valores = new ArrayList<>();
        float spaceBarForBar = 1f;

        int quantA = 0, quantI = 0, quantO = 0;

        if(possuiClientes)
        for(Cliente cliente: clientes){

            if(cliente.getPlataforma().equals(Cliente.ANDROID))
                quantA++;
            else if(cliente.getPlataforma().equals(Cliente.IOS))
                quantI++;
            else
                quantO++;
        }
        else{
            quantA = 10;
            quantI = 7;
            quantO = 6;
        }

        if(quantA!=0)
        valores.add(new BarEntry(0*spaceBarForBar, quantA));
        if(quantI!=0)
        valores.add(new BarEntry(1*spaceBarForBar, quantI));
        if(quantO!=0)
        valores.add(new BarEntry(2*spaceBarForBar, quantO));

        BarDataSet barDataSet = new BarDataSet(valores, "");

        //add colors to dataset
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(getResources().getColor(R.color.color3));
        colors.add(getResources().getColor(R.color.color2));
        colors.add(getResources().getColor(R.color.color1));

        barDataSet.setColors(colors);
        barDataSet.setValueTextSize(15);

        BarData data = new BarData(barDataSet);
        //data.setBarWidth(20f);
        barChartPlataforma.setData(data);
        barChartPlataforma.getLegend().setEnabled(false);
        barChartPlataforma.getDescription().setEnabled(false);

        String[] values = new String[]{Cliente.ANDROID, Cliente.IOS, Cliente.OUTROS};

        XAxis xAxis = barChartPlataforma.getXAxis();
        xAxis.setTextSize(10f);
        xAxis.setValueFormatter(new MyXAxisValueFormatter(values));
        xAxis.setGranularity(1);
        //xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);;
        AnimacaoUtil.progressBar(progressBarBarChart, barChartPlataforma, false);
    }


    //Atribui outros dados aos Gráficos
    private void dadosGraficosOutros(){
        fabAtualizar.setEnabled(true);

        try {
            graficoConexoes();
        }catch (Exception e){
            Log.i(TAG, "Conexoes: "+e.getMessage());
        }

    }

    private void graficoConexoes() {

        lineChartConexoes.setDragEnabled(true);
        lineChartConexoes.setScaleEnabled(false);
        lineChartConexoes.setDescription(null);


        YAxis leftAxis = lineChartConexoes.getAxisLeft();
        leftAxis.removeAllLimitLines();
        //leftAxis.setAxisMaximum(60f); //Máximo que irá mostrar
        //leftAxis.setAxisMinimum(10f); //Mínimo que irá mostrar
        leftAxis.enableGridDashedLine(10f, 10f, 0);
        leftAxis.setDrawLimitLinesBehindData(true);

        lineChartConexoes.getAxisRight().setEnabled(false);//Habilita / debasilita a barra da direita
        ArrayList<Entry> valores = new ArrayList<>();
        String[] values = new String[conexoes.size()];

        if (possuiClientes)
            for (int i = 0; i < conexoes.size(); i++) {
                Conexao conexao = conexoes.get(i);
                values[i] = conexao.getMesGrafico();
                valores.add(new Entry(i, conexao.getQuant()));
            }
        else {
            valores.add(new Entry(0, 5));
            valores.add(new Entry(1, 10));
            valores.add(new Entry(2, 15));
            valores.add(new Entry(3, 30));

            values = new String[4];
            values[0] = "06/2018";
            values[1] = "07/2018";
            values[2] = "08/2018";
            values[3] = "09/2018";

        }

        LineDataSet dataSet = new LineDataSet(valores, null);
        dataSet.setFillAlpha(110);
        dataSet.setValueTextSize(14f);
        int color = getResources().getColor(R.color.color1);
        int colorDark = getResources().getColor(R.color.color4);
        dataSet.setColor(color);
        dataSet.setValueTextColor(color);
        dataSet.setCircleColor(colorDark);

        dataSets = new ArrayList<>();
        dataSets.add(dataSet);

        LineData data = new LineData(dataSets);
        lineChartConexoes.setData(data);
        lineChartConexoes.getLegend().setEnabled(false);


        Log.i(TAG, "Conexoes, values size: " + values.length + " conexoes size: " + conexoes.size());
        XAxis xAxis = lineChartConexoes.getXAxis();
        xAxis.setValueFormatter(new MyXAxisValueFormatter(values));
        xAxis.setGranularity(1);
        //xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        AnimacaoUtil.progressBar(progressBarLineChart, lineChartConexoes, false);

    }


    public class MyXAxisValueFormatter implements IAxisValueFormatter {

        private String[] mValues;

        public MyXAxisValueFormatter(String[] values){
            this.mValues = values;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            return mValues[(int) value];
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
        if(!iniciado) {
            AnimacaoUtil.progressBar(progressBarConexoes, txtQuantConexoes, true);
            AnimacaoUtil.progressBar(progressBarHistorico, recyclerViewHistoricoConexoes, true);
            AnimacaoUtil.progressBar(progressBarBarChartIdade, pieChartIdade, true);
            AnimacaoUtil.progressBar(progressBarPieChart, pieChartGenero, true);
            AnimacaoUtil.progressBar(progressBarLineChart, lineChartConexoes, true);
            AnimacaoUtil.progressBar(progressBarBarChart, barChartPlataforma, true);
            iniciado = true;
            getActivity().registerReceiver(broadcastReceiver, new IntentFilter(TAG));
            broadcastAtivo = false;
            recuperarDadosClientes();
            recuperarDadosOutros();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
        try {
            clientesRef.removeEventListener(valueEventListenerClientes);
            conexoesRef.removeEventListener(valueEventListenerConexoes);
            broadcastAtivo = true;
            getActivity().unregisterReceiver(broadcastReceiver);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
