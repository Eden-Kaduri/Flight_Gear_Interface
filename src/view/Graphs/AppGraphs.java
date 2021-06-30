package view.Graphs;

import javafx.beans.property.ListProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import config.Point;
import java.io.IOException;

public class AppGraphs extends Pane{

    private GraphsController controller;

    public AppGraphs(){
        super();
        FXMLLoader fxl=new FXMLLoader();
        try {
            Pane adg=fxl.load(getClass().getResource("Graphs.fxml").openStream());
            controller=fxl.getController();
            this.getChildren().add(adg);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    public ListProperty<Point> getSelectedAttributePoints() { return controller.selectedAttributePoints; }
    public ListProperty<Point> getTheMostCorrelativeAttributePoints() { return controller.topCorrelativeAttributePoints; }
    public StringProperty getSelectedFeature(){return controller.selectedFeature; }
    public StringProperty getTheMostCorrelativeAttribute(){return controller.topCorrelativeAttribute.textProperty(); }
}