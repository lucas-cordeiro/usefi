package loureiro.enzo.usefi.model;

public class Cliente {
    private String id;
    private String nome;
    private String idade;
    private String genero;
    private String login;
    private String imgUrl;
    private String tipoLogin;
    private String plataforma;
    private long tempoDeConexao;
    private String ultimaConexao;
    private String ultimaVisita;
    private int visitas;

    public static final String MASCULINO = "m", FEMINO="f", OUTRO = "o";
    public static final String ANDROID = "Android", IOS = "iOS",OUTROS="Outros";
    public static final String ULTIMA_CONEXAO = "ultimaConexao";

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

    public long getTempoDeConexao() {
        return tempoDeConexao;
    }

    public void setTempoDeConexao(long tempoDeConexao) {
        this.tempoDeConexao = tempoDeConexao;
    }

    public String getUltimaVisita() {
        return ultimaVisita;
    }

    public void setUltimaVisita(String ultimaVisita) {
        this.ultimaVisita = ultimaVisita;
    }

    public String getUltimaConexao() {
        return ultimaConexao;
    }

    public void setUltimaConexao(String ultimaConexao) {
        this.ultimaConexao = ultimaConexao;
    }

    public int getVisitas() {
        return visitas;
    }

    public void setVisitas(int visitas) {
        this.visitas = visitas;
    }
}
