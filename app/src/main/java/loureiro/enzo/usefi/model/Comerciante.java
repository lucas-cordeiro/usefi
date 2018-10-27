package loureiro.enzo.usefi.model;

import com.google.firebase.database.Exclude;

import java.io.Serializable;

public class Comerciante implements Serializable{
    private String id;
    private String login;
    private String senha;
    private String nomeLoja;
    private String CNPJ;
    private String plano;
    private String dataNotificacoes;
    private int notificacoes;
    private String urlLogo;
    private String urlFotoComercio;
    private String urlBanner;
    private String dataPagamento;
    private boolean pago;
    private String local;
    private String wifiNome;
    private String wifiSenha;
    private long conexoesComerciante;
    private String dataInscricao;
    private double latitude;
    private double longitude;
    private double distance;

    public final static String DATA_NULL = "NÃO DEFINIDO", CONEXOES = "Conexoes", CATEGORIAS = "Categorias", MAIS_VISITADOS = "Mais Visitados", VISITADOS_RECENTEMENTE = "Visitados Recentemente", COMERCIANTES = "Comerciantes", BANNER = "urlBanner";
    public static final String LOGIN = "Login", SENHA = "Senha";
    public static String TODOS, POPULAR, NOVOS;
    public final static String TODOS_ID = "1", POPULAR_ID="2", NOVOS_ID = "3";

    public Comerciante() {
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    @Exclude
    public String getSenha() {
        return senha;
    }

    @Exclude
    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getNomeLoja() {
        return nomeLoja;
    }

    public void setNomeLoja(String nomeLoja) {
        this.nomeLoja = nomeLoja;
    }

    public String getCNPJ() {
        return CNPJ;
    }

    public void setCNPJ(String CNPJ) {
        this.CNPJ = CNPJ;
    }

    public String getPlano() {
        return plano;
    }

    public void setPlano(String plano) {
        this.plano = plano;
    }

    public String getDataNotificacoes() {
        return dataNotificacoes;
    }

    public void setDataNotificacoes(String dataNotificacoes) {
        this.dataNotificacoes = dataNotificacoes;
    }

    public int getNotificacoes() {
        return notificacoes;
    }

    public void setNotificacoes(int notificacoes) {
        this.notificacoes = notificacoes;
    }

    public String getUrlBanner() {
        return urlBanner;
    }

    public void setUrlBanner(String urlBanner) {
        this.urlBanner = urlBanner;
    }

    public String getUrlLogo() {
        return urlLogo;
    }

    public void setUrlLogo(String urlLogo) {
        this.urlLogo = urlLogo;
    }

    public String getUrlFotoComercio() {
        return urlFotoComercio;
    }

    public void setUrlFotoComercio(String urlFotoComercio) {
        this.urlFotoComercio = urlFotoComercio;
    }

    public String getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(String dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    public boolean isPago() {
        return pago;
    }

    public void setPago(boolean pago) {
        this.pago = pago;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getWifiNome() {
        return wifiNome;
    }

    public void setWifiNome(String wifiNome) {
        this.wifiNome = wifiNome;
    }

    public String getWifiSenha() {
        return wifiSenha;
    }

    public void setWifiSenha(String wifiSenha) {
        this.wifiSenha = wifiSenha;
    }

    public long getConexoesComerciante() {
        return conexoesComerciante;
    }

    public void setConexoesComerciante(long conexoesComerciante) {
        this.conexoesComerciante = conexoesComerciante;
    }

    public String getDataInscricao() {
        return dataInscricao;
    }

    public void setDataInscricao(String dataInscricao) {
        this.dataInscricao = dataInscricao;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}
