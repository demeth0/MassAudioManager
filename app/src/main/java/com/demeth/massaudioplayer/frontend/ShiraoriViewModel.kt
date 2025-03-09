package com.demeth.massaudioplayer.frontend

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.demeth0.massaudioplayer.backend.IShiraori
import com.demeth0.massaudioplayer.backend.models.objects.Audio
import com.demeth0.massaudioplayer.backend.models.objects.EventCodeMap
import com.demeth0.massaudioplayer.backend.models.objects.LoopMode
import com.demeth0.massaudioplayer.backend.models.objects.Timestamp

class ShiraoriViewModel : ViewModel() {
    private val _serviceTrigger = MutableLiveData(false)
    private val _playPauseState = MutableLiveData(false)
    private val _currentAudio = MutableLiveData<Audio?>(null)
    private val _randomMode = MutableLiveData(false)
    private val _loopMode = MutableLiveData(LoopMode.NONE)
    private val _timestamp = MutableLiveData(Timestamp(0, 0.0))

    private val _audioList = MutableLiveData(emptyList<Audio>())

    val playPauseState: LiveData<Boolean>
        get() = _playPauseState
    val currentAudio: LiveData<Audio?>
        get() = _currentAudio
    val randomMode: LiveData<Boolean>
        get() = _randomMode
    val loopMode: LiveData<LoopMode>
        get() = _loopMode
    val timestamp: LiveData<Timestamp>
        get() = _timestamp
    val serviceTrigger: LiveData<Boolean>
        get() = _serviceTrigger
    val audioList: LiveData<List<Audio>>
        get() = _audioList


    private var shiraori: IShiraori? = null
    fun connectService(service: IShiraori, context: Context) {
        shiraori = service
        shiraori!!.apply {
            _playPauseState.value = isPaused()
            _randomMode.value = isRandomModeEnabled()
            setHandler("main_activity_on_database_reload") {
                when (it.code) {
                    EventCodeMap.EVENT_DATABASE_RELOADED -> {
                        _audioList.value = getDatabaseEntries()
                    }

                    EventCodeMap.EVENT_AUDIO_START -> {
                        _playPauseState.value = false
                        _currentAudio.value = getCurrentAudio()
                    }

                    else -> Unit
                }
            }
            reloadDatabase(context)
        }
        _serviceTrigger.value = true
    }

    fun setPlayState(value: Boolean) {
        shiraori?.apply {
            if (isPaused() != value) pauseAudio()
        }
        _playPauseState.value = value
    }

    fun setCurrentAudio(value: Audio?) {
        _currentAudio.value = value
    }

    fun setRandomMode(mode: Boolean) {
        shiraori?.apply {
            if (isRandomModeEnabled() != mode) setRandomModeEnabled(mode)
        }
        _randomMode.value = mode
    }

    fun playAllAudio() {
        shiraori?.apply {
            setRandomMode(true)
            playInPlaylist(getDatabaseEntries())
        }
        _randomMode.value = true
    }

    fun setLoopMode(loopMode: LoopMode) {
        shiraori?.setLoopMode(loopMode)
        _loopMode.value = loopMode
    }

    fun updateTimestamp() {
        shiraori?.apply {
            _timestamp.postValue(getTimestamp())
        }
    }

    fun reloadAudioList(context: Context) {
        shiraori!!.apply {
            reloadDatabase(context)
        }
    }

    fun skipToNextAudio() {
        shiraori?.skipToNextAudio()

    }

    fun skipToPrevAudio() {
        shiraori?.skipToPreviousAudio()
    }

    fun setTimestamp(ts: Double) {
        shiraori?.setTimestamp(ts)
    }

    fun playAudio(audio: Audio) {
        shiraori?.playAudio(audio)
    }

    fun getWaitingList(): List<Audio>? {
        return shiraori?.run { viewQueue() + viewPlaylist() }
    }
}