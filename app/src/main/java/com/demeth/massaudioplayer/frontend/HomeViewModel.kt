package com.demeth.massaudioplayer.frontend

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import com.demeth0.massaudioplayer.backend.models.objects.Audio
import com.demeth0.massaudioplayer.backend.models.objects.LoopMode
import com.demeth0.massaudioplayer.backend.models.objects.Timestamp

/**
 * This ViewModel is responsible for updating the UI elements of the Home page of the application.<br> <br>
 * The methods preceded by <strong>get</strong> (ex: getMethodeName) are meant to be used by the UI elements of the HomeActivity.<br><br>
 * The methode preceded by <strong>set</strong> (ex: setMethodeName) are supposed to be called by the
 * backend system when a value is updated.
 */
class HomeViewModel : ViewModel() {
    private val randomMode = MutableLiveData<Boolean>()

    private val loopMode = MutableLiveData<LoopMode>()

    //This value represent the state of the diffusion only, true if an audio is being diffused and false otherwise. There is no other objectives.
    private val pauseState = MutableLiveData(false)

    private val controllerVisibility = MutableLiveData(false)

    private val category = MutableLiveData("PISTES")

    private val validatedSearchQuery = MutableLiveData("")

    private val currentAudio = MutableLiveData<Audio?>(null)

    private val audioTimestamp = MutableLiveData(Timestamp(0,0.0))

    fun setRandomModeUI(randomMode1: Boolean){
        randomMode.postValue(randomMode1)
    }

    fun getRandomModeUI(): LiveData<Boolean>{
        return randomMode
    }

    fun setLoopModeUI(loopMode1: LoopMode){
        loopMode.postValue(loopMode1)
    }

    fun getLoopModeUI(): LiveData<LoopMode>{
        return loopMode
    }

    /**
     * A value of true mean that an audio is playing. A value of false mean the audio is not playing.
     * @param pauseState1
     */
    fun setPlayPauseStateUI(pauseState1: Boolean){
        pauseState.postValue(pauseState1)
    }

    /**
     * This value represent the state of the diffusion only, true if an audio is being diffused and false otherwise. There is no other objectives.
     *A value of true mean that an audio is playing. A value of false mean the audio is not playing.
     */
    fun getPlayPauseStateUI(): LiveData<Boolean>{
        return pauseState
    }

    fun setControllerVisibility(controllerVisible: Boolean) {
        this.controllerVisibility.postValue(controllerVisible)
    }

    fun getControllerVisibility(): LiveData<Boolean> {
        return controllerVisibility
    }

    fun setAudioSelectionCategory(category: String){
        this.category.postValue(category)
    }

    fun getAudioSelectionCategory(): LiveData<String>{
        return category
    }

    fun setSearchQuery(filter: String){
        validatedSearchQuery.postValue(filter)
    }

    fun getSearchQuery(): LiveData<String>{
        return validatedSearchQuery
    }

    fun setCurrentAudioUI(audio: Audio?){
        currentAudio.postValue(audio)
    }

    fun getCurrentAudioUI(): LiveData<Audio?>{
        return  currentAudio
    }

    fun setAudioTimestamp(stamp: Timestamp){
        audioTimestamp.postValue(stamp)
    }

    fun getAudioTimestamp(): LiveData<Timestamp>{
        return audioTimestamp
    }
}