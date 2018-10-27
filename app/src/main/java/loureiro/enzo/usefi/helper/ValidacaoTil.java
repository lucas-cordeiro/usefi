package loureiro.enzo.usefi.helper;

import android.support.design.widget.TextInputLayout;
import android.util.Log;

import loureiro.enzo.usefi.R;

public class ValidacaoTil {

    private static final String TAG = "VALIDACAO_TIL";

    public static boolean verificarTahPreenchido(TextInputLayout textInputLayout){
        boolean tahPreenchido = false;
        Log.i(TAG, "Dado: "+textInputLayout.getEditText().getText().toString());

        if(textInputLayout.getEditText().getText().toString().length() > 0) {
            tahPreenchido = true;
        }
        else {
            textInputLayout.setErrorEnabled(true);
            textInputLayout.setErrorTextAppearance(R.style.ErrorTextColorVermelho);
            textInputLayout.setError("Preencha este campo corretamente!");
        }

        return tahPreenchido;
    }

    public static boolean verificarEhEmail(TextInputLayout textInputLayout) {
        boolean ehEmail = verificarTahPreenchido(textInputLayout);

        if (ehEmail)
            if (!ValidacaoDados.verificaEmail(textInputLayout.getEditText().getText().toString())) {
                ehEmail = false;
                textInputLayout.setErrorEnabled(true);
                textInputLayout.setErrorTextAppearance(R.style.ErrorTextColorVermelho);
                textInputLayout.setError("Login Inválido!");
            }

        return ehEmail;
    }

    public static boolean verificarEhSenha(TextInputLayout textInputLayout) {
        boolean ehSenha = verificarTahPreenchido(textInputLayout);
        if (ehSenha)
            if (!ValidacaoDados.verificaSenha(textInputLayout.getEditText().getText().toString())) {
                ehSenha = false;
                textInputLayout.setErrorEnabled(true);
                textInputLayout.setErrorTextAppearance(R.style.ErrorTextColorVermelho);
                textInputLayout.setError("Senha Inválida!");
            }

        return ehSenha;
    }

    public static boolean verificarEhEmailCadastro(TextInputLayout textInputLayout) {
        boolean ehEmail = verificarTahPreenchido(textInputLayout);

        if (ehEmail)
            if (!ValidacaoDados.verificaEmail(textInputLayout.getEditText().getText().toString())) {
                ehEmail = false;
                textInputLayout.setErrorEnabled(true);
                textInputLayout.setErrorTextAppearance(R.style.ErrorTextColorVermelho);
                textInputLayout.setError("Email Inválido! exemplo@dominio.com");
            }

        return ehEmail;
    }

    public static boolean verificarEhSenhaCadastro(TextInputLayout textInputLayout) {
        boolean ehSenha = verificarTahPreenchido(textInputLayout);

        if (ehSenha) {

            Log.i(TAG, "ehSenha: " + ehSenha);
            Log.i(TAG, "verificaSenha: "+ValidacaoDados.verificaSenha(textInputLayout.getEditText().getText().toString()));

            if (!ValidacaoDados.verificaSenha(textInputLayout.getEditText().getText().toString())) {

                ehSenha = false;
                textInputLayout.setErrorEnabled(true);
                textInputLayout.setErrorTextAppearance(R.style.ErrorTextColorVermelho);
                textInputLayout.setError("Senha Fraca!Use letras Maiúsculas, minúsculas e números!");
            }
        }
        return ehSenha;
    }

    public static boolean verificarConfirmacaoSenha(TextInputLayout textInputLayout1, TextInputLayout textInputLayout2) {
        boolean ehSenha1 = verificarTahPreenchido(textInputLayout2), ehSenha2 = verificarTahPreenchido(textInputLayout1), ehIgual = false;

        verificarEhSenhaCadastro(textInputLayout1);

        if (ehSenha1 && ehSenha2) {

            if (textInputLayout1.getEditText().getText().toString().equals(textInputLayout2.getEditText().getText().toString())) {
                ehIgual = true;
            } else {
                textInputLayout2.setErrorEnabled(true);
                textInputLayout2.setErrorTextAppearance(R.style.ErrorTextColorVermelho);
                textInputLayout2.setError("As Senhas devem ser Iguais!");
            }
        }

        return ehIgual;
    }

    public static boolean verificarCNPJ(TextInputLayout textInputLayout) {
        boolean ehCNPJ = verificarTahPreenchido(textInputLayout);

        if (ehCNPJ) {

            if(!ValidacaoDados.isCNPJ(ValidacaoDados.leCNPJ(textInputLayout.getEditText().getText().toString()))) {
                textInputLayout.setErrorEnabled(true);
                textInputLayout.setErrorTextAppearance(R.style.ErrorTextColorVermelho);
                textInputLayout.setError("CNPJ Inválido! 99.999.999/0009-99");
                ehCNPJ = false;
            }
        }

        return ehCNPJ;
    }


    public  static boolean verificarData(TextInputLayout textInputLayout){
        boolean ehData = verificarTahPreenchido(textInputLayout);

        if(ehData){
            if(!ValidacaoDados.isData(textInputLayout.getEditText().getText().toString())){
                textInputLayout.setErrorEnabled(true);
                textInputLayout.setErrorTextAppearance(R.style.ErrorTextColorVermelho);
                textInputLayout.setError("Data Inválida! dd/MM/AAAA");
                ehData = false;
            }
        }

        return ehData;
    }
}
