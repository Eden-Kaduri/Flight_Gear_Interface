package view_model;

import anomalyDetectors.*;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.scene.layout.AnchorPane;
import model.Model;
import config.PearsonCorrelation;
import config.Point;
import config.Properties;
import config.TimeSeries;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ViewModel extends Observable{
    private Model m;
    private Properties properties;
    private TimeSeries ts, regularTs;
    private int csvLength,localNumOfRow;
    private String localSelectedFeature;
    private ExecutorService executor;
    private Map<String, CorrelatedFeatures> correlatedFeaturesMap;
    public DoubleProperty playSpeed, progress,throttle,rudder,aileron,elevators,heading,speed,altitude,roll,pitch,yaw;
    public StringProperty flightPath,propertiesPath,currentTime,selectedFeature, topCorrelativeAttribute, correlationPercentage;
    public ListProperty<Point> selectedAttributePoints, topCorrelativeAttributePoints;
    public ListProperty<String> featureList;
    public IntegerProperty numOfRow;

    public ViewModel(Model m){
        this.m=m;
        executor = Executors.newSingleThreadExecutor();
        numOfRow= new SimpleIntegerProperty();
        numOfRow.bind(m.numOfRow);
        playSpeed = new SimpleDoubleProperty();
        progress = new SimpleDoubleProperty();
        throttle = new SimpleDoubleProperty();
        rudder = new SimpleDoubleProperty();
        aileron = new SimpleDoubleProperty();
        elevators = new SimpleDoubleProperty();
        heading = new SimpleDoubleProperty();
        speed = new SimpleDoubleProperty();
        altitude = new SimpleDoubleProperty();
        roll = new SimpleDoubleProperty();
        pitch = new SimpleDoubleProperty();
        yaw = new SimpleDoubleProperty();
        flightPath = new SimpleStringProperty();
        propertiesPath=new SimpleStringProperty();
        currentTime = new SimpleStringProperty("0:0");
        selectedFeature=new SimpleStringProperty();
        topCorrelativeAttribute =new SimpleStringProperty("");
        selectedAttributePoints=new SimpleListProperty(FXCollections.observableArrayList());
        topCorrelativeAttributePoints =new SimpleListProperty(FXCollections.observableArrayList());
        featureList =new SimpleListProperty(FXCollections.observableArrayList());
        correlationPercentage = new SimpleStringProperty();
        properties=new Properties();
        properties.deserializeFromXML("settings.xml");//the default path for the properties file
        m.setProperties(properties);
        localSelectedFeature="";

        playSpeed.addListener((observable, oldValue, newValue)-> m.setPlaySpeed((double)newValue));
        flightPath.addListener((observable, oldValue, newValue) -> {
            if(newValue!=null) {
                try {
                    ts = new TimeSeries(newValue);
                    regularTs =new TimeSeries(properties.getNormalFlightCsvPath());
                    if ((ts.getNumOfColumns() != 42) || (regularTs.getNumOfColumns() != 42)) {
                        throw new Exception();
                    }
                }
                catch (Exception e) {
                    setChanged();
                    notifyObservers("CSV file error");
                    return;
                }
                m.setTimeSeries(ts);
                csvLength = ts.getRowSize();

                PearsonCorrelation pearsonCorrelation=new PearsonCorrelation();
                correlatedFeaturesMap=pearsonCorrelation.getTheMostCorrelatedFeaturesMap(regularTs);
                featureList.set(ts.getAttributes());
            }
        });

        propertiesPath.addListener((observable, oldValue, newValue) -> {
            if(!properties.deserializeFromXML(newValue)){
                propertiesPath.setValue(null);
                setChanged();
                notifyObservers("properties file error");
            }
            else{
                m.setProperties(properties);
            }
        });
        progress.addListener((observable, oldValue, newValue)->{
            if(ts!=null)
                m.setProgression((int)(ts.getRowSize() * newValue.doubleValue()));});
        selectedFeature.addListener((observable, oldValue, newValue) -> {
            if(ts!=null){
                if(correlatedFeaturesMap.containsKey(newValue)){//find who is the MostCorrelativeAttribute from the map
                    String temp=correlatedFeaturesMap.get(newValue).feature1;
                    if(!temp.equals(selectedFeature.getValue())) {
                        topCorrelativeAttribute.setValue(temp);
                    }
                    else{
                        topCorrelativeAttribute.setValue(correlatedFeaturesMap.get(newValue).feature2);
                    }
                    correlationPercentage.set(String.valueOf(Math.abs(correlatedFeaturesMap.get(newValue).correlation*100)).substring(0,5)+"% of coronation");
                }
                else{
                    topCorrelativeAttribute.setValue(""); //there is no correlative feature
                    correlationPercentage.set("");
                }
            }
        });
        numOfRow.addListener((observable, oldValue, newValue) -> {
            executor.execute(() -> progress.setValue((double)numOfRow.getValue()/csvLength));
            executor.execute(() -> setTime(numOfRow.getValue()));
            executor.execute(() -> throttle.setValue(ts.getDataFromSpecificRowAndColumn(properties.propertyName("throttle"),numOfRow.getValue())));
            executor.execute(() -> rudder.setValue(ts.getDataFromSpecificRowAndColumn(properties.propertyName("rudder"),numOfRow.getValue())));
            executor.execute(() -> aileron.setValue(ts.getDataFromSpecificRowAndColumn(properties.propertyName("aileron"),numOfRow.getValue())*40));
            executor.execute(() -> elevators.setValue(ts.getDataFromSpecificRowAndColumn(properties.propertyName("elevators"),numOfRow.getValue())*40));
            executor.execute(() -> heading.setValue(ts.getDataFromSpecificRowAndColumn(properties.propertyName("heading"),numOfRow.getValue())));
            executor.execute(() -> speed.setValue(ts.getDataFromSpecificRowAndColumn(properties.propertyName("speed"),numOfRow.getValue())));
            executor.execute(() -> altitude.setValue(ts.getDataFromSpecificRowAndColumn(properties.propertyName("altitude"),numOfRow.getValue())));
            executor.execute(() -> roll.setValue(ts.getDataFromSpecificRowAndColumn(properties.propertyName("roll"),numOfRow.getValue())));
            executor.execute(() -> pitch.setValue(ts.getDataFromSpecificRowAndColumn(properties.propertyName("pitch"),numOfRow.getValue())));
            executor.execute(() -> yaw.setValue(ts.getDataFromSpecificRowAndColumn(properties.propertyName("yaw"),numOfRow.getValue())));

            boolean nameNotChanged=localSelectedFeature.intern()==selectedFeature.getValue().intern();

            if(nameNotChanged && localNumOfRow+1==numOfRow.getValue()) {
                Point point=new Point(numOfRow.getValue(),ts.getAttributeData(localSelectedFeature).get(numOfRow.getValue()));
                Platform.runLater(()->selectedAttributePoints.add(point));
                if(topCorrelativeAttribute.getValue().intern()!=("")){
                    Point point2=new Point(numOfRow.getValue(),ts.getAttributeData(topCorrelativeAttribute.getValue()).get(numOfRow.getValue()));
                    Platform.runLater(()-> topCorrelativeAttributePoints.add(point2));
                }
            }
            else if(nameNotChanged && localNumOfRow-1==numOfRow.getValue()) {
                Platform.runLater(()->{
                int length=selectedAttributePoints.size();
                if(length>0) {
                    selectedAttributePoints.remove(length - 1);
                    if(topCorrelativeAttribute.getValue().intern()!=("")) {
                        topCorrelativeAttributePoints.remove(length - 1);
                      }
                    }
                });
            }
            else {
                ListProperty tempList=ts.getListOfPointsUntilSpecificRow(selectedFeature.getValue(),numOfRow.getValue());
                Platform.runLater(()-> selectedAttributePoints.setValue(tempList));
                localSelectedFeature=selectedFeature.getValue();
                if(topCorrelativeAttribute.getValue().intern()!=("").intern()){
                    ListProperty tempList2=ts.getListOfPointsUntilSpecificRow(topCorrelativeAttribute.getValue(),numOfRow.getValue());
                    Platform.runLater(()-> topCorrelativeAttributePoints.setValue(tempList2));
                    }
                }
                localNumOfRow=numOfRow.getValue();
                if(topCorrelativeAttribute.getValue().intern()==("").intern()){
                    Platform.runLater(()-> topCorrelativeAttributePoints.setValue(new SimpleListProperty(FXCollections.observableArrayList())));
                }
            });
    }
    private void setTime(int rowNumber)
    {
        int totalMilliseconds=(int)(rowNumber*properties.getRate());
        int seconds=(totalMilliseconds / 1000) % 60;
        int minutes=(totalMilliseconds / 60000) % 60;
        Platform.runLater(() -> currentTime.set((minutes+":"+seconds)));
    }
    public void shutdownExecutor() { executor.shutdown(); }

    public void play(){
        m.play((int)(progress.getValue()* ts.getRowSize()));
    }
    public void pause() {
        m.pause();
    }
    public void forward(){ m.forward(); }
    public void rewind(){ m.rewind(); }

    public Callable<AnchorPane> getPainter() throws Exception {
        return m.getPainter();
    }

    public void setAnomalyDetector(String classPath) throws MalformedURLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        URLClassLoader urlClassLoader = URLClassLoader.newInstance(new URL[] {
                new URL("file:///"+classPath)
        });
        Class<?> c=urlClassLoader.loadClass("anomalyDetectors.Algorithm");
        AnomalyDetector ad=(AnomalyDetector) c.newInstance();
        if(!m.setAnomalyDetector(ad,selectedFeature, regularTs)){
           throw new IllegalAccessException();
        }
    }
}
