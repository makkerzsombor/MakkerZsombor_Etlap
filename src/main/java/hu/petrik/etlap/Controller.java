package hu.petrik.etlap;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Controller {

    @FXML
    private Button ujEtelButton;
    @FXML
    private Button torlesButton;
    @FXML
    private Spinner<Integer> szazalekSpinner;
    @FXML
    private Button szazalekEmelesButton;
    @FXML
    private Spinner<Integer> ftSpinner;
    @FXML
    private Button ftEmelesButton;
    @FXML
    private TableView<Etel> Etlap;
    @FXML
    private TableColumn<Etel, String> nevCol;
    @FXML
    private TableColumn<Etel, String> katCol;
    @FXML
    private TableColumn<Etel, Integer> arCol;
    @FXML
    private ListView<String> leiras;

    private EtelDB db;

    private List<Etel> lista= new ArrayList<Etel>();

    private int updateId;
    @FXML
    public void initialize(){
        nevCol.setCellValueFactory(new PropertyValueFactory<>("nev"));
        katCol.setCellValueFactory(new PropertyValueFactory<>("kategoria"));
        arCol.setCellValueFactory(new PropertyValueFactory<>("ar"));
        szazalekSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5,50,10, 5));
        ftSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(50,3000,1250, 50));
        try {
            db = new EtelDB();
            readEtelek();
        } catch (SQLException e) {
            Platform.runLater(() -> {
                alert(Alert.AlertType.WARNING, "Hiba történt az adatbázis kapcsolat kialakításakor!",
                        e.getMessage());
            });
        }
    }

    private void readEtelek() throws SQLException {
        List<Etel> etelek = db.readEtelek();
        Etlap.getItems().clear();
        Etlap.getItems().addAll(etelek);
        lista.addAll(etelek);
    }

    private void sqlAlert(SQLException e) {
        Platform.runLater(() -> {
            alert(Alert.AlertType.WARNING, "Hiba történt az adatbázis kapcsolat kialakításakor!",
                    e.getMessage());
        });
    }

    private Optional<ButtonType> alert(Alert.AlertType alertType, String headerText, String contentText){
        Alert alert = new Alert(alertType);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        return alert.showAndWait();
    }

    @FXML
    public void ujEtelClick(ActionEvent actionEvent) {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("etel-form-view.fxml"));
        Scene scene = null;
        try {
            scene = new Scene(fxmlLoader.load(), 400, 320);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Stage stage = new Stage();
        stage.setTitle("Étel létrehozása");
        stage.setScene(scene);
        EtelFormView controller = fxmlLoader.getController();
        stage.show();
        try {
            readEtelek();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void torlesClick(ActionEvent actionEvent) {
        Etel selected = getSelectedEtel();
        if (selected == null) return;

        Optional<ButtonType> optionalButtonType = alert(Alert.AlertType.CONFIRMATION,"Biztos, hogy törölni szeretné a kiválasztott ételt?","");
        if (optionalButtonType.isEmpty() || !optionalButtonType.get().equals(ButtonType.OK) && !optionalButtonType.get().equals(ButtonType.YES)){
            return;
        }
        try {
            if (db.deleteEtel(selected.getId())) {
                alert(Alert.AlertType.WARNING, "Sikeres Törlés!", "");
            }else{
                alert(Alert.AlertType.WARNING, "Sikertelen törlés!", "");
            }
            readEtelek();
        } catch (SQLException e) {
            sqlAlert(e);
        }
        leiras.getItems().clear();
    }
    private Etel getSelectedEtel() {
        int selectedIndex = Etlap.getSelectionModel().getSelectedIndex();
        if (selectedIndex == -1){
            alert(Alert.AlertType.WARNING, "Előbb válasszon ki egy ételt a táblázatból!","");
            return null;
        }
        Etel selected = Etlap.getSelectionModel().getSelectedItem();
        return selected;
    }

    @FXML
    public void szazalekEmelesClick(ActionEvent actionEvent) {
        int selectedIndex = Etlap.getSelectionModel().getSelectedIndex();

        if (selectedIndex != -1){
            Optional<ButtonType> optionalButtonType = alert(Alert.AlertType.CONFIRMATION,"Biztos, hogy emelni szeretné a kiválasztott ételt?","");
            if (optionalButtonType.isEmpty() || !optionalButtonType.get().equals(ButtonType.OK) && !optionalButtonType.get().equals(ButtonType.YES)){
                return;
            }
            Etel selected = Etlap.getSelectionModel().getSelectedItem();
            updateId = selected.getId();
            double szazalek = szazalekSpinner.getValue();
            szazalek = 1 + (szazalek/100);
            Etel etel = new Etel(updateId, selected.getNev(), selected.getLeiras(), selected.getAr(), selected.getKategoria());
            try{
                if (db.updateEgyEtelSzazlek(etel, szazalek)){
                    alert(Alert.AlertType.WARNING, "Sikeres modosítás!", "");
                    readEtelek();
                }else{
                    alert(Alert.AlertType.WARNING, "Sikertelen modosítás!", "");
                }
            }catch (SQLException e){
                sqlAlert(e);
            }

        }else{
            Optional<ButtonType> optionalButtonType = alert(Alert.AlertType.CONFIRMATION,"Biztos, hogy emelni szeretné az ételeket?","");
            if (optionalButtonType.isEmpty() || !optionalButtonType.get().equals(ButtonType.OK) && !optionalButtonType.get().equals(ButtonType.YES)){
                return;
            }
            double szazalek = szazalekSpinner.getValue();
            szazalek = 1 + (szazalek/100);
            boolean siker = false;
            int i = 0;
            while(lista.size() > i){
                Etel etel = new Etel (lista.get(i).getId(), lista.get(i).getNev(),  lista.get(i).getLeiras(), lista.get(i).getAr(), lista.get(i).getKategoria());
                try {
                    if(db.updateEgyEtelSzazlek(etel, szazalek)) {
                        siker = true;
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                i++;
            }
            if (siker){
                alert(Alert.AlertType.WARNING, "Sikeres modosítás!", "");
                try {
                    readEtelek();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }else{
                alert(Alert.AlertType.WARNING, "Sikertelen modosítás!", "");
            }
        }
    }

    @FXML
    public void ftEmelesClick(ActionEvent actionEvent) {
        int selectedIndex = Etlap.getSelectionModel().getSelectedIndex();

        if (selectedIndex != -1){
            Optional<ButtonType> optionalButtonType = alert(Alert.AlertType.CONFIRMATION,"Biztos, hogy emelni szeretné a kiválasztott ételt?","");
            if (optionalButtonType.isEmpty() || !optionalButtonType.get().equals(ButtonType.OK) && !optionalButtonType.get().equals(ButtonType.YES)){
                return;
            }
            Etel selected = Etlap.getSelectionModel().getSelectedItem();
            updateId = selected.getId();
            int ar = ftSpinner.getValue();
            Etel etel = new Etel(updateId, selected.getNev(), selected.getLeiras(), selected.getAr(), selected.getKategoria());
            try{
                if (db.updateEgyEtelFt(etel, ar)){
                    alert(Alert.AlertType.WARNING, "Sikeres modosítás!", "");
                    readEtelek();
                }else{
                    alert(Alert.AlertType.WARNING, "Sikertelen modosítás!", "");
                }
            }catch (SQLException e){
                sqlAlert(e);
            }

        }else{
            Optional<ButtonType> optionalButtonType = alert(Alert.AlertType.CONFIRMATION,"Biztos, hogy emelni szeretné az ételeket?","");
            if (optionalButtonType.isEmpty() || !optionalButtonType.get().equals(ButtonType.OK) && !optionalButtonType.get().equals(ButtonType.YES)){
                return;
            }
            int ar = ftSpinner.getValue();
            boolean siker = false;
            int i = 0;
            while(lista.size() > i){
                Etel etel = new Etel (lista.get(i).getId(), lista.get(i).getNev(),  lista.get(i).getLeiras(), lista.get(i).getAr(), lista.get(i).getKategoria());
                try {
                    if(db.updateEgyEtelFt(etel, ar)) {
                        siker = true;
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                i++;
            }
            if (siker){
                alert(Alert.AlertType.WARNING, "Sikeres modosítás!", "");
                try {
                    readEtelek();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }else{
                alert(Alert.AlertType.WARNING, "Sikertelen modosítás!", "");
            }
        }
    }

    @FXML
    public void tableViewClick(Event event) {
        leiras.getItems().clear();
        leiras.getItems().add(getSelectedEtel().getLeiras());
    }

    @FXML
    public void sortList(Event event) {
        Etlap.getOnSort();
    }
}