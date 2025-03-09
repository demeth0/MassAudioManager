package com.demeth.massaudioplayer.frontend.service

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import com.demeth0.massaudioplayer.backend.models.objects.Audio
import com.demeth0.massaudioplayer.backend.models.objects.LoopMode
import com.demeth0.massaudioplayer.backend.models.objects.Timestamp

class AudioViewModel : ViewModel() {
    private val loopMode = MutableLiveData<LoopMode>()
    private val randomMode = MutableLiveData<Boolean>()
    private val paused = MutableLiveData<Boolean>()

    private val playlist = MutableLiveData<List<Audio>>()
    private val queue = MutableLiveData<List<Audio>>()
    private val listAllAudios = MutableLiveData<List<Audio>>()


    private val audioTimestamp = MutableLiveData<Timestamp>()
    private val currentAudio = MutableLiveData<Audio>()


    fun getLoopMode():LiveData<LoopMode>{
        return loopMode
    }

    fun getRandomMode(): LiveData<Boolean>{
        return randomMode
    }

    fun getPaused(): LiveData<Boolean>{
        return paused
    }

    fun getPlaylist(): LiveData<List<Audio>>{
        return playlist
    }

    fun getQueue(): LiveData<List<Audio>>{
        return queue
    }

    fun getListAllAudios(): LiveData<List<Audio>>{
        return listAllAudios
    }

    fun getAudioTimestamp(): LiveData<Timestamp>{
        return audioTimestamp
    }

    fun getCurrentAudio(): LiveData<Audio>{
        return currentAudio
    }
}
