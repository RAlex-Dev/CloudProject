package pkg.Controller;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;

public class loginController {

    Controller controller;
    @FXML
    private Button loginBtn;
    @FXML
    private TextField userInput;
    @FXML
    private TextField company;
    private boolean firstTime;
    private TrayIcon trayIcon;

    public void loginUser() throws IOException {
        controller = new Controller();
        startMainWindow(controller);
        exit();
    }

    public void startMainWindow(Controller controller) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/pkg/view/view.fxml"));
        JMetro jMetro = new JMetro(Style.LIGHT);
        Parent root = fxmlLoader.load();
        controller = fxmlLoader.getController();
        controller.initLog();
        controller.initLocalFileTree();
        controller.initDir();
        controller.initRemoteFileTree();
        firstTime = true;
        Platform.setImplicitExit(false);
        Scene scene = new Scene(root, 1200, 800);
        jMetro.setScene(scene);
        Stage stage = new Stage();
        stage.setTitle("KCCloud");
        stage.setScene(scene);
        stage.setResizable(false);
        controller.startMineService(createTrayIcon(stage));
        stage.setAlwaysOnTop(false);
        stage.show();
        controller.initCtrlCheck(scene);
        controller.setCredentials(userInput.getText(), company.getText());

    }

    public TrayIcon createTrayIcon(final Stage stage) {
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();

            java.awt.Image image = null;

            try {
                URL url = new URL("http://www.digitalphotoartistry.com/rose1.jpg");
                image = ImageIO.read(url);
            } catch (IOException e) {
                e.printStackTrace();
            }

            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent windowEvent) {
                    stage.hide();
                }
            });

            final ActionListener closeListener = new ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
                    System.exit(0);
                }
            };

            ActionListener showListener = new ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            stage.show();
                        }
                    });
                }
            };

            // create a popup menu
            PopupMenu popup = new PopupMenu();

            java.awt.MenuItem showItem = new java.awt.MenuItem("Show");
            showItem.addActionListener(showListener);
            popup.add(showItem);

            java.awt.MenuItem closeItem = new java.awt.MenuItem("Close");
            closeItem.addActionListener(closeListener);
            popup.add(closeItem);
            /// ... add other items
            // construct a TrayIcon
            trayIcon = new TrayIcon(image, "Cloud", popup);
            // set the TrayIcon properties
            trayIcon.addActionListener(showListener);
            // ...
            // add the tray image
            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                System.err.println(e);
            }
        }
        return trayIcon;
    }

    public void showProgramIsMinimizedMsg() {
        if (firstTime) {
            trayIcon.displayMessage("Some message.",
                    "Some other message.",
                    TrayIcon.MessageType.INFO);
            firstTime = false;
        }
    }

    private void hide(final Stage stage) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (SystemTray.isSupported()) {
                    stage.hide();
                    showProgramIsMinimizedMsg();
                } else {
                    System.exit(0);
                }
            }
        });
    }

    public void exit() {
        Stage stage = (Stage) loginBtn.getScene().getWindow();
        stage.close();
    }
}
