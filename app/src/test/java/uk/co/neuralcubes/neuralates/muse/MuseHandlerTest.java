package uk.co.neuralcubes.neuralates.muse;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.interaxon.libmuse.Accelerometer;
import com.interaxon.libmuse.Battery;
import com.interaxon.libmuse.ConnectionState;
import com.interaxon.libmuse.Eeg;
import com.interaxon.libmuse.Muse;
import com.interaxon.libmuse.MuseConnectionPacket;
import com.interaxon.libmuse.MuseDataPacket;
import com.interaxon.libmuse.MuseDataPacketType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * Created by javi on 10/04/16.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({MuseConnectionPacket.class,MuseDataPacket.class})
public class MuseHandlerTest {
    @Mock
    Muse muse;
    private EventBus eventBus;
    private MuseHandler museHandler;

    @Before
    public void setUp(){
        this.eventBus = new EventBus();
        this.museHandler = new MuseHandler(this.muse,this.eventBus);
    }


    /**
     * Verify that we connect to the muse hardware in an
     * asynchronous fashion
     */
    @Test
    public void testConnect(){
        this.museHandler.connect();
        Mockito.verify(this.muse).runAsynchronously();
    }

    @Test
    public void testSetConnectionListener() throws Exception {
        class StatusHolder{
            ConnectionState state;
            @Subscribe public void updateState(ConnectionState state){
                this.state=state;
            }
        }
        this.museHandler.setConnectionListener();
        StatusHolder holder = new StatusHolder();
        this.eventBus.register(holder);
        MuseConnectionPacket packet = PowerMockito.mock(MuseConnectionPacket.class);

        Mockito.when(packet.getCurrentConnectionState()).thenReturn(ConnectionState.CONNECTED);
        this.museHandler.getMuseConnectionListener().receiveMuseConnectionPacket(packet);
        assertEquals("The status gets propagated",holder.state,ConnectionState.CONNECTED);

        Mockito.when(packet.getCurrentConnectionState()).thenReturn(ConnectionState.CONNECTING);
        this.museHandler.getMuseConnectionListener().receiveMuseConnectionPacket(packet);
        assertEquals("The status gets propagated",holder.state,ConnectionState.CONNECTING);

        Mockito.when(packet.getCurrentConnectionState()).thenReturn(ConnectionState.DISCONNECTED);
        this.museHandler.getMuseConnectionListener().receiveMuseConnectionPacket(packet);
        assertEquals("The status gets propagated",holder.state,ConnectionState.DISCONNECTED);

    }

    @Test
    public void testSetBatteryListener() throws Exception {
        class LevelHolder{
            double level;
            @Subscribe public void updateState(MuseHandler.BatteryReading reading){
                this.level=reading.getLevel();
            }
        }
        this.museHandler.setBatteryListener();
        LevelHolder holder = new LevelHolder();
        this.eventBus.register(holder);
        MuseDataPacket packet = PowerMockito.mock(MuseDataPacket.class);



        ArrayList<Double> values = new ArrayList<>(20);
        //add values to match sizes so we're sure the value to add fits
        for (Battery b:Battery.values()){
            values.add(0.);
        }
        values.set(Battery.CHARGE_PERCENTAGE_REMAINING.ordinal(), 40.5);

        Mockito.when(packet.getPacketType()).thenReturn(MuseDataPacketType.BATTERY);
        Mockito.when(packet.getValues()).thenReturn(values);

        this.museHandler.getBatteryListener().receiveMuseDataPacket(packet);
        assertEquals("The battery is propagated",40.5,holder.level,0.0);


    }

    @Test
    public void testHorseshoeReading(){
        class HorseshoeHolder{
            MuseHandler.HorseshoeReading reading;
            @Subscribe public void set(MuseHandler.HorseshoeReading reading){
                this.reading = reading;
            }
        }
        HorseshoeHolder holder = new HorseshoeHolder();
        this.eventBus.register(holder);
        this.museHandler.setHorseshoeListener();
        MuseDataPacket packet = PowerMockito.mock(MuseDataPacket.class);

        ArrayList<Double> values = new ArrayList<>(4);
        //add values to match sizes so we're sure the value to add fits
        for (Eeg eeg:Eeg.values()){
            values.add(0.);
        }

        values.set(Eeg.TP9.ordinal(),4.);
        values.set(Eeg.FP1.ordinal(),3.);
        values.set(Eeg.FP2.ordinal(),2.);
        values.set(Eeg.TP10.ordinal(),1.);

        //mock calls
        Mockito.when(packet.getPacketType()).thenReturn(MuseDataPacketType.HORSESHOE);
        Mockito.when(packet.getValues()).thenReturn(values);

        this.museHandler.getHorseshoeListener().receiveMuseDataPacket(packet);
        assertEquals("Normalised Horseshoe value for tp9", holder.reading.getValues()[0], 0, 0);
        assertEquals("Normalised Horseshoe value for fp1", holder.reading.getValues()[1], 1/4., 0);
        assertEquals("Normalised Horseshoe value for fp2", holder.reading.getValues()[2], 2/4., 0);
        assertEquals("Normalised Horseshoe value for tp10", holder.reading.getValues()[3], 3/4., 0);



    }

    @Test
    public void testAccelerometerListener(){
        class AccelerometerHolder{
            MuseHandler.AccelerometerReading reading;
            @Subscribe public void set(MuseHandler.AccelerometerReading reading){
                this.reading = reading;
            }
        }
        AccelerometerHolder holder = new AccelerometerHolder();

        this.eventBus.register(holder);
        this.museHandler.setAccelerometerListener();
        MuseDataPacket packet = PowerMockito.mock(MuseDataPacket.class);

        ArrayList<Double> values = new ArrayList<>(3);
        //add values to match sizes so we're sure the value to add fits
        for (Accelerometer acc: Accelerometer.values()){
            values.add(0.);
        }

        values.set(Accelerometer.FORWARD_BACKWARD.ordinal(),1.0);
        values.set(Accelerometer.LEFT_RIGHT.ordinal(),2.0);
        values.set(Accelerometer.UP_DOWN.ordinal(),3.0);


        //mock calls
        Mockito.when(packet.getPacketType()).thenReturn(MuseDataPacketType.ACCELEROMETER);
        Mockito.when(packet.getValues()).thenReturn(values);

        this.museHandler.getAccelerometerListener().receiveMuseDataPacket(packet);
        assertEquals("Pitch is ok", holder.reading.getX(),1.,0.);
        assertEquals("Roll is ok", holder.reading.getY(), 2., 0.);
        assertEquals("Yaw is ok", holder.reading.getZ(), 3., 0);

    }


    @Test
    public void testSetFocusListener(){
        class FocusHolder{
            double focus;
            @Subscribe public void set(MuseHandler.FocusReading reading){
                this.focus = reading.getFocus();
            }
        }
        FocusHolder holder = new FocusHolder();

        this.eventBus.register(holder);
        this.museHandler.setFocusListener();
        MuseDataPacket packet = PowerMockito.mock(MuseDataPacket.class);

        ArrayList<Double> values = new ArrayList<>(1);
        //add values to match sizes so we're sure the value to add fits
        values.add(1.5);


        //mock calls
        Mockito.when(packet.getPacketType()).thenReturn(MuseDataPacketType.CONCENTRATION);
        Mockito.when(packet.getValues()).thenReturn(values);

        this.museHandler.getFocusListener().receiveMuseDataPacket(packet);
        assertEquals("The focus", holder.focus,1.5,0.);

    }
}