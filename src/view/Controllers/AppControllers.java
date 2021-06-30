package view.Controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import java.io.IOException;

public class AppControllers extends Pane {

    public final ControllersController controller;

    public AppControllers() {
        super();
        FXMLLoader fxml = new FXMLLoader();
        Pane controllers=null;
        try {
            controllers = fxml.load(getClass().getResource("Controllers.fxml").openStream());
            this.getChildren().add(controllers);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        if(controllers!=null){
            controller = fxml.getController();
        }
        else{
            controller=null;
        }
    }
}
