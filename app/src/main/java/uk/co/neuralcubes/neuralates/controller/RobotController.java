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

    private static final double MAX_THRUST = 0.1; //valid values from 0 to 1
    private double mConcentration = 0.0;
    private boolean mOverrideFocus = false;

    public RobotController(@NonNull ConvenienceRobot robot ,@NonNull EventBus bus) {
        mRobot = robot;
        mBus = bus;
        mBus.register(this);
    }

    public void unlink(){
        mBus.unregister(this);
    }

    @Subscribe
    public synchronized void updateAcelerometer(MuseHandler.AccelerometerReading reading) {
        mRobot.drive(computeAngle(reading.getX(),reading.getY()),getThrust());
    }

    float getThrust(){
            return (float)( mConcentration * MAX_THRUST);
    }

    @Subscribe
    public synchronized void updateConcentration(MuseHandler.FocusReading reading) {
        mConcentration = reading.getFocus();
    }

    static float computeAngle (double x,double y){
        //let's imaging that x and y are the elemenbts 'a' and 'b' of a complex number
        // then our complex number is c = x + yi
        // then angle in radians of the accelerometer vector is defined by
        // theta =tan^-1(b/a)


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

    public void toggleOverrideFocus() {
        mOverrideFocus = !mOverrideFocus;
    }

    public synchronized void setBaseReading() {
        //TODO jmanart assign the latest read.
    }
}
