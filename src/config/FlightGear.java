package config;

import model.Model;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class FlightGear {
    private Socket flightGear;
    private PrintWriter out;
    private double playSpeed, prevPlaySpeed;
    private TimeSeries ts;
    private Model flightSimulatorModel;
    private boolean pause, connectionIsClose, moveForward, moveRewind;

    public FlightGear(TimeSeries ts, double speed, Model flightSimulatorModel) {
        connectionIsClose = true;
        this.ts = ts;
        playSpeed = speed;
        this.flightSimulatorModel = flightSimulatorModel;
        moveForward = false;
        moveRewind = false;
    }

    public boolean isForwardInProgress() {
        return moveForward;
    }

    public void setForward(boolean flag) {
        this.moveForward = flag;
    }

    public boolean isRewindInProgress() {
        return moveRewind;
    }

    public void setRewind(boolean flag) {
        this.moveRewind = flag;
    }

    public boolean isPause() {
        return pause;
    }

    public void setPause(boolean pause) {
        this.pause = pause;
    }

    private boolean createConnection() {
        try {
            flightGear = new Socket(flightSimulatorModel.getProperties().getFlightGearIP(), flightSimulatorModel.getProperties().getFlightGearPort());
            out = new PrintWriter(flightGear.getOutputStream());
            connectionIsClose = false;
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public synchronized void play(int start) {
        if (connectionIsClose) {
            createConnection();
        }
        try {
            for (int i = start; i < ts.getRowSize(); i++) {
                while (pause) {
                    wait();
                }
                try {
                    out.println(ts.getRowByRowNumber(i));
                    out.flush();
                } catch (Exception e) { }
                flightSimulatorModel.setNumOfRow(i);
                Thread.sleep((long) (flightSimulatorModel.getProperties().getRate()/ playSpeed));
            }
            out.close();
            flightGear.close();
            connectionIsClose = true;
        } catch (Exception e) { }
    }

    public synchronized void wake() {
        setPause(false);
        notify();
    }

    public void setPlaySpeed(double speed) {
        playSpeed = speed;
    }

    public double getPlaySpeed() {
        return playSpeed;
    }

    public void forward() {
        setForward(true);
        prevPlaySpeed = getPlaySpeed();
        setPlaySpeed(3.5);
    }

    public void rewind(int start) {
        if (connectionIsClose) {
            createConnection();
        }
        try {
            for (int i = start; i >= 0; i--) {
                try {
                    out.println(ts.getRowByRowNumber(i));
                    out.flush();
                } catch (Exception e) {
                }
                flightSimulatorModel.setNumOfRow(i);
                Thread.sleep((long) (flightSimulatorModel.getProperties().getRate() / 3.5));
            }
            stop();
        }
        catch (Exception e) { }
    }

    public void unForward() {
        if (isForwardInProgress()) {
            setPlaySpeed(prevPlaySpeed);
            setForward(false);
        }
    }

    private void stop() {
        try {
            out.close();
            flightGear.close();
            connectionIsClose = true;
        }
        catch (IOException e) { }
    }
}
