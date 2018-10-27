package loureiro.enzo.usefi.helper;

import android.util.Log;

import java.util.InputMismatchException;

public class ValidacaoDados {

    private static final String TAG = "VALIDACAO_DADOS";

    private static boolean isChar(char text) {
        boolean texto = true;

        char a = 'a';
        boolean verifica_a = true;
        while (verifica_a) {
            if (a > 'z') {
                switch (text) {
                    case 'ã':
                        break;
                    case 'ç':
                        break;
                    case 'é':
                        break;
                    case 'ú':
                        break;
                    case 'í':
                        break;
                    default:
                        texto = false;
                        verifica_a = false;
                        break;
                }
            }
            if (a == text) {
                verifica_a = false;
            } else {
                a++;
            }
        }

        return texto;
    }

    private static boolean isChar2(char text) {
        boolean texto = true;

        char a = 'a';
        boolean verifica_a = true;
        while (verifica_a) {
            if (a > 'z') {
                switch (text) {
                    case '0':
                        break;
                    case '1':
                        break;
                    case '2':
                        break;
                    case '3':
                        break;
                    case '4':
                        break;
                    case '5':
                        break;
                    case '6':
                        break;
                    case '7':
                        break;
                    case '8':
                        break;
                    case '9':
                        break;
                    case '@':
                        break;
                    case '.':
                        break;

                    default:
                        texto = false;
                        verifica_a = false;
                        break;
                }
            }
            if (a == text) {
                verifica_a = false;
            } else {
                a++;
            }
        }

        return texto;
    }

    private static boolean isCharMinusculo(char character) {
        boolean caracter = true;

        char letra = character;
        char a = 'a';
        boolean verifica_A = true;

        while (verifica_A) {
            if (a > 'z') {
                caracter = false;
                verifica_A = false;
            }
            if (a == letra) {
                verifica_A = false;
            } else {
                a++;
            }
        }

        return caracter;
    }

    private static boolean isCharMaiusculo(char character) {
        boolean caracter = true;

        char A = 'A';
        boolean verifica_A = true;

        while (verifica_A) {
            if (A > 'Z') {
                caracter = false;
                verifica_A = false;
            }
            if (A == character) {
                verifica_A = false;
            } else {
                A++;
            }
        }

        return caracter;
    }

    public static boolean isString(String text) {
        boolean texto = true;

        if (!text.equals("")) {

            boolean vdd = true;
            while (vdd) {
                int tamanho = text.length();
                boolean vdd2 = true;
                int n = 0;
                while (vdd2) {
                    if (n >= tamanho) {
                        vdd2 = false;
                        vdd = false;
                    } else {
                        char letra = text.charAt(n);
                        char a = 'a';
                        boolean verifica_a = true;
                        while (verifica_a) {
                            if (a > 'z') {
                                switch (letra) {
                                    case 'ã':
                                        break;
                                    case 'ç':
                                        break;
                                    case 'é':
                                        break;
                                    case 'ú':
                                        break;
                                    case 'í':
                                        break;
                                    case ' ':
                                        break;
                                    case '\'':
                                        break;
                                    default:
                                        texto = false;
                                        verifica_a = false;
                                        vdd2 = false;
                                        vdd = false;

                                        break;
                                }
                            }
                            if (a == letra) {
                                verifica_a = false;
                            } else {
                                a++;
                            }
                        }

                        n++;
                    }
                }
            }
            return texto;
        } else
            return false;

    }

    public static boolean isData(String text) {
        boolean data = true;

        if (text == null) {
            data = false;
        } else {
            int tamanho = text.length();

            if (tamanho != 10) {
                data = false;
            } else {
                String data_teste = "" + text.substring(0, 2) + "" + text.substring(3, 5) + "" + text.substring(6, 10);
                try {
                    long num = Long.parseLong(data_teste);

                    int dia = Integer.parseInt(text.substring(0, 2));

                    String mes = text.substring(3, 5);

                    int ano = Integer.parseInt(text.substring(6, 10));

                    boolean ano_zuado = false;

                    if (ano % 6 == 0) {
                        ano_zuado = true;
                    }

                    int dia_maior = 0;

                    switch (mes) {
                        case "01":
                            dia_maior = 31;
                            break;

                        case "02":
                            if (ano_zuado)
                                dia_maior = 29;
                            else
                                dia_maior = 28;
                            break;

                        case "03":
                            dia_maior = 31;
                            break;

                        case "04":
                            dia_maior = 30;
                            break;

                        case "05":
                            dia_maior = 31;
                            break;

                        case "06":
                            dia_maior = 30;
                            break;

                        case "07":
                            dia_maior = 31;
                            break;

                        case "08":
                            dia_maior = 31;
                            break;

                        case "09":
                            dia_maior = 30;
                            break;

                        case "10":
                            dia_maior = 31;
                            break;

                        case "11":
                            dia_maior = 30;
                            break;

                        case "12":
                            dia_maior = 31;
                            break;

                        default:
                            data = false;
                            break;
                    }

                    if (dia > dia_maior || dia < 1) {
                        data = false;
                    }
                    if (ano < 1) {
                        data = false;
                    }
                } catch (NumberFormatException erro) {
                    data = false;
                }
            }
        }

        return data;
    }

    public static boolean leNome(String nome) {
        boolean txt;

        if (nome != null) {
            nome = nome.toLowerCase();
            txt = nome.length() >= 3 && isString(nome);
        } else
            txt = false;


        return txt;
    }

    public static boolean verificaEmail(String email) {
        boolean mail = true;

        int tamanho = email.length();

        if (tamanho < 7) {
            mail = false;
        } else {
            for (int n = 0; n < tamanho; n++) {
                mail = isChar2(email.charAt(n));

                if (!mail)
                    break;
            }

            boolean vdd = false;

            for (int n = 0; n < tamanho; n++) {
                if (email.charAt(n) == '@') {
                    if (n + 1 < tamanho) {
                        if (isChar(email.charAt(n + 1))) {
                            if (!vdd) {
                                vdd = true;
                            } else {
                                vdd = false;
                                break;
                            }
                        }
                    }

                }
            }

            mail = vdd;
        }

        if (mail) {
            mail = false;

            for (int n = 0; n < tamanho; n++) {
                if (email.charAt(n) == '.') {
                    if (n + 1 < tamanho && n + 2 < tamanho) {
                        if (isChar(email.charAt(n + 1)) && isChar(email.charAt(n + 2))) {
                            mail = true;
                        }
                    }
                }
            }
        }

        return mail;

    }

    public static boolean verificaSenha(String texto) {
        boolean password = true;

        int tamanho = texto.length();

        if (tamanho < 8) {
            password = false;
        } else {
            boolean minuscula = false;

            boolean maiuscula = false;

            for (int n = 0; n < tamanho; n++) {
                if (isCharMaiusculo(texto.charAt(n))) {
                    maiuscula = true;
                }
                if (isCharMinusculo(texto.charAt(n))) {
                    minuscula = true;
                }

                if (maiuscula && minuscula) {
                    break;
                }
            }

            if (maiuscula && minuscula) {
                boolean numero = false;

                for (int n = 0; n < tamanho; n++) {
                    if (!numero) {
                        try {
                            byte num = (byte) texto.charAt(n);
                            numero = true;
                            break;
                        } catch (NumberFormatException erro) {

                        }
                    }
                }

                password = numero;

            } else {

                password = false;
            }
        }

        return password;
    }

    public static boolean isCNPJ(String CNPJ) {
        if (CNPJ.equals("00000000000000") || CNPJ.equals("11111111111111") ||
                CNPJ.equals("22222222222222") || CNPJ.equals("33333333333333") ||
                CNPJ.equals("44444444444444") || CNPJ.equals("55555555555555") ||
                CNPJ.equals("66666666666666") || CNPJ.equals("77777777777777") ||
                CNPJ.equals("88888888888888") || CNPJ.equals("99999999999999") ||
                (CNPJ.length() != 14))
            return(false);

        char dig13, dig14;
        int sm, i, r, num, peso;

        try {
            sm = 0;
            peso = 2;
            for (i=11; i>=0; i--) {
                num = (int)(CNPJ.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso + 1;
                if (peso == 10)
                    peso = 2;
            }

            r = sm % 11;
            if ((r == 0) || (r == 1))
                dig13 = '0';
            else dig13 = (char)((11-r) + 48);

            sm = 0;
            peso = 2;
            for (i=12; i>=0; i--) {
                num = (int)(CNPJ.charAt(i)- 48);
                sm = sm + (num * peso);
                peso = peso + 1;
                if (peso == 10)
                    peso = 2;
            }

            r = sm % 11;
            if ((r == 0) || (r == 1))
                dig14 = '0';
            else dig14 = (char)((11-r) + 48);

            if ((dig13 == CNPJ.charAt(12)) && (dig14 == CNPJ.charAt(13)))
                return(true);
            else return(false);
        } catch (InputMismatchException erro) {
            return(false);
        }
    }

    public static String leCNPJ(String CNPJ){
        // máscara do CNPJ: 99.999.999/9999-99
        //Index             012345678901234567

        String CNPJFormatado = "";
        Log.i(TAG,"CNPJ: "+CNPJ);

        try{
            CNPJFormatado = CNPJ.substring(0,2)+CNPJ.substring(3,6)+CNPJ.substring(7,10)+CNPJ.substring(11,15)+CNPJ.substring(16);
        }
        catch (Exception ignored){}

        Log.i(TAG,"CNPJFormatado: "+CNPJFormatado);
        return CNPJFormatado;
    }
}
