package com.demeth.massaudioplayer.ui.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.demeth.massaudioplayer.database.IdentifiedEntry;

public class DiffusionViewModel extends ViewModel {
    public class Timestamp{
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


    private MutableLiveData<IdentifiedEntry> entry = new MutableLiveData<>();

    @SuppressWarnings("ConstantConditions")
    public void setCurrentTime(int current){
        timestamp.setValue(timestamp.getValue().setCurrent(current));
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
}
