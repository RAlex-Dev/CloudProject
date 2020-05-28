package pkg.Service;

import okhttp3.Response;
import org.json.JSONObject;
import pkg.Controller.BlockClient;
import pkg.Controller.WebController;

import java.awt.*;
import java.io.IOException;
import java.util.TimerTask;

public class MineService extends TimerTask {
    TrayIcon trayIcon;

    public MineService(TrayIcon trayIcon) {
        this.trayIcon = trayIcon;
    }

    @Override
    public void run() {
        WebController webController = new WebController();
        try {
            Response jsonBc = webController.getBlockchain();
            Response jsonWork = webController.getWork();
            String stringBlockChain = jsonBc.body().string();
            String work = jsonWork.body().string();
            BlockClient blockClient = new BlockClient(stringBlockChain);
            JSONObject jsonBlock = blockClient.mineBlock(work);
            Response submittedWorkResponse = webController.submitWork(jsonBlock);
            Response response = webController.getInfo();
            JSONObject jsonResponse = new JSONObject(response.body().string());

            int points = jsonResponse.getInt("total");
            String userName = jsonResponse.getString("user_id");

            trayIcon.displayMessage("Kevin00", userName + " " + "points: " + points, TrayIcon.MessageType.INFO);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


