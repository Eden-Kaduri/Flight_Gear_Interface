package view;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import model.FlightGearModel;
import view_model.ViewModel;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxl=new FXMLLoader(getClass().getResource("MainWindow.fxml"));
        Parent root= fxl.load();
        primaryStage.setTitle("Flight Simulator");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        FlightGearModel flightGearModel=new FlightGearModel();
        ViewModel vm=new ViewModel(flightGearModel);
        MainWindowController mwc=fxl.getController();
        mwc.setViewModel(vm);
        vm.addObserver(mwc);

        primaryStage.setOnCloseRequest(event -> {
            vm.shutdownExecutor();
            Platform.exit();
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
