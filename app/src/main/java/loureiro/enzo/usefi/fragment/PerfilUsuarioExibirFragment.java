package loureiro.enzo.usefi.fragment;


import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import loureiro.enzo.usefi.helper.Others;
import loureiro.enzo.usefi.model.Cliente;
import loureiro.enzo.usefi.model.Usuario;
import loureiro.enzo.usefi.R;
import loureiro.enzo.usefi.util.AnimacaoUtil;
import loureiro.enzo.usefi.util.Base64Util;
import loureiro.enzo.usefi.util.ConfiguracaoFirebase;
import loureiro.enzo.usefi.util.DateUtil;
import loureiro.enzo.usefi.util.FirebaseUtil;
import loureiro.enzo.usefi.util.SharedPreferencesUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class PerfilUsuarioExibirFragment extends Fragment {

    private TextView txtNome, txtIdade, txtGenero, txtLocalizacao;
    private ImageView imgFoto;
    private ProgressBar progressBarExibir;
    private FloatingActionButton fabEdit;

    private boolean usuarioDefinido = false;
    private Usuario usuario;
    private DatabaseReference referenceUser;
    private ValueEventListener valueEventListenerUsuario;

    private PerfilUsuarioFragment fragment;

    private static final String TAG = "PERFIL_EXIBIR_FRAGMENT";

    public PerfilUsuarioExibirFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_perfil_usuario_exibir, container, false);

        txtNome = view.findViewById(R.id.txtNomeUsuarioPerfil);
        txtIdade = view.findViewById(R.id.txtNascimenteUsuarioPerfil);
        txtGenero = view.findViewById(R.id.txtGeneroUsuarioPerfil);
        txtLocalizacao = view.findViewById(R.id.txtLocalizacaoUsuarioPerfil);

        imgFoto = view.findViewById(R.id.fotoComerciante);

        progressBarExibir = view.findViewById(R.id.progressBarUsuarioPerfil);

        fabEdit = view.findViewById(R.id.fabEditUsuarioPerfil);
        fabEdit.setOnClickListener(listenerFabEdit);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
      if(!usuarioDefinido) {
          recuperarUsuario();
          AnimacaoUtil.progressBar(progressBarExibir, imgFoto, true);
      }
      else {
          fabEdit.setVisibility(View.GONE);
          definirDadosUsuario();
      }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(valueEventListenerUsuario!=null)
            referenceUser.removeEventListener(valueEventListenerUsuario);
    }

    //Recupera o Usuario Logado
    private void recuperarUsuario() {
        if(Others.isOnline(getActivity())) {
            new Thread() {
                @Override
                public void run() {
                    FirebaseAuth firebaseAuth = ConfiguracaoFirebase.getFirebaseAuth();
                    final FirebaseUser user = firebaseAuth.getCurrentUser();
                    final String id = Base64Util.dadoToBase64(user.getEmail());
                    referenceUser = ConfiguracaoFirebase.getReferenceFirebase().child("Usuarios").child(id);
                    valueEventListenerUsuario = referenceUser.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Log.i(TAG, dataSnapshot.getValue().toString());
                            usuario = dataSnapshot.getValue(Usuario.class);
                            usuario.setId(id);

                            StorageReference referenceImagem = ConfiguracaoFirebase.getStorageReference().child(Usuario.USUARIOS).child(usuario.getId()).child("perfil.png");
                            referenceImagem.getDownloadUrl().addOnCompleteListener(getActivity(), new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if(task.isSuccessful()){
                                        usuario.setImgUrl(task.getResult().toString());
                                    }

                                    SharedPreferencesUtil.gravarValor(getActivity(), Usuario.NOME, usuario.getNome());
                                    SharedPreferencesUtil.gravarValor(getActivity(), Usuario.GENERO, usuario.getGenero());
                                    SharedPreferencesUtil.gravarValor(getActivity(), Usuario.IDADE, usuario.getIdade());

                                    definirDadosUsuario();
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }.start();
        }else{
            usuario = new Usuario();
            usuario.setNome(SharedPreferencesUtil.lerValor(getActivity(), Usuario.NOME));
            usuario.setGenero(SharedPreferencesUtil.lerValor(getActivity(), Usuario.GENERO));
            usuario.setIdade(SharedPreferencesUtil.lerValor(getActivity(), Usuario.IDADE));
            definirDadosUsuario();
        }
    }

    private void definirDadosUsuario() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtNome.setText(usuario.getNome());
                txtIdade.setText("");
                txtIdade.setText(DateUtil.dataFirebaseToDataNormal(usuario.getIdade()));

                String genero = Usuario.MASCULINO;

                if(usuario.getGenero().equals(Cliente.FEMINO))
                    genero = Usuario.FEMININO;
                else if(usuario.getGenero().equals(Cliente.OUTRO))
                    genero = Usuario.OUTRO;

                txtGenero.setText(genero);

                txtLocalizacao.setText(usuario.getLocalizacao());

                FirebaseUtil.recuperarImagemStorage(getActivity(), Uri.parse(usuario.getImgUrl()), progressBarExibir, imgFoto, imgFoto);
            }
        });
    }

    private View.OnClickListener listenerFabEdit = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            fragment.trocarFragment();
        }
    };

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        usuarioDefinido = true;
    }

    public void setFragment(PerfilUsuarioFragment fragment) {
        this.fragment = fragment;
    }
}
