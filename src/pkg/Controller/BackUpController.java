package pkg.Controller;

import com.google.gson.GsonBuilder;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;
import pkg.model.ScheduleItem;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class BackUpController extends Frame {

    @FXML
    public Button okBtn;
    @FXML
    TableView backupTable;
    @FXML
    TableColumn dirCol;
    TableRow<Path> row;
    Path schedulePath;
    @FXML
    Button addRow;
    @FXML
    TextField filePathField;
    @FXML
    private Button selectFolderBtn;
    @FXML
    private ArrayList<ScheduleItem> scheduleItems = new ArrayList<>();
    private Preferences prefs = Preferences.userRoot().node(this.getClass().getName());

    public void start() throws BackingStoreException {
        prefs = Preferences.userRoot().node(this.getClass().getName());
        dirCol.setCellValueFactory(new PropertyValueFactory<>("path"));

        scheduleItems = deSerialize();

        if (!scheduleItems.isEmpty()) {
            for (ScheduleItem scheduleItem : scheduleItems) {
                backupTable.getItems().add(scheduleItem);
            }
        }
    }

    public void onAddClick(ActionEvent e) {
        addDir();
    }

    public void addDir() {
        ScheduleItem scheduleItem = new ScheduleItem(filePathField.getText());
        scheduleItems.add(scheduleItem);
        backupTable.getItems().add(scheduleItem);
    }


    public void onRemoveClick(ActionEvent e) {
        ScheduleItem selectedItem = (ScheduleItem) backupTable.getSelectionModel().getSelectedItem();
        backupTable.getItems().remove(selectedItem);
    }

    public void serialize(ArrayList<ScheduleItem> scheduleItems) {
        GsonBuilder gson = new GsonBuilder();
        String serializedItems = gson.create().toJson(scheduleItems);

        System.out.println("Serialized items: " + serializedItems);
        prefs.put("scheduleList", serializedItems);
    }

    public ArrayList<ScheduleItem> deSerialize() {
        //   java.lang.reflect.Type arrayType = new TypeToken<ArrayList<ScheduleItem>>() {}.getType();

        String serializedItems;

        System.out.println("stored in prefs: " + prefs.get("scheduleList", ""));
        if (!prefs.get("scheduleList", "").equals("")) {  // something in the array
            serializedItems = prefs.get("scheduleList", "");
        } else {                                                // nothing in the array
            System.out.println("empty");
            return new ArrayList<ScheduleItem>();
        }

        JSONArray jsonArray = new JSONArray(serializedItems);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String jsonString = jsonObject.toString();
            ScheduleItem scheduleItem = new ScheduleItem(jsonString);
            scheduleItems.add(scheduleItem);
        }
        return scheduleItems;
    }

    // Method invocation for browsing for a file or directory
    public void onBrowseButtonClicked(ActionEvent e) {
        JFrame frame = new JFrame();
        frame.setAlwaysOnTop(true);

        JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        fileChooser.setAcceptAllFileFilterUsed(false);

        if (e.getSource().equals(selectFolderBtn)) {
            fileChooser.setDialogTitle("Choose a directory");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnValue = fileChooser.showOpenDialog(frame);

            if (returnValue == JFileChooser.APPROVE_OPTION) {
                String dirPath = fileChooser.getSelectedFile().getPath();
                filePathField.setText(dirPath);
            }
        }
    }

    public void exit() {
        serialize(scheduleItems);
        Stage stage = (Stage) okBtn.getScene().getWindow();
        stage.close();
    }
}

