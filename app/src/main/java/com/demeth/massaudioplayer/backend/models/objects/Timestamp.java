package com.demeth.massaudioplayer.backend.models.objects;

public class Timestamp {
    private int duration;
    private double progress;
    public Timestamp(int duration, double progress){
        this.duration=duration;
        this.progress=progress;
    }

    public double getProgress() {
        return progress;
    }

    public int getDuration() {
        return duration;
    }
}
