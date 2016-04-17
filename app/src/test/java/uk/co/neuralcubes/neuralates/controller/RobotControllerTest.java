package uk.co.neuralcubes.neuralates.controller;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by javi on 17/04/16.
 */
public class RobotControllerTest {

    @Test
    public void computeAngle(){
        //having an accelerometer reading of x = 1.0 y= 0.0
        //should give us 0 as angle

        double angle = RobotController.computeAngle(1.0,0.);
        assertEquals("(1,0) is 0 deg",angle,0.,0.);

        angle = RobotController.computeAngle(0,1.);
        assertEquals("(0,1) is 270 deg",angle,90.,0.);
        angle = RobotController.computeAngle(-1,.0);
        assertEquals("(-1,0) is 180 deg",angle,180.,0.);

        angle = RobotController.computeAngle(0,-1.);
        assertEquals("For (0,-1) the angle is 90.",angle,270.,0.);

    }
}