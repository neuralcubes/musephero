package uk.co.neuralcubes.neuralates.sphero;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.common.collect.Lists;
import com.orbotix.ConvenienceRobot;
import com.orbotix.DualStackDiscoveryAgent;
import com.orbotix.common.DiscoveryException;
import com.orbotix.common.Robot;
import com.orbotix.common.RobotChangedStateListener;
import com.orbotix.le.RobotLE;

import java.util.List;

/**
 * Created by monchote on 14/04/2016.
 */
public class SpheroManager implements RobotChangedStateListener
{
    private static final String TAG = "SpheroManager";

    private List<Robot> mRobots = Lists.newArrayList();

    private List<RobotSetListener> robotSetListeners = Lists.newLinkedList();

    private static SpheroManager sInstance;

    private SpheroManager()
    {
        DualStackDiscoveryAgent.getInstance().addRobotStateListener(this);
    }

    public static synchronized SpheroManager getInstance()
    {
        if (sInstance == null) {
            sInstance = new SpheroManager();
        }
        return sInstance;
    }

    public List<Robot> getRobots()
    {
        return mRobots;
    }

    public void startDiscovery(@NonNull Context context)
    {
        //If the DiscoveryAgent is not already looking for robots, start discovery.
        if(!DualStackDiscoveryAgent.getInstance().isDiscovering()) {
            try {
                DualStackDiscoveryAgent.getInstance().startDiscovery(context);
                Log.d(TAG, "Start discovery");
            } catch (DiscoveryException e) {
                Log.e(TAG, "DiscoveryException: " + e.getMessage());
            }
        }
    }

    public void stopDiscovery()
    {
        if(DualStackDiscoveryAgent.getInstance().isDiscovering()) {
            DualStackDiscoveryAgent.getInstance().stopDiscovery();
        }
    }
    public synchronized void addRobotSetListener(RobotSetListener listener){
        this.robotSetListeners.add(listener);
    }
    public synchronized void notifyRobotSetListeners(){
        for (RobotSetListener l: this.robotSetListeners){
            l.updateRobots(this.mRobots);
        }
    }
    @Override
    public void handleRobotChangedState(Robot robot, RobotChangedStateNotificationType robotChangedStateNotificationType)
    {
        switch(robotChangedStateNotificationType) {
            case Online: {
                if (robot == null) {
                    break;
                }
                //If robot uses Bluetooth LE, Developer Mode can be turned on.
                //This turns off DOS protection. This generally isn't required.
                if(robot instanceof RobotLE) {
                    ((RobotLE)robot).setDeveloperMode(true);
                }

                Log.d(TAG, "Robot " + robot.getName() + " is now online. Address: " + robot.getAddress());
                break;
            }
            case Offline:
                Log.d(TAG, "Robot " + robot.getName() + " went offline.");
                mRobots.remove(robot);
                break;
            case Connecting:
                Log.d(TAG, "Robot " + robot.getName() + " is connecting.");

                break;
            case Connected:
                mRobots.add(robot);
                //Start blinking the robot's LED
                this.notifyRobotSetListeners();
                Log.d(TAG, "Robot " + robot.getName() + " is connected.");
                this.blink(new ConvenienceRobot(robot),true);
                this.showTail(new ConvenienceRobot(robot));
                break;
            case Disconnected:
                break;
            case FailedConnect:
                break;
        }
    }

    //Turn the robot LED on or off every two seconds
    private void blink(@NonNull final ConvenienceRobot convenienceRobot, final boolean lit )
    {
        if(lit) {
            convenienceRobot.setLed( 0.0f, 0.0f, 0.0f );
        } else {
            convenienceRobot.setLed( 0.0f, 0.0f, 1.0f );
        }

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                blink(convenienceRobot, !lit);
            }
        }, 2000);
    }

    private void showTail(@NonNull final ConvenienceRobot robot){
        robot.setBackLedBrightness(1.0f);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                robot.calibrating(false);
            }
        }, 2000);

    }

}
