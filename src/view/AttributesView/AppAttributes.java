package view.AttributesView;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import java.io.IOException;

public class AppAttributes extends Pane {

    public StringProperty selectedFeature;
    public ListProperty<String> featureList;
    public ListView<String> attrView;

    public void LoadList() {
        FXMLLoader fxl = new FXMLLoader();
        try {
            Pane attr = fxl.load(getClass().getResource("AttributesView.fxml").openStream());
            AttributesViewController attributesViewController = fxl.getController();
            featureList =new SimpleListProperty<>(FXCollections.observableArrayList());
            attrView = attributesViewController.attributeslistView;
            featureList.addListener((observable, oldValue, newValue) -> {
                attrView.setItems(newValue);
                attrView.getSelectionModel().select(0);
                selectedFeature=new SimpleStringProperty(attrView.getSelectionModel().getSelectedItems().get(0));
            });

            attrView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if(selectedFeature!=null) {
                    selectedFeature.setValue(newValue);
                }
            });

            this.getChildren().add(attr);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
