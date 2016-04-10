package uk.co.neuralcubes.neuralates.muse;

import com.google.common.eventbus.EventBus;
import com.interaxon.libmuse.Accelerometer;
import com.interaxon.libmuse.Battery;
import com.interaxon.libmuse.Eeg;
import com.interaxon.libmuse.Muse;
import com.interaxon.libmuse.MuseArtifactPacket;
import com.interaxon.libmuse.MuseConnectionListener;
import com.interaxon.libmuse.MuseConnectionPacket;
import com.interaxon.libmuse.MuseDataListener;
import com.interaxon.libmuse.MuseDataPacket;
import com.interaxon.libmuse.MuseDataPacketType;

import java.io.Closeable;

/**
 * Created by javi on 10/04/16.
 */

/**
 * This class connects to a paired muse and emits a series of events to the event bus to inform
 * of the changed states
 *
 * This events are:
 *
 *  <ul>
 *     <li> com.interaxon.libmuse.ConnectionState </li>
 *     <li> com.interaxon.libmuse.Muse </li>
 *     <li> uk.co.neuralcubes.MuseHandler.AccelerometerReading </li>
 *     <li> uk.co.neuralcubes.MuseHandler.FocusLevel </li>
 *     <li> uk.co.neuralcubes.MuseHandler.HorseshoeReading </li>
 *  </ul>
 *
 *
 */
public class MuseHandler implements Closeable {
    class AccelerometerReading {

        private double x,y,z;

        public AccelerometerReading(double x, double y, double z) {
            this.z = z;
            this.y = y;
            this.x = x;
        }


        double getX(){
            return x;
        }
        double getY(){
            return y;
        }
        double getZ(){
            return z;
        }
    }

    class BatteryReading{
        double value = 0;

        public BatteryReading(double value) {
            this.value = value;
        }
        public double getLevel(){
            return this.value;
        }
    }
    class HorseshoeReading{
        private double values[];
        public HorseshoeReading(double[] values) {
            this.values = values;
        }

        /*
         * Returns Tp9,fp1,fp2,tp10
         */
        public double[]getValues(){
            return values;
        }
    }
    private final Muse muse;
    private final EventBus bus;


    /**
     * Private listeners
     */
    private MuseConnectionListener connListener;
    private MuseDataListener batteryListener;
    private MuseDataListener horseshoeListener;

    /**
     * Connects to a paired muse identified by it index
     * and start sending event to the event bus
     * @param museIndex the index returned by Muse.getPairedMuses()
     * @param bus
     * @return A MuseHandler
     */
    public static MuseHandler connectTo(int museIndex, EventBus bus){
        return null;
    }
    public MuseHandler(Muse muse, EventBus bus) {
        this.muse = muse;
        this.bus = bus;
    }

    void connect(){
        this.setConnectionListener();
        this.setBatteryListener();
        this.muse.runAsynchronously();
    }
    void setConnectionListener(){
        this.connListener= new MuseConnectionListener() {

            @Override
            public void receiveMuseConnectionPacket(MuseConnectionPacket museConnectionPacket) {
                MuseHandler.this.bus.post(museConnectionPacket.getCurrentConnectionState());
            }
        };
        this.muse.registerConnectionListener(this.connListener);
    }
    void setBatteryListener(){
       this.batteryListener = new MuseDataListener() {
            @Override
            public void receiveMuseDataPacket(MuseDataPacket museDataPacket) {
                if (museDataPacket.getPacketType() == MuseDataPacketType.BATTERY) {
                    double value = museDataPacket.getValues().get(Battery.CHARGE_PERCENTAGE_REMAINING.ordinal());
                    MuseHandler.this.bus.post(new BatteryReading(value));
                }
            }

            @Override
            public void receiveMuseArtifactPacket(MuseArtifactPacket museArtifactPacket) {

            }
        };
        this.muse.registerDataListener(this.batteryListener,MuseDataPacketType.BATTERY);
    }

    void setHorseshoeListener(){
        this.horseshoeListener =  new MuseDataListener() {
            private double normalise(double val){
                //because 4 is the lowest quality, why not?!
                return (4.-val)/4.;
            }
            @Override
            public void receiveMuseDataPacket(MuseDataPacket museDataPacket) {
                if (museDataPacket.getPacketType() == MuseDataPacketType.HORSESHOE) {
                    double tp9= normalise(museDataPacket.getValues().get(Eeg.TP9.ordinal()));
                    double fp1= normalise(museDataPacket.getValues().get(Eeg.FP1.ordinal()));
                    double fp2= normalise(museDataPacket.getValues().get(Eeg.FP2.ordinal()));
                    double tp10= normalise(museDataPacket.getValues().get(Eeg.TP10.ordinal()));
                    MuseHandler.this.bus.post(new HorseshoeReading(new double[]{tp9,fp1,fp2,tp10}));
                }
            }

            @Override
            public void receiveMuseArtifactPacket(MuseArtifactPacket museArtifactPacket) {

            }
        };
        this.muse.registerDataListener(this.horseshoeListener,MuseDataPacketType.HORSESHOE);
    }
    public void setAccelerometerListener() {
        this.accelerometerListener =new MuseDataListener() {

            @Override
            public void receiveMuseDataPacket(MuseDataPacket museDataPacket) {
                if (museDataPacket.getPacketType() == MuseDataPacketType.ACCELEROMETER) {
                    MuseHandler.this.bus.post(new AccelerometerReading(
                            museDataPacket.getValues().get(Accelerometer.FORWARD_BACKWARD.ordinal()),
                            museDataPacket.getValues().get(Accelerometer.LEFT_RIGHT.ordinal()),
                            museDataPacket.getValues().get(Accelerometer.UP_DOWN.ordinal())
                    ));
                }
            }
            @Override
            public void receiveMuseArtifactPacket(MuseArtifactPacket museArtifactPacket) {

            }
        };
        this.muse.registerDataListener(this.accelerometerListener,MuseDataPacketType.ACCELEROMETER);
    }

    /**
     * Resets all the focus related computed states
     * call this method when changing muse users
     */
    public void resetFocusState(){

    }

    @Override
    public void close(){

    }

    MuseConnectionListener getMuseConnectionListener() {
        return connListener;
    }
    
    MuseDataListener getBatteryListener() {
        return batteryListener;
    }
    MuseDataListener getHorseshoeListener() {
        return horseshoeListener;
    }
    MuseDataListener getAccelerometerListener() {
        return accelerometerListener;
    }

}
