package view.Joystick;


import javafx.beans.property.DoubleProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import java.io.IOException;

public class AppJoystick extends Pane {

    public DoubleProperty throttle, rudder,aileron,elevators;

    public AppJoystick(){
        super();
        FXMLLoader fxml=new FXMLLoader();
        try {
            Pane joystick=fxml.load(getClass().getResource("JoystickWindow.fxml").openStream());
            JoystickWindowController joystickWindowController=fxml.getController();

            aileron=joystickWindowController.getJoystickControl().centerXProperty();
            elevators=joystickWindowController.getJoystickControl().centerYProperty();
            rudder=joystickWindowController.getRudder().valueProperty();
            throttle=joystickWindowController.getThrottle().valueProperty();

            this.getChildren().add(joystick);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}