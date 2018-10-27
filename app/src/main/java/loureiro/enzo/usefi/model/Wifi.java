package loureiro.enzo.usefi.model;

import java.io.Serializable;

public class Wifi implements Serializable {
    private String id;
    private String nome;
    private String bssid;
    private String senha;
    private String idComerciante;

    public static final String ID = "ID", WIFI_NOME = "WIFI_NOME", WIFI_BSSID = "WIFI_BSSID", WIFI_ID_COMERCIANTE = "WIFI_ID_COMERCIANTE", WIFI_SENHA = "WIFI_SENHA";

    public Wifi(String nome, String bssid, String senha, String idComerciante) {
        this.nome = nome;
        this.bssid = bssid;
        this.senha = senha;
        this.idComerciante = idComerciante;
    }

    public Wifi() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getIdComerciante() {
        return idComerciante;
    }

    public void setIdComerciante(String idComerciante) {
        this.idComerciante = idComerciante;
    }
}
