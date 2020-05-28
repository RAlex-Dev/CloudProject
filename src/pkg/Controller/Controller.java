package pkg.Controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import okhttp3.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import pkg.Constants;
import pkg.Service.MineService;
import pkg.Tools.EDTool;
import pkg.model.EncryptedFile;
import pkg.model.FilePathTreeItem;
import pkg.model.User;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class Controller {

    boolean firstTime;
    @FXML
    private Tab localTab;
    @FXML
    private Tab remoteTab;
    @FXML
    private ScrollPane logPane;
    @FXML
    private TextField userInput;
    @FXML
    private TextField passInput;
    @FXML
    private TextField codeInput;
    @FXML
    private Button loginBtn;
    @FXML
    private Button createBtn;
    @FXML
    private Button uploadBtn;
    @FXML
    private Button addSlot;
    @FXML
    private ImageView refresh;
    @FXML
    private Label programStatus;
    @FXML
    private Label userNameField;
    @FXML
    private Label status;
    @FXML
    private Label points;

    private TrayIcon trayIcon;

    private boolean isMultiOn = false;

    private TextArea logger;
    private char[] password = new char[]{'r', 'o', 's', 'h', 'a', 'n', '8', '9', '1', '2', '3', '4'};
    private TreeItem<String> rootNode;
    private ArrayList<FilePathTreeItem> fileUploadList;
    private ArrayList<FilePathTreeItem> fileDownloadList;
    private SecureRandom secureRandom = new SecureRandom();
    private WebController webController;
    private TreeView localTreeView;
    private TreeView remoteTreeView;
    private boolean isDirCreated = false;
    private Preferences prefs;

    private String userName;
    private String company;
    private User user;

    public Controller() {
        user = new User("kevin@kevin.com", "password", "22203");
        user.setCredits(50);
        user.setStatus("Gold");

    }

    public void setCredentials(String userName, String company) {
        this.userName = userName;
        this.company = company;
        setUserDetail();
    }

    public void setUserDetail() {
        userNameField.setText(userName);
        status.setText(user.getStatus());
        userNameField.setTextFill(Color.web("#0076a3"));
        status.setTextFill(Color.web("#C2B381"));

    }


    public void initCtrlCheck(Scene scene) throws IOException {

        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.isAltDown()) {
                    if (isMultiOn) {
                        isMultiOn = false;
                        logger.appendText('\n' + "MultiMode OFF");
                    } else {
                        isMultiOn = true;
                        logger.appendText('\n' + "MultiMode ON");
                    }
                }
            }
        });
    }

    // start the mining service, the interval can be specified

    public void startMineService(TrayIcon trayIcon) throws IOException {
        Timer timer = new Timer();
        TimerTask mineService = new MineService(trayIcon);
        timer.schedule(mineService, 15000000, 15000000);
    }

    // initialize the local file tree by building an object for each directory and file on the disk
    public void initLocalFileTree() {
        fileUploadList = new ArrayList<>();
        webController = new WebController();

        // Initialize the right click menu for the user
        ContextMenu contextMenu = new ContextMenu();
        MenuItem bronzeItem = new MenuItem("Bronze");
        MenuItem silverItem = new MenuItem("Silver");
        MenuItem goldItem = new MenuItem("Gold");
        Menu accessMenu = new Menu("Access Level", null,
                bronzeItem, silverItem, goldItem);
        contextMenu.getItems().add(accessMenu);

        // The VBox is a parent layout for all elements of the tree
        VBox treeBox = new VBox();
        treeBox.setPadding(new Insets(0, 0, 0, 0));
        treeBox.setSpacing(100);
        String hostName = "computer";
        try {
            hostName = InetAddress.getLocalHost().getHostName();
            logger.appendText('\n' + "File tree built for the host " + hostName);
            logger.appendText('\n' + "Ready");
        } catch (UnknownHostException e) {
            System.out.println(e.getMessage());
        }

        // The root node is the hostname and it contains every drive on the computer
        // Each drive has a treeItem representation, that includes external drives connected to the system
        rootNode = new TreeItem<>(hostName);
        Iterable<Path> rootDirectories = FileSystems.getDefault().getRootDirectories();
        for (Path name : rootDirectories) {
            FilePathTreeItem treeItem = new FilePathTreeItem(name);
            rootNode.getChildren().add(treeItem);
        }

        // the directories are added to the layout
        rootNode.setExpanded(true);
        localTreeView = new TreeView<>(rootNode);
        treeBox.getChildren().addAll(localTreeView);
        VBox.setVgrow(localTreeView, Priority.ALWAYS);

        localTab.setContent(treeBox);

        // Every file that is clicked on will be added to a list for uploading
        localTreeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);  // select multiple files
        localTreeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue,
                                Object newValue) {
                FilePathTreeItem selectedFile = (FilePathTreeItem) newValue;

                if (isMultiOn) {
                    fileUploadList.add(selectedFile);
                } else if (selectedFile.isDirectory()) {
                    Path path = Paths.get(selectedFile.getFullPath());
                    DirectoryStream<Path> stream = null;
                    try {
                        stream = Files.newDirectoryStream(path);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    for (Path filePath : stream) {
                        FilePathTreeItem filePathTreeItem = new FilePathTreeItem(filePath);
                        fileUploadList.add(filePathTreeItem);
                    }
                } else {
                    fileUploadList = new ArrayList<>();

                }
            }
        });

        // call back function for right buttons clicks for setting access level for each file
        localTreeView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {

                if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                    FilePathTreeItem treeItem = (FilePathTreeItem) localTreeView.getSelectionModel().getSelectedItem();
                    localTreeView.setContextMenu(contextMenu);
                    contextMenu.show(localTreeView, Side.BOTTOM, 0, 0);

                    bronzeItem.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            treeItem.setAccessLevel("BRONZE");
                            logger.setText("hello");
                        }
                    });

                    silverItem.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            treeItem.setAccessLevel("SILVER");
                        }
                    });

                    goldItem.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            treeItem.setAccessLevel("GOLD");
                        }
                    });
                }

                if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                    contextMenu.hide();
                }
            }
        });
    }

    // operates for the refresh button in the UI
    public void refreshRemoteTree() throws IOException {
        initRemoteFileTree();
    }


    // Initializes the file tree for the remote files on the server
    public void initRemoteFileTree() throws IOException {

        fileDownloadList = new ArrayList<>();

        // Initialize the context menu for user interactions
        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItemHistory = new MenuItem("Get History");
        MenuItem menuItemDelete = new MenuItem("delete");
        contextMenu.getItems().add(menuItemHistory);
        contextMenu.getItems().add(menuItemDelete);
        Popup popup = new Popup();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Remote File Deletion");

        // The VBox is a parent layout for every element in the tree
        VBox treeBox = new VBox();
        treeBox.setPadding(new Insets(0, 0, 0, 0));

        // Create the request before attempting to connect to the server
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create("{\n    \"user_id\": \"ACCloud@ACCloud.com\"\n}", JSON);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(Constants.directoryUrl)
                .method("POST", body)
                .build();

        // Attempt connect and display time and date of connection
        Response response = null;
        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String timeDate = localDateTime.format(formatter);
        try {
            response = client.newCall(request).execute();
            programStatus.setText("Connected, " + " connected established: " + timeDate);
        } catch (IOException exception) {
            programStatus.setText("Not connected");
        }

        // The root of the file tree, this is not an actual representation of anything like
        // on the local file tree, but it is needed to build the tree
        TreeItem rootNode = new TreeItem<>("KCCloud@KCCloud.com");

        //Nonsense to initialize the path
        Path path = Paths.get("C:");

        // parse Json response from server and build a tree item for every Json object
        JSONArray jsonArray = new JSONArray(response.body().string());
        for (int i = 0; i < jsonArray.length(); i++) {
            String fileName = jsonArray.getJSONObject(i).get("name").toString();
            String fileId = jsonArray.getJSONObject(i).get("id").toString();
            String accessLevel = jsonArray.getJSONObject(i).get("tier").toString();
            FilePathTreeItem treeItem = new FilePathTreeItem(fileName, fileId, path);
            treeItem.setAccessLevel(accessLevel);
            rootNode.getChildren().add(treeItem);
        }

        // create the file tree itself
        remoteTreeView = new TreeView(rootNode);
        rootNode.setExpanded(true);
        treeBox.getChildren().addAll(remoteTreeView);
        VBox.setVgrow(remoteTreeView, Priority.ALWAYS);

        remoteTab.setContent(treeBox);

        // Every file that is selected will be added to a list for downloading
        remoteTreeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);  // select multiple files
        remoteTreeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue,
                                Object newValue) {

                FilePathTreeItem selectedItem = (FilePathTreeItem) newValue;
                fileDownloadList.add(selectedItem);
                // The root directory causes a cast exception error because it is of the type treeItem
            }
        });

        // Call back function for right mouse clicks for remotely deleting files and getting the file history from the server
        remoteTreeView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {

                if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                    FilePathTreeItem treeItem = (FilePathTreeItem) remoteTreeView.getSelectionModel().getSelectedItem();
                    remoteTreeView.setContextMenu(contextMenu);
                    contextMenu.show(remoteTreeView, Side.BOTTOM, 0, 0);

                    menuItemDelete.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            alert.setHeaderText("Delete " + treeItem.getFileName());
                            alert.setContentText("Are you sure?");

                            Optional<ButtonType> result = alert.showAndWait();
                            if (result.get() == ButtonType.OK) {
                                try {
                                    Response response = webController.deleteToServer(treeItem.getFileId());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {

                            }
                        }
                    });

                    menuItemHistory.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            String fileEvent = "";
                            long fileTime;
                            String fileHash;

                            try {
                                FilePathTreeItem treeItem = (FilePathTreeItem) remoteTreeView.getSelectionModel().getSelectedItem();
                                Response response = webController.getHistory(treeItem.getFileId());
                                JSONArray jsonArray = new JSONArray(response.body().string());
                                logger.appendText("\n" + "File History for " + "'" + treeItem.getFileName() + "' " + ":");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = (JSONObject) jsonArray.getJSONObject(i);
                                    JSONObject value = (JSONObject) jsonObject.getJSONObject("value");

                                    fileEvent = value.getString("event");
                                    fileTime = value.getLong("time");
                                    java.util.Date time = new java.util.Date((long) fileTime * 1000);

                                    logger.appendText("\n\n" + "{event: " + fileEvent
                                            + "\n" + "time accessed: " + time + "}");
                                }
                                logger.appendText("\n " + "----------------------------------------------------------" +
                                        "----------------------------------------------------------" +
                                        " ----------------------------------------------------------" +
                                        " ----------------------------------------------------------");


                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

                // allow the right click menu to go away when the user clicks out of it
                if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                    contextMenu.hide();
                    FilePathTreeItem treeItem = (FilePathTreeItem) remoteTreeView.getSelectionModel().getSelectedItem();
                    System.out.println(treeItem.getFileName());
                }
            }
        });
    }

    // create directories for different program operations
    public void initDir() {
        if (!isDirCreated) {
            new File(Constants.tempDir).mkdir();
            new File(Constants.incDir).mkdir();
            new File(Constants.outDir).mkdir();
        }
    }

    // create the log
    public void initLog() {
        logger = new TextArea();
        logger.setWrapText(true);
        logPane.setContent(logger);
        logPane.setFitToWidth(true);

        logger.setText("Program started...");
    }

    // Callback function for starting the backup function
    public void onBackupOpt(ActionEvent e) throws BackingStoreException, IOException, ClassNotFoundException {

        BackUpController backUpController = new BackUpController();
        startBackupController(backUpController);

    }

    // creates layout for backup view
    public void startBackupController(BackUpController backUpController) throws IOException, BackingStoreException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/pkg/view/backupView.fxml"));

        Parent root = fxmlLoader.load();
        backUpController = fxmlLoader.getController();
        backUpController.start();
        Stage stage = new Stage();
        stage.setTitle("Auto BackUp");
        stage.setScene(new Scene(root, 600, 300));
        stage.setAlwaysOnTop(true);
        stage.show();
    }

    // Callback for downloading files from the server
    public void onDownloadButtonClicked(ActionEvent e) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException,
            BadPaddingException, InvalidKeyException, InvalidKeySpecException {
        EDTool decrypter = new EDTool();

        Task task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // Check the download list and download each file from the server
                boolean canDownload = false;

                if (fileDownloadList != null) {
                    for (FilePathTreeItem treeItem : fileDownloadList) {
                        // Determine if the user can download the files selected
                        if (treeItem.getAccessLevel().equals("BRONZE") && user.getCredits() > 0) {
                            canDownload = true;
                        } else if (treeItem.getAccessLevel().equals("SILVER") && user.getCredits() > 10) {
                            canDownload = true;
                        } else if (treeItem.getAccessLevel().equals("GOLD") && user.getCredits() > 20) {
                            canDownload = true;
                        }

                        if (canDownload) {
                            Response response = null;
                            response = webController.downloadFromServer(treeItem.getFileId());
                            JSONObject jsonObject = null;
                            jsonObject = new JSONObject(response.body().string());
                            String fileString = jsonObject.get("data").toString();
                            String saltString = jsonObject.get("salt").toString();
                            String fileExt = jsonObject.get("extension").toString();
                            String fileName = jsonObject.get("name").toString();
                            String tier = jsonObject.get("tier").toString();

                            // convert the salt and file data back to a byte arrays
                            byte[] salt = Base64.decodeBase64(saltString);
                            byte[] backToBytes = Base64.decodeBase64(fileString);

                            // write an encrypted file from the byte array and begin decryption

                            FileUtils.writeByteArrayToFile(new File(Constants.outDir + "encryptedFile"), backToBytes);
                            decrypter.decryptFile(new File(Constants.outDir + "/encryptedFile"), salt, fileExt, fileName, password);
                            logger.appendText("\n" + treeItem.getFileName() + " was downloaded!");

                        } else {
                            JOptionPane.showMessageDialog(null, "You do not have access to this file!");
                        }
                    }
                }
                System.out.println("I am near the new array list");
                fileDownloadList = new ArrayList<>();
                return null;
            }
        };
        new Thread(task).start();
    }

    // Callback function for uploading files to the server
    public void onUploadButtonClicked(ActionEvent e) throws IOException {
        EDTool encrypter = new EDTool();
        Task task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                FilePathTreeItem treeItem;
                File fileUpload;
                EncryptedFile encryptedFile;
                Response response;
                treeItem = (FilePathTreeItem) localTreeView.getSelectionModel().getSelectedItem();

                if (fileUploadList.isEmpty()) {
                    System.out.println(treeItem.getFullPath());

                    fileUpload = new File(treeItem.getFullPath());
                    encryptedFile = null;
                    encryptedFile = encrypter.encryptFile(fileUpload, password);
                    logger.appendText("\n" + encryptedFile.getName() + " was encrypted!");
                    encryptedFile.setAccessLevel(treeItem.getAccessLevel());
                    createFileString(encryptedFile);
                    System.out.println("The encrypted file's access level is " + encryptedFile.getAccessLevel());
                    //upload to server
                    response = null;
                    response = webController.uploadFileToServer(encryptedFile);
                    logger.appendText("\n" + encryptedFile.getName() + " was uploaded!");
                    System.out.println(response);
                } else {
                    ArrayList<EncryptedFile> encryptedFiles = new ArrayList<>();
                    for (FilePathTreeItem singleTreeItem : fileUploadList) {
                        if (singleTreeItem != null) {
                            if (singleTreeItem.isDirectory()) {
                                Path path = Paths.get(singleTreeItem.getFullPath());
                                DirectoryStream<Path> stream = null;
                                stream = Files.newDirectoryStream(path);

                                for (Path filePath : stream) {
                                    fileUpload = new File(filePath.toString());
                                    encryptedFile = null;
                                    encryptedFile = encryptFile(fileUpload, password);
                                    encryptedFiles.add(encryptedFile);
                                }
                                response = null;
                                response = webController.uploadDirToServer(encryptedFiles);
                            } else if (!singleTreeItem.isDirectory()) {
                                fileUpload = new File(singleTreeItem.getFullPath());
                                encryptedFile = null;
                                encryptedFile = encrypter.encryptFile(fileUpload, password);
                                if (encryptedFile != null) {
                                    // Encode the file
                                    createFileString(encryptedFile);
                                    encryptedFile.setAccessLevel(singleTreeItem.getAccessLevel());
                                    //upload to server
                                    response = null;
                                    response = webController.uploadFileToServer(encryptedFile);
                                }
                            }
                        }
                    }
                }
                fileUploadList = new ArrayList<>();
                return null;
            }
        };
        new Thread(task).start();
    }

    public void refreshFileTree() throws IOException {
        initRemoteFileTree();
    }

    // Grab user data from login window
    public void getLogin() {

        String userName = userInput.getCharacters().toString();
        String userPass = passInput.getCharacters().toString();
        String userCode = codeInput.getCharacters().toString();

        User tempUser = new User(userName, userPass, userCode);
    }

    // encode the file to a string representation using base64 algorithm
    public void createFileString(EncryptedFile encryptedFile) throws IOException {

        FileInputStream fileInputStreamReader = new FileInputStream(encryptedFile);
        StringBuilder sb = new StringBuilder();
        int bufferSize = 3 * 1024; //3 mb is the size of a chunk
        byte[] bytes = new byte[bufferSize];
        int readSize = 0;

        while ((readSize = fileInputStreamReader.read(bytes)) == bufferSize) {
            sb.append(Base64.encodeBase64String(bytes));
        }

        if (readSize > 0) {
            bytes = Arrays.copyOf(bytes, readSize);
            sb.append(Base64.encodeBase64String(bytes));
        }

        String fileString = sb.toString();
        encryptedFile.setFileString(fileString);

    }

    // Encrypt a file using a password and randomly generated salt
    public EncryptedFile encryptFile(File file, char[] password) throws NoSuchAlgorithmException, InvalidKeySpecException,
            NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

        byte[] salt = new byte[8];
        secureRandom.nextBytes(salt);

        if (file == null) {
            return null;
        } else {
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec keySpec = new PBEKeySpec(password, salt, 65535, 256);
            SecretKey tempKey = secretKeyFactory.generateSecret(keySpec);
            SecretKey secretKey = new SecretKeySpec(tempKey.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            int partSize = 1024 * 1024;
            byte[] buffer = new byte[partSize];

            EncryptedFile encryptedFile = new EncryptedFile(Constants.tempDir, file.getName());
            encryptedFile.setSaltString(salt); // create salt string for transmission
            encryptedFile.setFileName(file.getName());
            encryptedFile.setFilePath(file.getPath());

            try (
                    FileOutputStream fileOutputStream = new FileOutputStream(encryptedFile);
                    FileInputStream fileInputStream = new FileInputStream(file);
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {

                int bytesLeft = 0;

                while ((bytesLeft = bufferedInputStream.read(buffer)) > -1) { // read until there is no data left from the input stream
                    byte[] cipherData = cipher.update(buffer, 0, bytesLeft);
                    fileOutputStream.write(cipherData);
                }

                byte[] outputArr = cipher.doFinal();
                if (outputArr != null) {
                    fileOutputStream.write(outputArr);
                }

                fileInputStream.close();
                fileOutputStream.flush();
                fileOutputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            logger.appendText("\n" + file.getName() + " was encrypted!");

            return encryptedFile;
        }
    }

    // Decrypt file using params from server
    public void decryptFile(File encryptedFile, byte[] salt, String fileExt, String fileName) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException,
            IOException, BadPaddingException, IllegalBlockSizeException {
        byte[] buffer = new byte[1024 * 1024];
        byte[] decipherData;

        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

        KeySpec keySpec = new PBEKeySpec(password, salt, 65535, 256);
        SecretKey tempKey = secretKeyFactory.generateSecret(keySpec);
        SecretKey secretKey = new SecretKeySpec(tempKey.getEncoded(), "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        // insert (try... catch) later
        FileInputStream fileInputStream = new FileInputStream(encryptedFile);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

        File outputFile = new File(Constants.incDir, fileName + "." + fileExt);

        FileOutputStream fileOutputStream = new FileOutputStream(outputFile); // fix for multiple files
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);

        int bytesRead;

        while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
            byte[] output = cipher.update(buffer, 0, bytesRead);

            if (output != null) {
                bufferedOutputStream.write(output);
            }
        }
        byte[] output = cipher.doFinal();
        if (output != null) {
            bufferedOutputStream.write(output);
        }
        fileInputStream.close();
        fileOutputStream.flush();
        fileOutputStream.close();
        logger.appendText("\n" + " was decrypted!");

    }

    /*
    public void cleanUp() throws IOException {
        File splitFileDir = new File(tempDir);
        FileUtils.cleanDirectory(splitFileDir);
    }
    */

    // create layout for the split and merge UI
    public void startSplitMerge() throws IOException {

        SplitMergeController splitMergeController = new SplitMergeController();

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/pkg/view/splitMerge.fxml"));

        Parent root = fxmlLoader.load();
        splitMergeController = fxmlLoader.getController();
        splitMergeController.initLog();

        Stage stage = new Stage();
        stage.setTitle("File Splitter");
        stage.setScene(new Scene(root, 600, 400));
        stage.setAlwaysOnTop(true);
        stage.show();
    }

    // Generate a tray icon for Window OS
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
            // construct a TrayIcon
            trayIcon = new TrayIcon(image, "Cloud", popup);
            // set the TrayIcon properties
            trayIcon.addActionListener(showListener);
            // add the tray image
            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                System.err.println(e);
            }
        }
        return trayIcon;
    }

    // Display
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
}

