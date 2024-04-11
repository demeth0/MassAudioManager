package com.demeth.massaudioplayer.frontend.service;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;

public class GuiUpdater implements ViewModelStoreOwner {
    private AudioViewModel audio_view_model=null;
    @NonNull
    @Override
    public ViewModelStore getViewModelStore() {
        return null;
    }
    public GuiUpdater(){

    }

    public void set_view_model(AudioViewModel view_model){
        audio_view_model = view_model;
    }
}
