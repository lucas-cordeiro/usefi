package loureiro.enzo.usefi.util;

import loureiro.enzo.usefi.rnum.EnumPlanos;

public class PlanoUtil {

    public static final String BASIC = "Basic", STANDARD = "Standard", PREMIUM = "Premium";
    public static final float BASIC_VALOR = 29.9f, STANDARD_VALOR = 59.9f, PREMIUM_VALOR = 99.9f;
    public static final int PUSH_STANDARD = 2, PUSH_PREMIUM = 4;

    public static EnumPlanos stringToEnumPlano(String plano){
        EnumPlanos planoEnum = EnumPlanos.Basic;

        if(plano.equals(STANDARD))
            planoEnum = EnumPlanos.Standard;
        else if(plano.equals(PREMIUM))
            planoEnum = EnumPlanos.Premium;

        return  planoEnum;
    }

    public static String enumPlanoToString(EnumPlanos planoEnum){
        String plano = BASIC;

        if(planoEnum == EnumPlanos.Standard)
            plano = STANDARD;
        else if(planoEnum == EnumPlanos.Premium)
            plano = PREMIUM;

        return plano;
    }

    public static float valorPlanoEnum(EnumPlanos plano){
        float valor = BASIC_VALOR;

        if(plano == EnumPlanos.Standard)
            valor = STANDARD_VALOR;
        else if(plano == EnumPlanos.Premium)
            valor = PREMIUM_VALOR;

        return valor;
    }
}
