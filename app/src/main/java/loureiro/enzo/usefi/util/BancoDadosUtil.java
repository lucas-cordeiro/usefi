package loureiro.enzo.usefi.util;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import loureiro.enzo.usefi.model.Wifi;

public class BancoDadosUtil {
    private static final String TABLE_WIFI="WIFI";
    private static final String CAMPO_NOME="nome", CAMPO_SENHA="senha", CAMPO_IDCOMERCIANTE="idComerciante";

    public static boolean gravarWifi(final Activity activity, Wifi wifi){
        try{
            //Banco de Dados
            SQLiteDatabase database = activity.openOrCreateDatabase("app", activity.MODE_PRIVATE, null);

            //Tabela
            database.execSQL("CREATE TABLE IF NOT EXISTS "+TABLE_WIFI+" ("+CAMPO_NOME+" VARCHAR, "+CAMPO_SENHA+" VARCHAR, "+CAMPO_IDCOMERCIANTE+" VARCHAR)");

            //Verificar se já há o registro[Início]
            boolean adicionar = true;
            //Recuperar itens
            Cursor cursor = database.rawQuery("SELECT "+CAMPO_NOME+", "+CAMPO_SENHA+", "+CAMPO_IDCOMERCIANTE+" FROM "+TABLE_WIFI, null);
            //Indices da Tabela
            int indiceIdComerciante = cursor.getColumnIndex(CAMPO_IDCOMERCIANTE);

            if(cursor.getCount()>0) {
                cursor.moveToFirst();
                while (true) {
                    if (cursor.getString(indiceIdComerciante).equals(wifi.getIdComerciante())) {
                        adicionar = false;
                        break;
                    }
                    if (cursor.isLast())
                        break;
                    cursor.moveToNext();
                }
            }


            if(!adicionar) {
                return atualizarWifi(activity, wifi);
            }
            //Verificar se já há o registro[Fim]

            //Inserir dados
            database.execSQL("INSERT INTO "+TABLE_WIFI+"("+CAMPO_NOME+","+CAMPO_SENHA+","+CAMPO_IDCOMERCIANTE+") VALUES('"+wifi.getNome()+"', '"+wifi.getSenha()+"', '"+wifi.getIdComerciante()+"')");

            return true;
        }
        catch (final Exception e){
            e.printStackTrace();
           activity.runOnUiThread(new Runnable() {
               @Override
               public void run() {
                   ToastUtil.toastCustom(activity,"Erro Leitura Banco: "+e.getClass().getName()+"\nMensagem: "+e.getMessage(), false);
               }
           });
            return false;
        }
    }

    public static boolean gravarWifi(final Context context, Wifi wifi){
        try{
            //Banco de Dados
            SQLiteDatabase database = context.openOrCreateDatabase("app", context.MODE_PRIVATE, null);

            //Tabela
            database.execSQL("CREATE TABLE IF NOT EXISTS "+TABLE_WIFI+" ("+CAMPO_NOME+" VARCHAR, "+CAMPO_SENHA+" VARCHAR, "+CAMPO_IDCOMERCIANTE+" VARCHAR)");

            //Verificar se já há o registro[Início]
            boolean adicionar = true;
            //Recuperar itens
            Cursor cursor = database.rawQuery("SELECT "+CAMPO_NOME+", "+CAMPO_SENHA+", "+CAMPO_IDCOMERCIANTE+" FROM "+TABLE_WIFI, null);
            //Indices da Tabela
            int indiceIdComerciante = cursor.getColumnIndex(CAMPO_IDCOMERCIANTE);

            if(cursor.getCount()>0) {
                cursor.moveToFirst();
                while (true) {
                    if (cursor.getString(indiceIdComerciante).equals(wifi.getIdComerciante())) {
                        adicionar = false;
                        break;
                    }
                    if (cursor.isLast())
                        break;
                    cursor.moveToNext();
                }
            }

            //Verificar se já há o registro[Fim]
            if(!adicionar)
                return atualizarWifi(context, wifi);

            //Inserir dados
            database.execSQL("INSERT INTO "+TABLE_WIFI+"("+CAMPO_NOME+","+CAMPO_SENHA+","+CAMPO_IDCOMERCIANTE+") VALUES('"+wifi.getNome()+"', '"+wifi.getSenha()+"', '"+wifi.getIdComerciante()+"')");

            return true;
        }
        catch (final Exception e){
            e.printStackTrace();
            return false;
        }
    }


    private static boolean atualizarWifi(final Context context, Wifi wifi) {
       try{
           //Banco de Dados
           SQLiteDatabase database = context.openOrCreateDatabase("app", context.MODE_PRIVATE, null);

           //Tabela
           database.execSQL("CREATE TABLE IF NOT EXISTS "+TABLE_WIFI+" ("+CAMPO_NOME+" VARCHAR, "+CAMPO_SENHA+" VARCHAR, "+CAMPO_IDCOMERCIANTE+" VARCHAR)");

           //Inserir dados
           database.execSQL("UPDATE "+TABLE_WIFI+" SET "+CAMPO_NOME+" = '"+wifi.getNome()+"', "+CAMPO_SENHA+" = '"+wifi.getSenha()+"' WHERE "+CAMPO_IDCOMERCIANTE+" = '"+wifi.getIdComerciante()+"'");

           return true;
       }
       catch (final Exception e){
           return false;
       }
    }

    private static boolean atualizarWifi(final Activity activity, Wifi wifi) {
        try{
            //Banco de Dados
            SQLiteDatabase database = activity.openOrCreateDatabase("app", activity.MODE_PRIVATE, null);

            //Tabela
            database.execSQL("CREATE TABLE IF NOT EXISTS "+TABLE_WIFI+" ("+CAMPO_NOME+" VARCHAR, "+CAMPO_SENHA+" VARCHAR, "+CAMPO_IDCOMERCIANTE+" VARCHAR)");

            //Inserir dados
            database.execSQL("UPDATE "+TABLE_WIFI+" SET "+CAMPO_NOME+" = '"+wifi.getNome()+"', "+CAMPO_SENHA+" = '"+wifi.getSenha()+"' WHERE "+CAMPO_IDCOMERCIANTE+" = '"+wifi.getIdComerciante()+"'");

            return true;
        }
        catch (final Exception e){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.toastCustom(activity,"Erro Leitura Banco: "+e.getClass().getName()+"\nMensagem: "+e.getMessage(), false);
                }
            });
            return false;
        }
    }

    public static  List<Wifi> lerWifis(final Activity activity, boolean aviso){
        List<Wifi> wifis = new ArrayList<>();
        try{
            //Banco de Dados
            SQLiteDatabase database = activity.openOrCreateDatabase("app", activity.MODE_PRIVATE, null);

            //Tabela
            database.execSQL("CREATE TABLE IF NOT EXISTS "+TABLE_WIFI+" ("+CAMPO_NOME+" VARCHAR, "+CAMPO_SENHA+" VARCHAR, "+CAMPO_IDCOMERCIANTE+" VARCHAR)");

            //Recuperar itens
            Cursor cursor = database.rawQuery("SELECT "+CAMPO_NOME+", "+CAMPO_SENHA+", "+CAMPO_IDCOMERCIANTE+" FROM "+TABLE_WIFI, null);

            if(cursor.getCount()>0) {
                //Indices da Tabela
                int indiceNome = cursor.getColumnIndex(CAMPO_NOME),
                        indiceSenha = cursor.getColumnIndex(CAMPO_SENHA),
                        indiceIdComerciante = cursor.getColumnIndex(CAMPO_IDCOMERCIANTE);

                cursor.moveToFirst();
                while (true) {
                    wifis.add(new Wifi(cursor.getString(indiceNome),"", cursor.getString(indiceSenha), cursor.getString(indiceIdComerciante)));
                    if (cursor.isLast())
                        break;
                    cursor.moveToNext();
                }
            }else if(aviso)
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.toastCustom(activity,"Não há redes cadastradas!", false);
                    }
                });

        }catch (final Exception e){
            e.printStackTrace();
          activity.runOnUiThread(new Runnable() {
              @Override
              public void run() {
                  ToastUtil.toastCustom(activity,"Erro Leitura Banco: "+e.getClass().getName()+"\nMensagem: "+e.getMessage(), false);
              }
          });
        }

        return wifis;
    }

    public static  List<Wifi> lerWifis(final Context context){
        List<Wifi> wifis = new ArrayList<>();
        try{
            //Banco de Dados
            SQLiteDatabase database = context.openOrCreateDatabase("app", context.MODE_PRIVATE, null);

            //Tabela
            database.execSQL("CREATE TABLE IF NOT EXISTS "+TABLE_WIFI+" ("+CAMPO_NOME+" VARCHAR, "+CAMPO_SENHA+" VARCHAR, "+CAMPO_IDCOMERCIANTE+" VARCHAR)");

            //Recuperar itens
            Cursor cursor = database.rawQuery("SELECT "+CAMPO_NOME+", "+CAMPO_SENHA+", "+CAMPO_IDCOMERCIANTE+" FROM "+TABLE_WIFI, null);

            if(cursor.getCount()>0) {
                //Indices da Tabela
                int indiceNome = cursor.getColumnIndex(CAMPO_NOME),
                        indiceSenha = cursor.getColumnIndex(CAMPO_SENHA),
                        indiceIdComerciante = cursor.getColumnIndex(CAMPO_IDCOMERCIANTE);

                cursor.moveToFirst();
                while (true) {
                    wifis.add(new Wifi(cursor.getString(indiceNome),"", cursor.getString(indiceSenha), cursor.getString(indiceIdComerciante)));
                    if (cursor.isLast())
                        break;
                    cursor.moveToNext();
                }
            }

        }catch (final Exception e){
            e.printStackTrace();
        }

        return wifis;
    }
}
