package pkg.Controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.w3c.dom.CDATASection;
import pkg.model.DateTime;

import javax.swing.*;
import java.io.IOException;
import java.time.LocalDate;

public class BackUpController {

    @FXML
    public Button okBtn;
    @FXML
    TableView backupTable;
    @FXML
    TableColumn dateTimeCol;
    @FXML
    TableColumn dirCol;
    TableRow<DateTime> row;
    DateTime dateTime;
    @FXML
    private ComboBox hoursBox;
    @FXML
    private ComboBox minutesBox;
    @FXML
    private ComboBox timeOfDayBox;
    @FXML
    private Button browseBtn;
    @FXML
    private DatePicker datePicker;
    private ObservableList<DateTime> dateTimeList =
            FXCollections.observableArrayList();

    public void start() {

        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/pkg/view/backupView.fxml"));

            Scene scene = new Scene(fxmlLoader.load(), 600, 700);
            Stage stage = new Stage();
            stage.setTitle("Auto BackUp");
            stage.setScene(scene);
            stage.show();


        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void onAddClick(ActionEvent e) {

        dateTimeCol.setCellValueFactory(new PropertyValueFactory<DateTime, String>("dayTimeFormat"));
        //    dirCol.setCellValueFactory(new PropertyValueFactory<DateTime, String>("directory"));

        dateTime = getDateTime();
        dateTimeList.add(dateTime);
        dateTime.setDayTimeFormat();
        backupTable.getItems().add(dateTime);
        initTableView();


        System.out.println(dateTime.getHour() + " " + dateTime.getMin() + " " + dateTime.getTimeOfDay());
    }

    public void onRemoveClick(ActionEvent e) {
        DateTime selectedItem = (DateTime) backupTable.getSelectionModel().getSelectedItem();
        backupTable.getItems().remove(selectedItem);
    }

    public void onBrowseButtonClicked(ActionEvent e) {
        String osName = System.getProperty("os.name");
        String homeDir = System.getProperty("user.home");

        System.out.print(osName + " " + homeDir);
        JFileChooser fc = new JFileChooser(homeDir);

        // The user should click the browse button which will open a dialog to the file system
        if (e.getSource().equals(browseBtn)) {
            int returnVal = fc.showOpenDialog(null);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                //   fileName = file.getName();
                //   System.out.println(" The file path is " + file.getAbsolutePath());
                //   encFile = encryptFile(file, password);
                /*

                 */
                //  splitFile(encFile);
            } else {
                System.out.println("The dialog was cancelled by the user");
            }
        }
    }


    public DateTime getDateTime() {

        // Getting the time from the user input

        int hours = Integer.parseInt((hoursBox.getValue().toString()));
        int minutes = Integer.parseInt((minutesBox.getValue().toString()));
        String timeOfDay = timeOfDayBox.getValue().toString();

        // Getting the date from the user input

        LocalDate localDate;
        localDate = datePicker.getValue();

        return new DateTime(hours, minutes, timeOfDay, localDate);
    }

    public void initTableView() {
        backupTable.setRowFactory(tableView -> {
            row = new TableRow<>();
            row.setOnMouseClicked(mouseEvent -> {
                if (!row.isEmpty()) {
                    DateTime dateTime = row.getItem();
                    System.out.println(dateTime.getHour() + " " + dateTime.getLocalDate());
                }
            });
            return row;
        });
    }


    public void exit() {
        Stage stage = (Stage) okBtn.getScene().getWindow();
        stage.close();
    }
}

