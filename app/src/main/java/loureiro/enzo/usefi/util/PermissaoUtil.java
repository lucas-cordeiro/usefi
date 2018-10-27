package loureiro.enzo.usefi.util;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissaoUtil {

    public static boolean validarPermissoes(String permissoes[], Activity activity, int requestCode){

        if(Build.VERSION.SDK_INT >= 23){

            List<String> listaPermissoes = new ArrayList<>();

            //Percorre a lista de permissões
            for(String permissao : permissoes){
                if(ContextCompat.checkSelfPermission(activity, permissao) != PackageManager.PERMISSION_GRANTED)
                    listaPermissoes.add(permissao);
            }

            //Se não houver permissões não é continuado a solicitação
            if(listaPermissoes.isEmpty()) return true;

            String[] novasPermissoes = new String[listaPermissoes.size()];
            listaPermissoes.toArray( novasPermissoes );

            //Solicitar a permissão
            ActivityCompat.requestPermissions(activity, novasPermissoes, requestCode);
            return false;
        }
        else
            return true;
    }

    public static boolean validarPermissoes(String permissoes[], Fragment fragment, int requestCode){

        if(Build.VERSION.SDK_INT >= 23){

            List<String> listaPermissoes = new ArrayList<>();

            //Percorre a lista de permissões
            for(String permissao : permissoes){
                if(ContextCompat.checkSelfPermission(fragment.getContext(), permissao) != PackageManager.PERMISSION_GRANTED)
                    listaPermissoes.add(permissao);
            }

            //Se não houver permissões não é continuado a solicitação
            if(listaPermissoes.isEmpty()) return true;

            String[] novasPermissoes = new String[listaPermissoes.size()];
            listaPermissoes.toArray( novasPermissoes );

            //Solicitar a permissão
            fragment.requestPermissions(novasPermissoes, requestCode);
            return false;
        }
        else
            return true;
    }

}
