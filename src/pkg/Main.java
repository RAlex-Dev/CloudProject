package pkg;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;
import pkg.Controller.Controller;


public class Main extends Application {

    private TreeView<String> treeView;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Controller controller = new Controller();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("view/view.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        controller.initLog();
        controller.initLocalFileTree();
        controller.initDir();
        controller.initRemoteFileTree();
        //    controller.initBtns();
        primaryStage.setTitle("CloudProto");
        primaryStage.setScene(new Scene(root, 1200, 800));
        primaryStage.setResizable(false);
        primaryStage.show();
    }
}
