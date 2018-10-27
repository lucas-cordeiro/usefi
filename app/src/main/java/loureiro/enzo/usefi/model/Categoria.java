package loureiro.enzo.usefi.model;

import java.io.Serializable;

public class Categoria implements Serializable{
    private String id;
    private String nome;

    public static final String CATEGORIA = "CATEGORIA";

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
}
