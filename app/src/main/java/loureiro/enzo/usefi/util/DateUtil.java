package loureiro.enzo.usefi.util;

import android.content.Intent;
import android.util.Log;

import org.joda.time.LocalDate;
import org.joda.time.Years;

import java.util.Calendar;
import java.util.Date;

import loureiro.enzo.usefi.helper.Others;

public class DateUtil {

    private static final String TAG = "DATE_UTIL";
    public static final long ONE_MINUTE_IN_MILLIS=60000, ONE_HOUR_IN_MILLIS = 60*ONE_MINUTE_IN_MILLIS;

    public static String dataDateToDataNormal(Date date){
        //Data normal dd/MM/YYYY
        //Index       0123456789
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date.getTime());
        String dataNormal="";

        int dia = calendar.get(Calendar.DAY_OF_MONTH);

        if(dia<10)
            dataNormal += "0"+dia+"/";
        else
            dataNormal += dia+"/";

        int mes = calendar.get(Calendar.MONTH)+1;

        if(mes<10)
            dataNormal += "0"+mes+"/";
        else
            dataNormal += mes+"/";

        int ano = calendar.get(Calendar.YEAR);

        if(ano<10)
            dataNormal += "000"+ano;
        else if(ano<100)
            dataNormal += "00"+ano;
        else if(ano<1000)
            dataNormal += "0"+ano;
        else
            dataNormal += ano;

        return dataNormal;
    }

    public static String dataDateToDataCompleta(Date date){
        //Data normal dd/MM/YYYY
        //Index       0123456789
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date.getTime());
        String dataNormal="";

        int ano = calendar.get(Calendar.YEAR);

        if(ano<10)
            dataNormal += "000"+ano;
        else if(ano<100)
            dataNormal += "00"+ano;
        else if(ano<1000)
            dataNormal += "0"+ano;
        else
            dataNormal += ano;

        int mes = calendar.get(Calendar.MONTH)+1;

        if(mes<10)
            dataNormal += "-0"+mes;
        else
            dataNormal += "-"+mes;


        int dia = calendar.get(Calendar.DAY_OF_MONTH);

        if(dia<10)
            dataNormal += "-0"+dia;
        else
            dataNormal += "-"+dia;

        int hora = calendar.get(Calendar.HOUR_OF_DAY);

        if(hora<10)
            dataNormal+=":0"+hora;
        else
            dataNormal+=":"+hora;

        int minuto = calendar.get(Calendar.MINUTE);
        if(minuto<10)
            dataNormal+="-0"+minuto;
        else
            dataNormal+="-"+minuto;

        return dataNormal;
    }

    public static Date dataCompletaToDate(String dataCompleta){
        int year = Integer.valueOf(dataCompleta.substring(0,4));
        int month = Integer.valueOf(dataCompleta.substring(5,7))-1;
        int day = Integer.valueOf(dataCompleta.substring(8,10));
        int hour = Integer.valueOf(dataCompleta.substring(11,13));
        int minute = Integer.valueOf(dataCompleta.substring(14,16));

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute, 0);

        return calendar.getTime();
    }

    public static String dataDateToDataCompletaSegundos(Date date){
        String dataCompleta = "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date.getTime());

        int ano = calendar.get(Calendar.YEAR);

        if(ano<10)
            dataCompleta += "000"+ano;
        else if(ano<100)
            dataCompleta += "00"+ano;
        else if(ano<1000)
            dataCompleta += "0"+ano;
        else
            dataCompleta += ano;

        int mes = calendar.get(Calendar.MONTH)+1;

        if(mes<10)
            dataCompleta += "-0"+mes;
        else
            dataCompleta += "-"+mes;


        int dia = calendar.get(Calendar.DAY_OF_MONTH);

        if(dia<10)
            dataCompleta += "-0"+dia;
        else
            dataCompleta += "-"+dia;

        int hora = calendar.get(Calendar.HOUR_OF_DAY);

        if(hora<10)
            dataCompleta+=":0"+hora;
        else
            dataCompleta+=":"+hora;

        int minuto = calendar.get(Calendar.MINUTE);
        if(minuto<10)
            dataCompleta+="-0"+minuto;
        else
            dataCompleta+="-"+minuto;

        int segundo = calendar.get(Calendar.SECOND);
        if(segundo<10)
            dataCompleta+="-0"+segundo;
        else
            dataCompleta+="-"+segundo;

        return  dataCompleta;
    }

    public static String dataCompletaToDataNormal(String dataCompleta){
        String dataNormal = "";
        //2018-08-15:20-10
        //0123456789012345
        String dia = dataCompleta.substring(8,10);
        String mes = dataCompleta.substring(5,7);
        String ano = dataCompleta.substring(0,4);

        dataNormal = dia + '/' + mes + '/' + ano;

        return dataNormal;
    }

    public static  Date dataCompletaSegundosToDate(String dataCompleta){
        //2018-08-15:20-10-05
        //0123456789012345678

        int year = Integer.valueOf(dataCompleta.substring(0,4));
        int month = Integer.valueOf(dataCompleta.substring(5,7))-1;
        int day = Integer.valueOf(dataCompleta.substring(8,10));
        int hour = Integer.valueOf(dataCompleta.substring(11,13));
        int minute = Integer.valueOf(dataCompleta.substring(14,16));
        int second = Integer.valueOf(dataCompleta.substring(17));

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute, second);

        return calendar.getTime();
    }

    public static Date dataNormalToDate(String dataNormal){
        //Data normal dd/MM/YYYY
        //Index       0123456789
        Calendar calendar = Calendar.getInstance();
        calendar.set(Integer.parseInt(dataNormal.substring(6)),Integer.parseInt(dataNormal.substring(3,5))-1,Integer.parseInt(dataNormal.substring(0,2)));
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date dataFirebase = new Date();
        dataFirebase.setTime(calendar.getTimeInMillis());

        return dataFirebase;
    }

    public static String dataNormalToDataFirebase(String dataNormal){
        return dataNormal.replaceAll("/","-");
    }

    public static String dataFirebaseToDataNormal(String dataFirebase){
        return dataFirebase.replaceAll("-","/");
    }

    //Retorna false se a data colocada for menor que a atual
    public static boolean comparaData (Date date){
        boolean dataPassou = false;

        Date dateAtual = Others.getNTPDate();

        //Log.i(TAG, "DataAtual: "+dateAtual+"\nDataComerciane: "+dateComerciantePagamento);
        Log.i(TAG, "Data: " + dateAtual.compareTo(date));

        int comparacao = dateAtual.compareTo(date);

        dataPassou =  comparacao < 0;//Data Atual menor que a Data de Pagamento = true | Plano em dia

        return dataPassou;
    }

    public static boolean comparaData (Date date, Date dateAtual){
        //Log.i(TAG, "DataAtual: "+dateAtual+"\nDataComerciane: "+dateComerciantePagamento);
        Log.i(TAG, "Data: " + dateAtual.compareTo(date));

        int comparacao = dateAtual.compareTo(date);

        return comparacao < 0;//Data Atual menor que a Data de Pagamento = true | Plano em dia
    }

    public static int getIdade(Date dateAtual, Date dateCliente){
        LocalDate aniversarioCliente = new LocalDate(dateCliente);
        LocalDate atual = new LocalDate(dateAtual);
        final Years age = Years.yearsBetween(aniversarioCliente, atual);
        return age.getYears();
    }

    /*
    public static String intToMes(int mes){
        if(mes == 1)
            return "Janeiro";
        else if(mes == 2)
            return "Fevereiro";
        else if()
    }*/
}
