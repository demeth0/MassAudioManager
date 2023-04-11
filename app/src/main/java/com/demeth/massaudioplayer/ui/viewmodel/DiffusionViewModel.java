package com.demeth.massaudioplayer.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.demeth.massaudioplayer.audio_player.AudioPlayer;
import com.demeth.massaudioplayer.database.IdentifiedEntry;

public class DiffusionViewModel extends ViewModel {
    public static class Timestamp{
        public int duration=0;
        public int current=0;

        public Timestamp setCurrent(int current){
            this.current=current;
            return this;
        }

        public Timestamp setDuration(int duration){
            this.duration=duration;
            return this;
        }
    }

    /*music diffusion current timestamps*/
    private final MutableLiveData<Timestamp> timestamp = new MutableLiveData<>(new Timestamp());

    private final MutableLiveData<IdentifiedEntry> entry = new MutableLiveData<>();

    private final MutableLiveData<AudioPlayer.LoopMode> loopMode = new MutableLiveData<>(AudioPlayer.LoopMode.NONE);
    private final MutableLiveData<Boolean> randomMode = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> paused = new MutableLiveData<>(true);

    @SuppressWarnings("ConstantConditions")
    public void setCurrentTime(int current){
        timestamp.postValue(timestamp.getValue().setCurrent(current));
    }

    @SuppressWarnings("ConstantConditions")
    public void setTime(int current,int duration){
        timestamp.setValue(timestamp.getValue().setCurrent(current).setDuration(duration));
    }

    public void setEntry(IdentifiedEntry entry) {
        this.entry.setValue(entry);
    }

    public LiveData<IdentifiedEntry> getEntry() {
        return entry;
    }

    public LiveData<Timestamp> getTimestamp(){
        return timestamp;
    }

    public LiveData<Boolean> getRandomMode() {
        return randomMode;
    }

    public LiveData<AudioPlayer.LoopMode> getLoopMode() {
        return loopMode;
    }

    public LiveData<Boolean> getPaused() {
        return paused;
    }

    public void setPaused(boolean mode){
        paused.setValue(mode);
    }

    public void setLoopMode(AudioPlayer.LoopMode mode){
        loopMode.setValue(mode);
    }

    public void setRandomMode(boolean mode){
        randomMode.setValue(mode);
    }
}
