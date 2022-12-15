package hu.petrik.etlap;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.util.Optional;

public class EtelFormView
{
    @FXML
    private TextField nevField;
    @FXML
    private Button hozzaadasButton;
    @FXML
    private Spinner<Integer> arSpinner;
    @FXML
    private TextArea leirasField;
    @FXML
    private MenuButton menuButton;
    @FXML
    private MenuItem eloetelMenuItem;
    @FXML
    private MenuItem foetelMenuItem;
    @FXML
    private MenuItem desszertMenuItem;

    private EtelDB db;

    public void initialize(){
        arSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(100,2500,1250));
    }

    @FXML
    public void hozzaadasClick(ActionEvent actionEvent) {
        String nev = nevField.getText().trim();
        String leiras = leirasField.getText();
        int ar = arSpinner.getValue();
        String kategoria = menuButton.getText();
        Etel etel = new Etel(nev, leiras, ar, kategoria);
        try {
            if (db.createEtel(etel)){
                alert(Alert.AlertType.WARNING, "Sikeres felvétel!", "");
            }else{
                alert(Alert.AlertType.WARNING, "Sikertelen felvétel!", "");
            }
        } catch (SQLException e) {
            Platform.runLater(() -> {
                alert(Alert.AlertType.WARNING, "Hiba történt az adatbázis kapcsolat kialakításakor!",
                        e.getMessage());
            });
        }
        Platform.exit();
    }

    private Optional<ButtonType> alert(Alert.AlertType alertType, String headerText, String contentText){
        Alert alert = new Alert(alertType);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        return alert.showAndWait();
    }

    @FXML
    public void menuItem1Select(ActionEvent actionEvent) {
        menuButton.setText(eloetelMenuItem.getText());
    }

    @FXML
    public void menuItem2Select(ActionEvent actionEvent) {
        menuButton.setText(foetelMenuItem.getText());
    }

    @FXML
    public void menuItem3Select(ActionEvent actionEvent) {
        menuButton.setText(desszertMenuItem.getText());
    }
}