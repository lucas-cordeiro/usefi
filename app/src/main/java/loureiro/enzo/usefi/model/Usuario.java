package loureiro.enzo.usefi.model;

import com.google.firebase.database.Exclude;

import java.io.Serializable;

public class Usuario implements Serializable{
    private String id;
    private String nome;
    private String login;
    private String tipoLogin;
    private String senha;
    private String idade;
    private String genero;
    private String imgUrl;
    private String plataforma;
    private double latitude;
    private double longitude;
    private String localizacao;

    public static final String MASCULINO = "Masculino", FEMININO="Feminino", OUTRO = "Outro", VISITAS = "Visitas", VISITAS_RECENTES = "VisitasRecentes", PROXIMOS = "Próximos a você";
    public static final String NOME_NOTIFICACAO = ":Nome:", IDADE_NOTIFICACAO = ":Idade:";
    public static final String T_EMAIL = "Email", T_FACEBOOK = "Facebook", T_GMAIL = "Gmail";
    public static final String USUARIOS = "Usuarios",MANTER_LOGADO = "Manter Logado", ID = "ID_USUARIO",LOGIN = "Login", SENHA = "Senha", NOME="Nome", GENERO="Genero", IDADE = "Idade", NOTIFICACOES = "Notificacoes", TOPICOS="Topicos", REDES_BAIXADAS="RedesBaixadas";
    
    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getTipoLogin() {
        return tipoLogin;
    }

    public void setTipoLogin(String tipoLogin) {
        this.tipoLogin = tipoLogin;
    }

    @Exclude
    public String getSenha() {
        return senha;
    }

    @Exclude
    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getIdade() {
        return idade;
    }

    public void setIdade(String idade) {
        this.idade = idade;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getPlataforma() {
        return plataforma;
    }

    public void setPlataforma(String plataforma) {
        this.plataforma = plataforma;
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

    public String getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
    }
}
