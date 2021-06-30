package view;

import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Observable;
import java.util.Observer;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import view.Controllers.AppControllers;
import view.Graphs.AppGraphs;
import view.AttributesView.AppAttributes;
import view.ClocksPanel.AppClocksPanel;
import view.Joystick.AppJoystick;
import view_model.ViewModel;

public class MainWindowController implements Observer{
    private ViewModel vm;
    public StringProperty flightPath,propertiesPath;

    @FXML private AppJoystick joystick;
    @FXML private AppClocksPanel panel;
    @FXML private AppAttributes attributes;
    @FXML private AppGraphs graphs;
    @FXML private AppControllers controllers;
    @FXML private AnchorPane detectAP;


    public void setViewModel(ViewModel vm){
        this.vm=vm;
        flightPath=new SimpleStringProperty();
        vm.flightPath.bind(flightPath);
        propertiesPath=new SimpleStringProperty();
        vm.propertiesPath.bind(propertiesPath);
        controllers.controller.progressBar.valueProperty().bindBidirectional(vm.progress);
        controllers.controller.currentTime.textProperty().bind(vm.currentTime);
        vm.playSpeed.bind(controllers.controller.playSpeed);
        controllers.controller.onPlay=()->{
            if(flightPath.getValue()==null) {//If the user has not yet uploaded a flight file.
                errorMessage("Problem with flight recording file!\nYou must upload a CSV file.");
                return;
            }
            //else:
            vm.play();
        };
        controllers.controller.onPause=()->vm.pause();
        controllers.controller.onForward=()->vm.forward();
        controllers.controller.onRewind=()->vm.rewind();
        joystick.rudder.bind(vm.rudder);
        joystick.throttle.bind(vm.throttle);
        joystick.aileron.bind(vm.aileron);
        joystick.elevators.bind(vm.elevators);
        panel.heading.bind(vm.heading);
        panel.speed.bind(vm.speed);
        panel.altitude.bind(vm.altitude);
        panel.roll.bind(vm.roll);
        panel.pitch.bind(vm.pitch);
        panel.yaw.bind(vm.yaw);
        graphs.getSelectedAttributePoints().bind(vm.selectedAttributePoints);
        graphs.getTheMostCorrelativeAttributePoints().bind(vm.topCorrelativeAttributePoints);
        graphs.getTheMostCorrelativeAttribute().bind(vm.topCorrelativeAttribute);
    }

    private void errorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(message);
        alert.showAndWait();
    }

    private String getFilePath(String title,String desc,String ext){
        FileChooser fc = new FileChooser();
        fc.setTitle(title);
        fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(desc, ext));
        File file = fc.showOpenDialog(new Stage());
        if (file != null) {
            return file.getPath();
        }
        return null;
    }

    @FXML
    private void loadCsvBtn(){
        String filePath=getFilePath("Upload flight recording file - CSV","CSV file","*.csv*");
        if (filePath != null) {
            flightPath.setValue(filePath);
            attributes.LoadList();
            attributes.featureList.bind(vm.featureList);
            vm.selectedFeature.bind(attributes.selectedFeature);
            graphs.getSelectedFeature().bind(attributes.selectedFeature);
        }
    }

    @FXML
    private void loadXmlBtn(){
        String filePath=getFilePath("Upload properties file - XML","XML file","*.xml*");
        if (filePath != null) {
            propertiesPath.setValue(filePath);
        }
    }

    public AnchorPane getPainter() throws Exception {//this function ask for an anchorPane of the paint method on the anomaly algorithm
        return vm.getPainter().call();
    }

    @FXML
    private void loadAlgoBtn(){
        if(flightPath.getValue()==null){
            errorMessage("You must upload flight recording file before doing this!");
            return;
        }
        String filePath=getFilePath("Upload anomaly detection algorithm","JAR file","*.jar*");
        if (filePath != null) {
            try {
                vm.setAnomalyDetector(filePath);
                detectAP.getChildren().setAll(getPainter());
            }
            catch (MalformedURLException e) {
                errorMessage("Problem with selected JAR file!\nThis is not a valid JAR file.");
            }
            catch (ClassNotFoundException e) {
                errorMessage("Problem with selected JAR file!\nThere is no anomalyDetectors.Algorithm class.");
            }
            catch (InstantiationException e) {
                errorMessage("Problem with selected JAR file!\nYou don't realized the methods of the interface as well.");
            }
            catch (IllegalAccessException e) {
                errorMessage("Problem with selected JAR file!\nYou don't realized the methods of the interface as well.");
            }
            catch (Exception e) {
                errorMessage("Problem with selected JAR file!\nThe paint method is wrong.");
            }
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        switch (arg.toString()) {
            case "CSV file error":
                errorMessage("Problem with flight recording file!\nYou must upload a CSV file.");
                flightPath.setValue(null);
                break;
            case "properties file error":
                errorMessage("Problem with properties file!\nPlease try again.");
                break;
        }
    }
}