package uk.co.neuralcubes.neuralates.sphero;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import com.orbotix.ConvenienceRobot;
import com.orbotix.DualStackDiscoveryAgent;
import com.orbotix.common.DiscoveryException;
import com.orbotix.common.Robot;
import com.orbotix.common.RobotChangedStateListener;
import com.orbotix.le.RobotLE;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by monchote on 14/04/2016.
 */
public class SpheroManager implements RobotChangedStateListener
{
    private static final String TAG = "SpheroManager";

    private Set<Robot> mRobots = new HashSet<>();

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

    public Set<Robot> getRobots()
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

                mRobots.add(robot);

                //Start blinking the robot's LED
                blink(new ConvenienceRobot(robot), false);
                break;
            }
            case Offline:
                Log.d(TAG, "Robot " + robot.getName() + " went offline.");
                mRobots.remove(robot);
                break;
            case Connecting:
            case Connected:
            case Disconnected:
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
}
