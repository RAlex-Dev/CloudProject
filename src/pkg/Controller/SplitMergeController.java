package pkg.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import pkg.Constants;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;


public class SplitMergeController {

    @FXML
    private Button selectFileSplit;
    @FXML
    private Button selectFileMerge;
    @FXML
    private Button okBtn;
    @FXML
    private javafx.scene.control.TextField filePathField;
    @FXML
    private javafx.scene.control.TextField filePathField2;
    @FXML
    private Button splitBtn;
    @FXML
    private Button mergeBtn;

    private TextArea logger;
    @FXML
    private ScrollPane logPane;

    public void onBrowseButtonClicked(ActionEvent e) {
        JFrame frame = new JFrame();
        frame.setAlwaysOnTop(true);

        JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        fileChooser.setAcceptAllFileFilterUsed(false);

        if (e.getSource().equals(selectFileSplit) || e.getSource().equals(selectFileMerge)) {
            fileChooser.setDialogTitle("Choose a file");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int returnValue = fileChooser.showOpenDialog(frame);

            if (returnValue == JFileChooser.APPROVE_OPTION) {
                String dirPath = fileChooser.getSelectedFile().getPath();
                if (e.getSource().equals(selectFileSplit)) {
                    filePathField.setText(dirPath);
                } else if (e.getSource().equals(selectFileMerge)) {
                    filePathField2.setText(dirPath);
                }
            }
        }
    }

    public void onSplitFile(ActionEvent event) {

        File file = new File(filePathField.getText());
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
                File newFilePart = new File(Constants.tempDir, tempPartName);
                try (FileOutputStream fileOutputStream = new FileOutputStream(newFilePart)) {
                    fileOutputStream.write(buffer, 0, bytesLeft); // CHECK the bytesLeft variable, should it return the same number as the encoded byte array?
                    System.out.println("When Splitting: " + newFilePart.length());
                    totalFileSize = +newFilePart.length();
                }
            }
//            logger.appendText("\n" + partCount + " files were split! " + "The average file size was: " + totalFileSize / partCount + "bytes.");
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public File onMergeFile(ActionEvent event) {
        File firstFile = new File(filePathField2.getText());
        List<File> files = getFileList(firstFile);
        String extension = firstFile.getName().substring(firstFile.getName().indexOf("."));
        System.out.println(extension);
        File mergedFile = new File(Constants.splitFiles, firstFile.getName() + extension);

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

    public List<File> getFileList(File partFileSource) {
        String partFileName = partFileSource.getName();
        String finalFileName = partFileName.substring(0, partFileName.lastIndexOf('.'));

        File[] files = partFileSource.getParentFile().listFiles(
                (File dir, String name) -> name.matches(finalFileName + "[.]\\d+"));
        Arrays.sort(files);
        return Arrays.asList(files);
    }

    public void exit() {
        Stage stage = (Stage) okBtn.getScene().getWindow();
        stage.close();
    }

    public void initLog() {
        logger = new TextArea();
        logger.setWrapText(true);
        logPane.setContent(logger);
        logPane.setFitToWidth(true);

        logger.setText("Program started...");
    }
}
