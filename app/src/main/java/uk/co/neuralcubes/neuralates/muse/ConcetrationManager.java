package uk.co.neuralcubes.neuralates.muse;

import com.google.common.math.DoubleMath;

import org.apache.commons.collections4.queue.CircularFifoQueue;

/**
 * Created by javi on 18/04/16.
 */
public class ConcetrationManager {
    //The concentration value is send at 10Hz so we 5 values we have a
    //smoothing of half a second
    private static final int MUSE_CONCENTRATION_COOLDOWN=5;
    CircularFifoQueue<Double> mConcentrationBuffer = new CircularFifoQueue<>(MUSE_CONCENTRATION_COOLDOWN);


    public synchronized void addConcentrationValue(double value){
        mConcentrationBuffer.add(value);
    }

    /**
     * Returns the concentration level based on the internal values
     * @return
     */
    public synchronized float getConcentration(){
        return (float) DoubleMath.mean(this.mConcentrationBuffer);
    }

    public synchronized void reset(){
        mConcentrationBuffer.clear();
    }



}
