package loureiro.enzo.usefi.model;

public class Pagina {
    private String TAG;
    private int pagina;

    public Pagina(String TAG, int pagina) {
        this.TAG = TAG;
        this.pagina = pagina;
    }

    public String getTAG() {
        return TAG;
    }

    public void setTAG(String TAG) {
        this.TAG = TAG;
    }

    public int getPagina() {
        return pagina;
    }

    public void setPagina(int pagina) {
        this.pagina = pagina;
    }
}
