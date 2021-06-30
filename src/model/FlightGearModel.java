package model;

import anomalyDetectors.AnomalyDetector;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.AnchorPane;
import config.FlightGear;
import config.Properties;
import config.TimeSeries;
import java.util.concurrent.Callable;

public class FlightGearModel implements Model{
    private FlightGear fgPlayer;
    private Properties properties;
    private TimeSeries ts;
    private AnomalyDetector ad;
    private Thread thread;
    private double playSpeed;
    private boolean firstPlay;

    public FlightGearModel() {
        firstPlay=true;
    }

    @Override
    public boolean setAnomalyDetector(AnomalyDetector ad, StringProperty feature,TimeSeries regularTs) {
        if(feature.getValue()==null || regularTs==null)
            return false;
        try {
            this.ad=ad;
            this.ad.selectedFeature.bind(feature);
            this.ad.numOfRow.bindBidirectional(numOfRow);
            ad.learnNormal(regularTs);
            ad.detect(ts);
            return true;
        }
        catch (Exception e){
            return false;
        }
    }

    @Override
    public Callable<AnchorPane> getPainter() {
        return ()->ad.paint();
    }


    @Override
    public void setProperties(Properties prop) {
        this.properties = prop;
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public void setTimeSeries(TimeSeries ts) {
        this.ts=ts;
    }

    @Override
    public void setProgression(int rowNumber) {
        if (!firstPlay && !(rowNumber-3<this.numOfRow.getValue() && this.numOfRow.getValue()<rowNumber+3)) {
            thread.stop();
            thread=new Thread()
            {
                public void run() {
                    fgPlayer.play(rowNumber);
                }
            };
            thread.setDaemon(true);
            thread.start();
        }
    }
    @Override
    public void setPlaySpeed(double value) {
        playSpeed=value;
        if(!firstPlay) {
            fgPlayer.setPlaySpeed(value);
        }
    }
    @Override
    public void play(int start) {
        if(firstPlay) {
            firstPlay=false;
            fgPlayer=new FlightGear(ts,playSpeed,this);
            thread=new Thread()
            {
                public void run() {
                    fgPlayer.play(start);
                }
            };
            thread.setDaemon(true);
            thread.start();
        }
        else if(fgPlayer.isForwardInProgress()) {
            fgPlayer.unForward();
        }
        else if(fgPlayer.isRewindInProgress()) {
            fgPlayer.setRewind(false);
            thread.stop();
            thread=new Thread()
            {
                public void run() {
                    fgPlayer.play(start);
                }
            };
            thread.setDaemon(true);
            thread.start();
        }
        else {
            fgPlayer.wake();
        }
    }

    @Override
    public void pause() {
        if(fgPlayer!=null && !fgPlayer.isPause()) {
            fgPlayer.setPause(true);
        }
    }

    @Override
    public void forward() {
        if(fgPlayer!=null) {
            fgPlayer.forward();
        }
    }

    @Override
    public void rewind() {
        if(fgPlayer!=null && !fgPlayer.isRewindInProgress()) {
            fgPlayer.setRewind(true);
            thread.stop();
            thread=new Thread()
            {
                public void run() {
                    fgPlayer.rewind(numOfRow.getValue());
                }
            };
            thread.setDaemon(true);
            thread.start();
        }
    }

    @Override
    public void setNumOfRow(int numOfRow){
        this.numOfRow.setValue(numOfRow);
    }
}

