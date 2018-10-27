package loureiro.enzo.usefi.model;

public class ItemPlanos {

    private String item;
    private int color;
    private boolean visivel;

    public ItemPlanos() {
    }

    public ItemPlanos(String item, int color, boolean visivel) {
        this.item = item;
        this.color = color;
        this.visivel = visivel;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public boolean isVisivel() {
        return visivel;
    }

    public void setVisivel(boolean visivel) {
        this.visivel = visivel;
    }
}
