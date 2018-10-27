package loureiro.enzo.usefi.model;

public class Conexao {
    private String mes;
    private int quant;
    private String idComerciante;
    private String data;

    public String getMes() {
        return mes;
    }

    public String getMesGrafico(){
        return mes.substring(0,2)+"/"+mes.substring(2);
    }

    public void setMes(String mes) {
        this.mes = mes;
    }

    public int getQuant() {
        return quant;
    }

    public void setQuant(int quant) {
        this.quant = quant;
    }

    public String getIdComerciante() {
        return idComerciante;
    }

    public void setIdComerciante(String idComerciante) {
        this.idComerciante = idComerciante;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
