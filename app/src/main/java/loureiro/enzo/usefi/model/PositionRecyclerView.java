package loureiro.enzo.usefi.model;

public class PositionRecyclerView {
    private String TAG;
    private int position;

    public PositionRecyclerView(String TAG, int position) {
        this.TAG = TAG;
        this.position = position;
    }

    public String getTAG() {
        return TAG;
    }

    public void setTAG(String TAG) {
        this.TAG = TAG;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
