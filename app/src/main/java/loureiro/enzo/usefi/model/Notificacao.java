package loureiro.enzo.usefi.model;

public class Notificacao {
    private String id;
    private String title;
    private String mensagem;
    private String urlImagen;
    private String comercianteId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getUrlImagen() {
        return urlImagen;
    }

    public void setUrlImagen(String urlImagen) {
        this.urlImagen = urlImagen;
    }

    public String getComercianteId() {
        return comercianteId;
    }

    public void setComercianteId(String comercianteId) {
        this.comercianteId = comercianteId;
    }
}
