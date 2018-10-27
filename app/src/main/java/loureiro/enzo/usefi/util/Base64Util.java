package loureiro.enzo.usefi.util;

import android.util.Base64;

public class Base64Util {
    public static String dadoToBase64(String dado){
        return Base64.encodeToString(dado.getBytes(), Base64.DEFAULT).replaceAll("(\\n|\\r)","");
    }

    public static String base64ToDado(String base64){
        return new String(Base64.decode(base64, Base64.DEFAULT));
    }
}
