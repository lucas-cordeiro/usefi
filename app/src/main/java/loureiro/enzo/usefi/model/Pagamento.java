package loureiro.enzo.usefi.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Pagamento {
    private String id;
    private String status;

    public Pagamento() {
    }

    public Pagamento(String id, String status) {
        this.id = id;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void fromJSONObject(JSONObject object){
        try {
            this.id = (object.getJSONObject("response").getString("id"));
            this.status = object.getJSONObject("response").getString("state");
        }
        catch (JSONException e){
            e.printStackTrace();
        }
    }
}
