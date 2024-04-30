package com.demeth.massaudioplayer.frontend;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * This ViewModel is responsible for updating the UI elements of the Home page of the application.<br> <br>
 * The methods preceded by <strong>get</strong> (ex: getMethodeName) are meant to be used by the UI elements of the HomeActivity.<br><br>
 * The methode preceded by <strong>set</strong> (ex: setMethodeName) are supposed to be called by the
 * backend system when a value is updated.
 */
public class HomeViewModel extends ViewModel {
    private MutableLiveData<Boolean> randomMode = new MutableLiveData<>();

    public void setRandomModeUI(boolean random_mode){
        randomMode.postValue(random_mode);
    }

    public LiveData<Boolean> getRandomModeUI(){
        return randomMode;
    }

}