package view.Graphs;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import config.Point;
import java.net.URL;
import java.util.ResourceBundle;

public class GraphsController implements Initializable {
    @FXML private LineChart<Number, Number> selectedAttributeGraph, topCorrelativeAttributeGraph;
    @FXML public Label topCorrelativeAttribute, correlationPercentage;
    private String localSelectedFeature;
    private int localRowNumber;
    public ListProperty<Point> selectedAttributePoints, topCorrelativeAttributePoints;
    public StringProperty selectedFeature;

    public void updateChart(ObservableList<XYChart.Data<Number, Number>> seriesData, ObservableList<Point> points){
        seriesData.clear();
        for(int i =0;i<points.size();i++) {
            seriesData.add(new XYChart.Data(points.get(i).getX(), points.get(i).getY()));
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        selectedAttributePoints = new SimpleListProperty<>(FXCollections.observableArrayList());
        topCorrelativeAttributePoints = new SimpleListProperty<>(FXCollections.observableArrayList());
        selectedFeature = new SimpleStringProperty();
        localRowNumber=0;

        XYChart.Series<Number, Number> selectedAttributeSeries = new XYChart.Series<>();

        selectedAttributeGraph.getData().add(selectedAttributeSeries);

        XYChart.Series<Number, Number> topCorrelativeAttributeSeries = new XYChart.Series<>();
        topCorrelativeAttributeSeries.setName("Top correlative attribute");
        topCorrelativeAttributeGraph.getData().add(topCorrelativeAttributeSeries);


        selectedAttributePoints.addListener((observable, oldValue, newValue) -> {
            if (newValue.size() > 0) {
                boolean nameNotChanged=localSelectedFeature.intern()==selectedFeature.getValue().intern();
                if(nameNotChanged && localRowNumber+1==newValue.size()) {
                    Point newPoint = newValue.get(newValue.size() - 1);
                    selectedAttributeSeries.getData().add(new XYChart.Data(newPoint.getX(), newPoint.getY()));
                }
                else if(nameNotChanged && localRowNumber-1==newValue.size()) {
                    int length=selectedAttributeSeries.getData().size();
                    if(length>0) {
                        selectedAttributeSeries.getData().remove(length - 1);
                    }
                }
                else {
                    updateChart(selectedAttributeSeries.getData(), newValue);
                    localSelectedFeature=selectedFeature.getValue();
                }
                localRowNumber=newValue.size();
            }
        });
        topCorrelativeAttributePoints.addListener((observable, oldValue, newValue) -> {
            if (newValue.size() > 0) {
                boolean nameNotChanged=localSelectedFeature.intern()==selectedFeature.getValue().intern();
                if(nameNotChanged && localRowNumber+1==newValue.size()) {
                    Point newPoint = newValue.get(newValue.size() - 1);
                    topCorrelativeAttributeSeries.getData().add(new XYChart.Data(newPoint.getX(), newPoint.getY()));
                }
                else if(nameNotChanged && localRowNumber-1==newValue.size()) {
                    int length=topCorrelativeAttributeSeries.getData().size();
                    if(length>0) {
                        topCorrelativeAttributeSeries.getData().remove(length - 1);
                    }
                }
                else {
                    updateChart(topCorrelativeAttributeSeries.getData(), newValue);
                }
                localRowNumber=newValue.size();
            }
            else if (topCorrelativeAttributePoints.size() == 0){
                topCorrelativeAttributeSeries.getData().clear();
            }
        });
        selectedFeature.addListener((observable, oldValue, newValue) -> {
            if(localSelectedFeature==null) {
                localSelectedFeature = newValue;
            }
        });
    }
}
