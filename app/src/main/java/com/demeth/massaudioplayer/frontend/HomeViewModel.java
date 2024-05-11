package com.demeth.massaudioplayer.frontend;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.demeth.massaudioplayer.backend.models.objects.Audio;
import com.demeth.massaudioplayer.backend.models.objects.LoopMode;
import com.demeth.massaudioplayer.backend.models.objects.Timestamp;

/**
 * This ViewModel is responsible for updating the UI elements of the Home page of the application.<br> <br>
 * The methods preceded by <strong>get</strong> (ex: getMethodeName) are meant to be used by the UI elements of the HomeActivity.<br><br>
 * The methode preceded by <strong>set</strong> (ex: setMethodeName) are supposed to be called by the
 * backend system when a value is updated.
 */
public class HomeViewModel extends ViewModel {
    private final MutableLiveData<Boolean> randomMode = new MutableLiveData<>();
    private final MutableLiveData<LoopMode> loopMode = new MutableLiveData<>();

    //This value represent the state of the diffusion only, true if an audio is being diffused and false otherwise. There is no other objectives.
    private final MutableLiveData<Boolean> pauseState = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> controllerVisibility = new MutableLiveData<>(false);

    private final MutableLiveData<String> category = new MutableLiveData<>("PISTES");

    private final MutableLiveData<String> validated_search_query = new MutableLiveData<>("");

    private final MutableLiveData<Audio> current_audio = new MutableLiveData<>(null);

    private final MutableLiveData<Timestamp> audio_timestamp = new MutableLiveData<>(new Timestamp(0,0));

    public void setRandomModeUI(boolean random_mode){
        randomMode.postValue(random_mode);
    }

    public LiveData<Boolean> getRandomModeUI(){
        return randomMode;
    }

    public void setLoopModeUI(LoopMode loop_mode){
        loopMode.postValue(loop_mode);
    }

    public LiveData<LoopMode> getLoopModeUI(){
        return loopMode;
    }

    /**
     * A value of true mean that an audio is playing. A value of false mean the audio is not playing.
     * @param pause_state
     */
    public void setPlayPauseStateUI(boolean pause_state){
        pauseState.postValue(pause_state);
    }
    /**
     * This value represent the state of the diffusion only, true if an audio is being diffused and false otherwise. There is no other objectives.
     *A value of true mean that an audio is playing. A value of false mean the audio is not playing.
     */
    public LiveData<Boolean> getPlayPauseStateUI(){
        return pauseState;
    }

    public void setControllerVisibility(boolean controller_visible) {
        this.controllerVisibility.postValue(controller_visible);
    }

    public LiveData<Boolean> getControllerVisibility() {
        return controllerVisibility;
    }

    public void setAudioSelectionCategory(String category){
        this.category.postValue(category);
    }

    public LiveData<String> getAudioSelectionCategory(){
        return category;
    }

    public void setSearchQuery(String filter){
        validated_search_query.postValue(filter);
    }

    public LiveData<String> getSearchQuery(){
        return validated_search_query;
    }

    public void setCurrentAudioUI(Audio audio){
        current_audio.postValue(audio);
    }

    public LiveData<Audio> getCurrentAudioUI(){
        return  current_audio;
    }

    public void setAudioTimestamp(Timestamp stamp){
        audio_timestamp.postValue(stamp);
    }

    public LiveData<Timestamp> getAudioTimestamp(){
        return audio_timestamp;
    }
}