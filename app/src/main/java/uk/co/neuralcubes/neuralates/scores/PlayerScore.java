package uk.co.neuralcubes.neuralates.scores;

/**
 * Created by jmanart on 23/04/2016.
 */
public class PlayerScore {
    private double mScore;
    private long mStart, mStop;
    private String mName;

    public double getScore() {
        return mScore;
    }

    public void setScore(double score) {
        mScore = score;
    }

    public void addScore(double more) {
        mScore += more;
    }

    public long getStart() {
        return mStart;
    }

    public void setStart(long start) {
        mStart = start;
    }

    public long getStop() {
        return mStop;
    }

    public void setStop(long stop) {
        mStop = stop;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }
}