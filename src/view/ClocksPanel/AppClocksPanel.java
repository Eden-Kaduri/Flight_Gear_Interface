package view.ClocksPanel;

import javafx.beans.property.DoubleProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import java.io.IOException;

public class AppClocksPanel extends Pane {
    public DoubleProperty heading,speed,altitude,roll,pitch,yaw;

    public AppClocksPanel() {
        super();
        FXMLLoader fxl = new FXMLLoader();
        try {
            Pane clockPanel = fxl.load(getClass().getResource("ClocksPanel.fxml").openStream());
            ClocksPanelController clocksPanelController = fxl.getController();

            heading=clocksPanelController.heading.valueProperty();
            speed=clocksPanelController.speed.valueProperty();
            altitude=clocksPanelController.altitude.valueProperty();
            roll=clocksPanelController.roll.valueProperty();
            pitch=clocksPanelController.pitch.valueProperty();
            yaw=clocksPanelController.yaw.valueProperty();

            this.getChildren().add(clockPanel);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
