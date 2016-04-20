package uk.co.neuralcubes.neuralates.sphero;

import com.orbotix.command.GetPowerStateResponse;
import com.orbotix.common.Robot;

import java.util.List;

/**
 * Created by javi on 16/04/16.
 */
public interface SpheroEventListener {
    void updateRobots(List<Robot> robots);

    void onPowerStateUpdate(Robot robot, GetPowerStateResponse.PowerState powerState);
}
