package model;

import anomalyDetectors.AnomalyDetector;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.AnchorPane;
import config.Properties;
import config.TimeSeries;

import java.util.concurrent.Callable;

public interface Model {
    IntegerProperty numOfRow= new SimpleIntegerProperty(0);

    boolean setAnomalyDetector(AnomalyDetector ad, StringProperty feature,TimeSeries regularTs);
    Callable<AnchorPane> getPainter() throws Exception;
    void setPlaySpeed(double value);
    void setProperties(Properties prop);
    void setTimeSeries(TimeSeries ts);
    void setProgression(int rowNumber);
    void setNumOfRow(int numOfRow);
    Properties getProperties();
    void play(int start);
    void pause();
    void forward();
    void rewind();
}
