package com.demeth.massaudioplayer.backend.models.objects;

import android.util.Log;

/**
 * Timestamp is an object structure that represent a progression of an audio track. The total duration is
 * saved in seconds and the current progress of the track is represented as a double precision integer between 0 and 1. <br><br>
 * This model is only used to output value and the fields of this object are not to be modified.
 */
public class Timestamp {
    private int duration;
    private double progress;

    /**
     * Create a timestamp object to pass time progression information about a specific audio track.
     * @param duration Total track duration in seconds.
     * @param progress Value between 0 and 1.
     */
    public Timestamp(int duration, double progress){
        this.duration=duration;
        this.progress=progress;
    }

    /**
     * @return The current progress of the track between 0 and 1.
     */
    public double getProgress() {
        return progress;
    }

    /**
     * @return The maximum duration in seconds.
     */
    public int getDuration() {
        return duration;
    }
}
