package pkg.Controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import okhttp3.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import pkg.model.EncryptedFile;
import pkg.model.FilePathTreeItem;
import pkg.model.User;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;

public class Controller {

    private static String outDir = "\\\\Mac\\Home\\Desktop\\outDir\\";
    private static String incDir = "\\\\Mac\\Home\\Desktop\\incDir";
    private static String strDir = "\\\\Mac\\Home\\Desktop";
    @FXML
    private Tab localTab;
    @FXML
    private Tab remoteTab;
    @FXML
    private ScrollPane logPane;
    @FXML
    private Button downloadButton;
    @FXML
    private Button uploadButton;
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
    private MenuItem backupOpt;
    @FXML
    private Button addSlot;
    @FXML
    private Label programStatus;
    private TextArea logger;
    private char[] password = new char[]{'r', 'o', 's', 'h', 'a', 'n', '8', '9', '1', '2', '3', '4'};
    private TreeView<String> treeView;
    private TreeItem<String> rootNode;
    private ArrayList<FilePathTreeItem> fileUploadList;
    private ArrayList<FilePathTreeItem> fileDownloadList;
    private SecureRandom secureRandom = new SecureRandom();
    private String tempDir = "\\\\Mac\\Home\\Desktop\\encryptedFiles";
    private WebController webController;

    private boolean isDirCreated = false;
    private Preferences prefs;

    public void initLocalFileTree() {
        fileUploadList = new ArrayList<>();

        webController = new WebController();

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

        rootNode = new TreeItem<>(hostName);

        Iterable<Path> rootDirectories = FileSystems.getDefault().getRootDirectories();
        for (Path name : rootDirectories) {
            FilePathTreeItem treeItem = new FilePathTreeItem(name);
            rootNode.getChildren().add(treeItem);
        }
        rootNode.setExpanded(true);
        treeView = new TreeView<>(rootNode);

        treeBox.getChildren().addAll(treeView);
        VBox.setVgrow(treeView, Priority.ALWAYS);

        localTab.setContent(treeBox);

        // Every file that is clicked on will be added to a list for uploading
        treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);  // select multiple files
        treeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue,
                                Object newValue) {
                FilePathTreeItem selectedFile = (FilePathTreeItem) newValue;
                fileUploadList.add(selectedFile);
            }
        });
    }

    public void initRemoteFileTree() throws IOException {
        fileDownloadList = new ArrayList<>();
        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItemDownload = new MenuItem("download");
        MenuItem menuItemDelete = new MenuItem("delete");
        contextMenu.getItems().add(menuItemDownload);
        contextMenu.getItems().add(menuItemDelete);
        Popup popup = new Popup();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Remote File Deletion");
        // Create the vertical box for the tree
        VBox treeBox = new VBox();
        treeBox.setPadding(new Insets(0, 0, 0, 0));

        // Create the request before attempting to connect to the server
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create("{\n    \"user_id\": \"kevin@kevin.com\"\n}", JSON);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://3.15.177.232/api/directory")
                .method("POST", body)
                .build();

        // Attempt connect
        Response response = null;
        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String timeDate = localDateTime.format(formatter);
        try {
            response = client.newCall(request).execute();
            programStatus.setText("Connected, " + " connected established: " + localDateTime);
        } catch (IOException exception) {
            programStatus.setText("Not connected");
        }

        TreeItem rootNode = new TreeItem<>("Remote Server");

        //Nonsense to initialize the path
        Path path = Paths.get("C:");

        JSONArray jsonArray = new JSONArray(response.body().string());
        for (int i = 0; i < jsonArray.length(); i++) {
            String fileName = jsonArray.getJSONObject(i).get("name").toString();
            String fileId = jsonArray.getJSONObject(i).get("id").toString();
            FilePathTreeItem treeItem = new FilePathTreeItem(fileName, fileId, path);
            rootNode.getChildren().add(treeItem);
        }

        TreeView treeView = new TreeView(rootNode);
        rootNode.setExpanded(true);
        treeBox.getChildren().addAll(treeView);
        VBox.setVgrow(treeView, Priority.ALWAYS);

        remoteTab.setContent(treeBox);

        // Every file that is selected will be added to a list for downloading
        treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);  // select multiple files
        treeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue,
                                Object newValue) {
                FilePathTreeItem selectedItem = (FilePathTreeItem) newValue;
                fileDownloadList.add(selectedItem);
                // The root directory causes a cast exception error because it is of the type treeItem

            }
        });

        treeView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {

                if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                    FilePathTreeItem treeItem = (FilePathTreeItem) treeView.getSelectionModel().getSelectedItem();
                    treeView.setContextMenu(contextMenu);
                    contextMenu.show(treeView, Side.BOTTOM, 0, 0);

                    menuItemDelete.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            alert.setHeaderText("Delete " + treeItem.getFileName());
                            alert.setContentText("Are you sure?");

                            Optional<ButtonType> result = alert.showAndWait();
                            if (result.get() == ButtonType.OK) {
                                try {
                                    Response response = webController.deleteToServer(treeItem.getFileId());
                                    System.out.println(response);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {

                            }
                        }
                    });
                }

                if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                    contextMenu.hide();
                    FilePathTreeItem treeItem = (FilePathTreeItem) treeView.getSelectionModel().getSelectedItem();
                    System.out.println(treeItem.getFileName());
                }
            }
        });
    }

    public void initCtrl() {

    }

    public void initBtns() {
        loginBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                getLogin();
            }
        });

        createBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                setLogin();
            }
        });
    }

    public void initDir() {

        if (!isDirCreated) {
            new File(tempDir).mkdir();
            new File(incDir).mkdir();
            new File(outDir).mkdir();
        }
    }

    public void initLog() {
        logger = new TextArea();
        logger.setWrapText(true);
        logPane.setContent(logger);
        logPane.setFitToWidth(true);

        logger.setText("Program started...");
    }

    public void onBackupOpt(ActionEvent e) {

        BackUpController backUpController = new BackUpController();

        backUpController.start(prefs);

    }

    public void onDownloadButtonClicked(ActionEvent e) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {

        Task task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // Check the download list and download each file from the server
                if (fileDownloadList != null) {
                    for (FilePathTreeItem treeItem : fileDownloadList) {
                        Response response = null;

                        response = webController.downloadFromServer(treeItem.getFileId());

                        JSONObject jsonObject = null;

                        jsonObject = new JSONObject(response.body().string());

                        String fileString = jsonObject.get("data").toString();
                        String saltString = jsonObject.get("salt").toString();
                        String fileExt = jsonObject.get("extension").toString();
                        String fileName = jsonObject.get("name").toString();

                        // convert the salt and file data back to a byte arrays
                        byte[] salt = Base64.decodeBase64(saltString);
                        byte[] backToBytes = Base64.decodeBase64(fileString);

                        // write an encrypted file from the byte array and begin decryption

                        FileUtils.writeByteArrayToFile(new File(outDir + "encryptedFile"), backToBytes);
                        decryptFile(new File(outDir + "/encryptedFile"), salt, fileExt, fileName);
                    }
                }
                fileDownloadList = new ArrayList<>();
                return null;
            }
        };
        new Thread(task).start();
    }

    public void onUploadButtonClicked(ActionEvent e) {

        Task task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {

                ArrayList<EncryptedFile> encryptedFiles = new ArrayList<>();
                for (FilePathTreeItem treeItem : fileUploadList) {
                    if (treeItem != null) {
                        if (treeItem.isDirectory()) {
                            Path path = Paths.get(treeItem.getFullPath());
                            DirectoryStream<Path> stream = null;
                            stream = Files.newDirectoryStream(path);

                            for (Path filePath : stream) {
                                File fileUpload = new File(filePath.toString());
                                EncryptedFile encryptedFile = null;
                                encryptedFile = encryptFile(fileUpload, password);
                                encryptedFiles.add(encryptedFile);
                            }
                            Response response = null;
                            response = webController.uploadDirToServer(encryptedFiles);
                            System.out.println(response);
                        } else if (!treeItem.isDirectory()) {
                            File fileUpload = new File(treeItem.getFullPath());
                            EncryptedFile encryptedFile = null;
                            encryptedFile = encryptFile(fileUpload, password);

                            if (encryptedFile != null) {
                                // Encode the file
                                createFileString(encryptedFile);

                                //upload to server
                                Response response = null;
                                response = webController.uploadFileToServer(encryptedFile);
                                System.out.println(response);
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

        /*
        encFile = encryptFile(selectedFile, password);

        if(encFile == null) {
            logger.appendText('\n' + "No file was selected");
        } else {
            splitFile(encFile);
        }

        This is for decryption
        File tempFile = new File(incDir + "\\EncryptFile.001");

        // remove hardcoding later
        String fileName = "EncryptFile";

        List<File> files = getFileList(tempFile);
        decryptFile(mergeFiles(files, fileName));
    */

    public void onRefreshButtonClicked(ActionEvent event) throws IOException {
        initRemoteFileTree();
    }

    // Grab user data from login window
    public void getLogin() {

        String userName = userInput.getCharacters().toString();
        String userPass = passInput.getCharacters().toString();
        String userCode = codeInput.getCharacters().toString();

        User tempUser = new User(userName, userPass, userCode);
    }

    public void setLogin() {
    }

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

    public void splitFile(File file) throws NoSuchAlgorithmException, InvalidKeySpecException,
            NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

        String fileName = file.getName();
        int partCount = 1;
        int partSize = 1024 * 1024;
        byte[] buffer = new byte[partSize];
        byte[] cipherData;
        long totalFileSize = 0;

        try (
                FileInputStream fileInputStream = new FileInputStream(file);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {

            int bytesLeft = 0;

            while ((bytesLeft = bufferedInputStream.read(buffer)) > 0) { // read until there is no data left from the input stream
                String tempPartName = String.format("%s.%03d", fileName, partCount++);
                File newFilePart = new File(tempDir, tempPartName);
                try (FileOutputStream fileOutputStream = new FileOutputStream(newFilePart)) {
                    fileOutputStream.write(buffer, 0, bytesLeft); // CHECK the bytesLeft variable, should it return the same number as the encoded byte array?
                    System.out.println("When Splitting: " + newFilePart.length());
                    totalFileSize = +newFilePart.length();
                }
            }
            logger.appendText("\n" + partCount + " files were split! " + "The average file size was: " + totalFileSize / partCount + "bytes.");
        } catch (IOException e) {
            e.printStackTrace();
        }
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

            EncryptedFile encryptedFile = new EncryptedFile(tempDir, file.getName());
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

        File outputFile = new File(incDir, fileName + "." + fileExt);

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
    }

    public void fileWriter(byte[] inputDataStream) {

    }

    // Retrieve the list of files to be reassembled

    public List<File> getFileList(File partFileSource) {
        String partFileName = partFileSource.getName();
        String finalFileName = partFileName.substring(0, partFileName.lastIndexOf('.'));

        File[] files = partFileSource.getParentFile().listFiles(
                (File dir, String name) -> name.matches(finalFileName + "[.]\\d+"));
        Arrays.sort(files);
        return Arrays.asList(files);
    }

    public File mergeFiles(List<File> files, String fileName) throws InvalidKeySpecException, InvalidKeyException, NoSuchAlgorithmException,
            NoSuchPaddingException, IOException {
        File mergedFile = new File(strDir, "CheckMe");
        try (FileOutputStream fileOutputStream = new FileOutputStream(mergedFile); // fix for multiple files
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {
            for (File file : files) {
                Files.copy(file.toPath(), bufferedOutputStream);
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return mergedFile;
    }

    /*
    public void cleanUp() throws IOException {
        File splitFileDir = new File(tempDir);
        FileUtils.cleanDirectory(splitFileDir);
    }
    */

    public void serverSend() {

    }
}

