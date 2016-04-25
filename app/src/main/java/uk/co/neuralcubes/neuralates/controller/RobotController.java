package uk.co.neuralcubes.neuralates.controller;

import android.support.annotation.NonNull;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.orbotix.ConvenienceRobot;

import uk.co.neuralcubes.neuralates.muse.MuseHandler;

/**
 * Created by javi on 17/04/16.
 */
public class RobotController {
    private static final String TAG = "ROBOT_CONTROLLER";
    final private ConvenienceRobot mRobot;
    final private EventBus mBus;
    private ColorMap mColorMap;

    private double mConcentration = 0.;
    private boolean mOverrideFocus = false;
    private double mOverrideValue = 0.;
    private boolean mIsCalibrating = false;
    private boolean mPanicModeEnabled = false;
    private double mMaximumThrust = 0.1;
    private MuseHandler.AccelerometerReading mLastAcceleromterReading = MuseHandler.AccelerometerReading.ZERO;
    private MuseHandler.AccelerometerReading mBaseAcceleromterReading = MuseHandler.AccelerometerReading.ZERO;

    public RobotController(@NonNull ConvenienceRobot robot, @NonNull EventBus bus, @NonNull ColorMap colorMap) {
        mRobot = robot;
        mBus = bus;
        mColorMap = colorMap;
        mBus.register(this);
    }

    public void unlink(){
        mBus.unregister(this);
    }

    @Subscribe
    public synchronized void updateAccelerometer(MuseHandler.AccelerometerReading reading) {
        mLastAcceleromterReading = reading;
        if (isCalibrating() || isPanicModeEnabled()) {
            return;
        }
        double thrust = mOverrideFocus? mOverrideValue : mConcentration;
        if (thrust > 1){
            thrust = 1f;
        }
        int []color = mColorMap.map(thrust);
        mRobot.setLed(color[0]/255.f, color[1]/255.f, color[2]/255.f);
        final double x = reading.getX() - mBaseAcceleromterReading.getX();
        final double y = reading.getY() - mBaseAcceleromterReading.getY();
        mRobot.drive(computeAngle(x, y), (float) (mMaximumThrust * thrust));
    }

    @Subscribe
    public synchronized void updateConcentration(MuseHandler.FocusReading reading) {
        mConcentration = reading.getFocus();
    }

    /**
     * This might come from the ui while the rest of the updates from the muse
     */
    public void setColorMap(ColorMap colorMap) {
        mColorMap = colorMap;
    }

    /**
     * Let's imagine that x and y are the elements 'a' and 'b' of a complex number
     * then our complex number is c = x + yi
     * then angle in radians of the accelerometer vector is defined by
     * theta =tan^-1(b/a)
     */
    static float computeAngle (double x,double y) {
        double theta = Math.atan2(y,x) ;
        //the result from atan2 goes from pi to -pi
        if (theta<0){
            theta=2*Math.PI+theta;
        }
        //now return the value in degrees
        return (float)( (theta * 180 / Math.PI) % 360);
    }

    public void setOverrideFocus(boolean overrideFocus) {
        mOverrideFocus = overrideFocus;
    }

    public void setBaseReading() {
        mBaseAcceleromterReading = mLastAcceleromterReading;
    }

    public void setOverrideValue(double overrideValue) {
        mOverrideValue = overrideValue;
    }

    public void setMaximumTrust(double maximumTrust) {
        mMaximumThrust = maximumTrust;
    }

    public void setCalibrating(boolean isCalibrating) {
        mIsCalibrating = isCalibrating;
    }

    public boolean isCalibrating() {
        return mIsCalibrating;
    }

    public void setPanicModeEnabled(boolean enablePanicMode) {
        mPanicModeEnabled = enablePanicMode;
    }

    public boolean isPanicModeEnabled() {
        return mPanicModeEnabled;
    }
}
