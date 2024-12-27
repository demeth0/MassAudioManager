package com.demeth.massaudioplayer.frontend.service;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.demeth.massaudioplayer.backend.models.objects.Audio;
import com.demeth.massaudioplayer.backend.models.objects.LoopMode;
import com.demeth.massaudioplayer.backend.models.objects.Timestamp;

import java.util.List;

public class AudioViewModel extends ViewModel {
    final MutableLiveData<LoopMode> loop_mode = new MutableLiveData<>();
    final MutableLiveData<Boolean> random_mode = new MutableLiveData<>();
    final MutableLiveData<Boolean> paused = new MutableLiveData<>();

    final MutableLiveData<List<Audio>> playlist = new MutableLiveData<List<Audio>>();
    final MutableLiveData<List<Audio>> queue = new MutableLiveData<List<Audio>>();
    final MutableLiveData<List<Audio>> list_all_audios = new MutableLiveData<List<Audio>>();


    final MutableLiveData<Timestamp> audio_timestamp = new MutableLiveData<>();
    final MutableLiveData<Audio> current_audio = new MutableLiveData<>();


    public LiveData<LoopMode> get_loop_mode(){
        return loop_mode;
    }

    public LiveData<Boolean> get_random_mode(){
        return random_mode;
    }

    public LiveData<Boolean> get_paused(){
        return paused;
    }

    public LiveData<List<Audio>> get_playlist(){
        return playlist;
    }

    public LiveData<List<Audio>> get_queue(){
        return queue;
    }

    public LiveData<List<Audio>> get_list_all_audios(){
        return list_all_audios;
    }

    public LiveData<Timestamp> get_audio_timestamp(){
        return audio_timestamp;
    }

    public LiveData<Audio> get_current_audio(){
        return current_audio;
    }
}
