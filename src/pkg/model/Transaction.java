package pkg.model;

import org.json.JSONObject;
import pkg.Controller.Helpers;
import pkg.Interface.Tx;

public class Transaction implements Tx {

    private String hash;
    private JSONObject value;

    public Transaction(JSONObject value) {
        this.setValue(value);
    }

    public static Transaction inflate(JSONObject t) {
        Transaction tx = new Transaction(t.getJSONObject("value"));
        //System.out.println(tx.serialize().toString());

        return tx;
    }

    public String hash() {
        return hash;
    }

    public JSONObject getValue() {
        return value;
    }

    public void setValue(JSONObject value) {
        this.hash = Helpers.sha256(value.toString());
        this.value = value;
    }

    public JSONObject serialize() {
        JSONObject j = new JSONObject();
        j.put("value", this.value);
        j.put("hash", this.hash);

        return j;
    }

}