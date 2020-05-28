package pkg.Controller;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class Driver {
    BlockClient bc;

    public Driver() throws IOException {
        String s = readLineByLineJava8("chain.txt");
        String tx = readLineByLineJava8("newtx.txt");

        //Please call the blockchain API before each mine.
        this.bc = new BlockClient(s);
        JSONObject newBlock = this.bc.mineBlock(tx);

        System.out.println(newBlock.toString());


    }

    private static String readLineByLineJava8(String filePath) {
        StringBuilder contentBuilder = new StringBuilder();

        try (Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return contentBuilder.toString();
    }
}
