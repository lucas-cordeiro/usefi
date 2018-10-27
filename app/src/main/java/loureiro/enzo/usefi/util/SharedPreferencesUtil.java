package loureiro.enzo.usefi.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesUtil {
    private static final String ARQUIVO_PREFERENCIA = "ArquivoPreferencia";
    private static SharedPreferences preferences;

    public static void excluirValor(Context context, String key) {
        if (preferences == null)
            preferences = context.getSharedPreferences(ARQUIVO_PREFERENCIA, 0);

        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(key);
        editor.apply();
    }

    public static void gravarValor(Context context, String key, String value) {
        if (preferences == null)
            preferences = context.getSharedPreferences(ARQUIVO_PREFERENCIA, 0);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }


    public static void gravarValor(Context context, String key, int value) {
        if (preferences == null)
            preferences = context.getSharedPreferences(ARQUIVO_PREFERENCIA, 0);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static void gravarValor(Context context, String key, boolean value) {
        if (preferences == null)
            preferences = context.getSharedPreferences(ARQUIVO_PREFERENCIA, 0);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static String lerValor(Context context, String key){
        if (preferences == null)
            preferences = context.getSharedPreferences(ARQUIVO_PREFERENCIA, 0);

        return preferences.getString(key, "");
    }

    public static int lerValorInt(Context context, String key){
        if (preferences == null)
            preferences = context.getSharedPreferences(ARQUIVO_PREFERENCIA, 0);

        return preferences.getInt(key, -1);
    }

    public static boolean lerValorBoolean(Context context, String key) {
        if (preferences == null)
            preferences = context.getSharedPreferences(ARQUIVO_PREFERENCIA, 0);

        return preferences.getBoolean(key, false);
    }

    public static boolean contemValor(Context context, String key){
        if (preferences == null)
            preferences = context.getSharedPreferences(ARQUIVO_PREFERENCIA, 0);

        return preferences.contains(key);
    }


}
