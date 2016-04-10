package uk.co.neuralcubes.neuralates.muse;

import com.google.common.eventbus.EventBus;
import com.interaxon.libmuse.Muse;

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
        private double getX(){
            return 0;
        }
        private double getY(){
            return 0;
        }
        private double getZ(){
            return 0;
        }
    }
    class BatteryReading{
        private double getLevel(){
            return 0;
        }
    }
    class HorseshoeReading{
        /*
         * Returns Tp9,fp1,fp2,tp10
         */
        private double[]getValues(){
            return null;
        }
    }
    private final Muse muse;
    private final EventBus bus;

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
        this.muse.runAsynchronously();
    }
    void setConnectionListener(){

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





}
