package com.demeth.massaudioplayer.frontend.service;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.demeth.massaudioplayer.backend.models.objects.Audio;
import com.demeth.massaudioplayer.backend.models.objects.LoopMode;
import com.demeth.massaudioplayer.backend.models.objects.Timestamp;

class AudioViewModel : ViewModel() {
    private val loop_mode = MutableLiveData<LoopMode>();
    private val random_mode = MutableLiveData<Boolean>();
    private val paused = MutableLiveData<Boolean>();

    private val playlist = MutableLiveData<List<Audio>>();
    private val queue = MutableLiveData<List<Audio>>();
    private val list_all_audios = MutableLiveData<List<Audio>>();


    private val audio_timestamp = MutableLiveData<Timestamp>();
    private val current_audio = MutableLiveData<Audio>();


    fun get_loop_mode():LiveData<LoopMode>{
        return loop_mode;
    }

    fun get_random_mode(): LiveData<Boolean>{
        return random_mode;
    }

    fun get_paused(): LiveData<Boolean>{
        return paused;
    }

    fun get_playlist(): LiveData<List<Audio>>{
        return playlist;
    }

    fun get_queue(): LiveData<List<Audio>>{
        return queue;
    }

    fun get_list_all_audios(): LiveData<List<Audio>>{
        return list_all_audios;
    }

    fun get_audio_timestamp(): LiveData<Timestamp>{
        return audio_timestamp;
    }

    fun get_current_audio(): LiveData<Audio>{
        return current_audio;
    }
}
