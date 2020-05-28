package pkg.Controller;

import org.json.*;
import pkg.model.Block;
import pkg.model.Transaction;

import java.util.ArrayList;
import java.time.Instant;

public class BlockClient {
    ArrayList<Block> blocks;

    public BlockClient(String bc) {
        //Assuming the entire chain is correct, we technically only need the last block.
        blocks = new ArrayList<Block>();

        String jsonString = bc; //assign your JSON String here
        JSONArray arr = new JSONArray(bc);

        //Build array of blocks from JSON
        for (int i = 0; i < arr.length(); i++) {
            blocks.add(Block.inflate(arr.getJSONObject(i)));
        }

        //getLastBlock -> only previousHash and index are relevant

        //mineBlock(Transaction JSON)
        //Create new Block
        // need calculateHash(), merkle()
        // mine nonce
        // don't need to save - get fresh copy from server every run

    }

    public Block tail() {
        return this.blocks.get(this.blocks.size() - 1);
    }

    public JSONObject mineBlock(String tx) {
        Block parent = this.tail();

        Block block = new Block();
        Transaction t = Transaction.inflate(new JSONObject(tx));
        block.transactions.add(t);
        block.timestamp = Instant.now().getEpochSecond();
        block.previousHash = parent.hash;
        block.index = parent.index + 1;
        block.computeMerkleRoot();
        block.computeHash();

        //Mine nonce
        int nonce = 0;
        boolean nonceFound = false;
        String nonceKey = "000";

        while (!nonceFound) {
            block.nonce = nonce;
            block.computeHash();
            nonceFound = block.hash.substring(0, nonceKey.length()).equals(nonceKey);
            nonce++;
        }

        return block.serialize();
    }
}
