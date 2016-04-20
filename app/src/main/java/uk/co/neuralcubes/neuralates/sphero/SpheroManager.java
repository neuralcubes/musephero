package uk.co.neuralcubes.neuralates.sphero;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.common.collect.Lists;
import com.orbotix.ConvenienceRobot;
import com.orbotix.DualStackDiscoveryAgent;
import com.orbotix.command.GetPowerStateCommand;
import com.orbotix.command.GetPowerStateResponse;
import com.orbotix.common.DiscoveryException;
import com.orbotix.common.ResponseListener;
import com.orbotix.common.Robot;
import com.orbotix.common.RobotChangedStateListener;
import com.orbotix.common.internal.AsyncMessage;
import com.orbotix.common.internal.DeviceResponse;
import com.orbotix.le.RobotLE;

import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by monchote on 14/04/2016.
 */
public class SpheroManager implements RobotChangedStateListener, ResponseListener
{
    private static final String TAG = "SpheroManager";

    private static final Integer SPHERO_BATTERY_CHECK_INTERVAL_MILLIS = 15000;

    private List<Robot> mRobots = Lists.newArrayList();

    private List<SpheroEventListener> mEventListeners = Lists.newLinkedList();

    private static SpheroManager sInstance;

    private SpheroManager()
    {
        DualStackDiscoveryAgent.getInstance().addRobotStateListener(this);
    }

    private HashMap<Robot, Timer> mBatteryCheckTimers = new HashMap<>();

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

    public synchronized void addRobotSetListener(SpheroEventListener listener)
    {
        this.mEventListeners.add(listener);
    }

    public synchronized void notifyRobotSetListeners()
    {
        for (SpheroEventListener l: this.mEventListeners) {
            l.updateRobots(mRobots);
        }
    }

    @Override
    public void handleRobotChangedState(Robot robot, RobotChangedStateNotificationType robotChangedStateNotificationType)
    {
        final String robotName = robot != null ? robot.getName() : "";
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

                mRobots.add(robot);
                robot.addResponseListener(this);
                mBatteryCheckTimers.put(robot, scheduleSpheroBatteryCheckTimer(robot));
                notifyRobotSetListeners();
                blink(new ConvenienceRobot(robot),true);
                showTail(new ConvenienceRobot(robot));

                Log.d(TAG, "Robot " + robotName + " is now online. Address: " + robot.getAddress());
                break;
            }
            case Disconnected:
                Log.d(TAG, "Robot " + robotName + " disconnected.");
                mRobots.remove(robot);
                robot.removeResponseListener(this);
                Timer timer = mBatteryCheckTimers.remove(robot);
                if (timer != null) {
                    timer.cancel();
                }
                break;
            case Connecting:
                Log.d(TAG, "Robot " + robotName + " is connecting.");
                break;
            case Connected:
                Log.d(TAG, "Robot " + robotName + " is connected.");
                break;
            case Offline:
                Log.d(TAG, "Robot " + robotName + " went offline.");
                break;
            case FailedConnect:
                Log.d(TAG, "Robot " + robotName + " failed to connect.");
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

    private void showTail(@NonNull final ConvenienceRobot robot)
    {
        robot.setBackLedBrightness(1.0f);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                robot.calibrating(false);
            }
        }, 2000);
    }

    private Timer scheduleSpheroBatteryCheckTimer(final Robot robot) {
        Timer batteryTimer = new Timer();
        batteryTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Log.d(TAG, "Issuing Sphero battery state command");
                robot.sendCommand(new GetPowerStateCommand());
            }
        }, 0, SPHERO_BATTERY_CHECK_INTERVAL_MILLIS);
        return batteryTimer;
    }

    // BEGINNING - ResponseListener

    @Override
    public void handleResponse(DeviceResponse deviceResponse, Robot robot) {
        Log.d(TAG, "handleResponse" + deviceResponse.toString());
        if (deviceResponse instanceof GetPowerStateResponse) {
            for (SpheroEventListener listener : mEventListeners) {
                listener.onPowerStateUpdate(robot, ((GetPowerStateResponse)deviceResponse).getPowerState());
            }
        }
    }

    @Override
    public void handleStringResponse(String s, Robot robot) {
        Log.d(TAG, "handleStringResponse" + s.toString());
    }

    @Override
    public void handleAsyncMessage(AsyncMessage asyncMessage, Robot robot) {
        Log.d(TAG, "handleAsyncMessage" + asyncMessage.toString());
    }

    // END - ResponseListener
}
