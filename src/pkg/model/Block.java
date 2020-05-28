package pkg.model;

import org.json.JSONArray;
import org.json.JSONObject;
import pkg.Controller.Helpers;
import pkg.Interface.Tx;

import java.util.ArrayList;
import java.util.List;

public class Block<T extends Tx> {
    public String previousHash;
    public int index;
    public String merkleRoot;
    public List<Transaction> transactions;
    public int nonce;
    public String hash;
    public long timestamp;

    public Block() {
        transactions = new ArrayList<Transaction>();
        nonce = 0;
        timestamp = 0;
    }

    public static Block inflate(JSONObject b) {
        Block block = new Block();
        block.previousHash = b.getString("previousHash");
        block.index = b.getInt("index");
        if (b.has("merkleRoot"))
            block.merkleRoot = b.getString("merkleRoot");

        JSONArray transactions = b.getJSONArray("transactions");
        for (int i = 0; i < transactions.length(); i++) {
            block.transactions.add(Transaction.inflate(transactions.getJSONObject(i)));
        }

        if (b.has("nonce"))
            block.nonce = b.getInt("nonce");
        block.hash = b.getString("hash");
        block.timestamp = b.getLong("timestamp");

        return block;
    }

    public void computeMerkleRoot() {
        List<String> treeList = merkleTree();
        // Last element is the merkle root hash if transactions
        this.merkleRoot = (treeList.get(treeList.size() - 1));
    }

    public List<String> merkleTree() {
        ArrayList<String> tree = new ArrayList<>();
        // add all transactions as leaves of the tree.
        for (Transaction t : transactions) {
            tree.add(t.hash());
        }
        int levelOffset = 0; // first level

        // Iterate through each level, stopping when we reach the root (levelSize
        // == 1).
        for (int levelSize = transactions.size(); levelSize > 1; levelSize = (levelSize + 1) / 2) {
            // For each pair of nodes on that level:
            for (int left = 0; left < levelSize; left += 2) {
                // The right hand node can be the same as the left hand, in the
                // case where we don't have enough
                // transactions.
                int right = Math.min(left + 1, levelSize - 1);
                String tleft = tree.get(levelOffset + left);
                String tright = tree.get(levelOffset + right);
                tree.add(Helpers.sha256(tleft + tright));
            }
            // Move to the next level.
            levelOffset += levelSize;
        }
        return tree;
    }

    public void computeHash() {
        JSONArray txs = new JSONArray();
        for (Transaction tx : this.transactions) {
            txs.put(tx.serialize());
        }
        String serializedData = txs.toString();
        this.hash = Helpers.sha256(timestamp + index + merkleRoot + serializedData + nonce + previousHash);
    }

    public JSONObject serialize() {
        JSONObject j = new JSONObject();
        j.put("previousHash", this.previousHash);
        j.put("index", this.index);
        j.put("merkleRoot", this.merkleRoot);
        JSONArray txs = new JSONArray();
        for (Transaction tx : this.transactions) {
            txs.put(tx.serialize());
        }
        j.put("transactions", txs);
        j.put("nonce", this.nonce);
        j.put("hash", this.hash);
        j.put("timestamp", this.timestamp);

        return j;
    }

}
