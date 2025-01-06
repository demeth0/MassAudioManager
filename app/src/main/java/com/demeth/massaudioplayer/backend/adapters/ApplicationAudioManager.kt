package com.demeth.massaudioplayer.backend.adapters

import android.util.Log
import com.demeth.massaudioplayer.backend.models.adapters.AudioManager
import com.demeth.massaudioplayer.backend.models.adapters.AudioPlayer
import com.demeth.massaudioplayer.backend.models.adapters.AudioPlayerFactory
import com.demeth.massaudioplayer.backend.models.adapters.AudioProvider
import com.demeth.massaudioplayer.backend.models.adapters.EventManager
import com.demeth.massaudioplayer.backend.models.adapters.PlayerNotImplementedException
import com.demeth.massaudioplayer.backend.models.objects.EventCodeMap
import com.demeth.massaudioplayer.backend.models.objects.Timestamp

/**
 * Implement the version of the audio manager for Android applications.
 * Create a audio manager specific for this project implementation.
 * @param audioPlayersFactory The provider that will give correct adapters to read the audio entries.
 * @param eventManager The event manager to react to audio player's events.
 */
class ApplicationAudioManager(private val audioPlayersFactory: AudioPlayerFactory, private val eventManager: EventManager, private val audioProvider: AudioProvider) : AudioManager {
    companion object {
        private const val PAUSED=1
        private const val PLAYING=0
        private const val INACTIVE=3
    }

    // fields
    private var playStatus=INACTIVE

    init{
        eventManager.registerHandler("AudioManager"){ event->
            // handle events
            if(event.code == EventCodeMap.EVENT_AUDIO_COMPLETED){
                audioProvider.advanceToNext()
                if(audioProvider.getAudio()==null){
                    setPlayStatus(INACTIVE)
                }else{
                    play()
                }
            }
        }
    }

    override fun playPrevious(){
        val stamp = this.timestamp()
        if(stamp.duration*stamp.progress>4000){
            setTimestampProgress(0.0)
        }else {
            val player = getAudioPlayer()
            player?.stop()
            setPlayStatus(INACTIVE)
            audioProvider.moveToPrev()
        }
        play()
    }

    override fun playNext(){
        val player = getAudioPlayer()
        player?.stop()
        setPlayStatus(INACTIVE)
        audioProvider.moveToNext()
        play()
    }

    /**
     * @return The audio player compatible with the current audio file. TODO Or crash the app for now.
     */
    private fun getAudioPlayer(): AudioPlayer? {
        val audio = audioProvider.getAudio() ?: return null
        try {
            return this.audioPlayersFactory.provide(audio.type)
        } catch (e: PlayerNotImplementedException) {
            throw RuntimeException(e)
        }
    }

    override fun timestamp(): Timestamp {
        val audioPlayer = getAudioPlayer() ?: return Timestamp(0,0.0)
        return Timestamp(audioPlayer.duration(),audioPlayer.progress()/audioPlayer.duration())
    }

    override fun setTimestampProgress(progress: Double) {
        val audioPlayer = getAudioPlayer()
        audioPlayer?.setProgress(progress)
    }

    override fun play() {
        val audio = audioProvider.getAudio()
        val audioPlayer = getAudioPlayer()

        if(playStatus==PAUSED){
            audioPlayer?.resume()
        }else{
            if(audio == null)
                return
            audioPlayer?.play(audio)
        }
        setPlayStatus(PLAYING) // Event handler will change this value in case of exceptions
    }

    override fun pause() {
        if(playStatus!=PLAYING)
            return

        val audioPlayer = getAudioPlayer() ?: return
        audioPlayer.pause()
        setPlayStatus(PAUSED)
    }

    override fun isPaused() : Boolean {
        return this.playStatus!=PLAYING
    }

    private fun setPlayStatus(status: Int){
        playStatus = status
    }
}
