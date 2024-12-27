package com.demeth.massaudioplayer.backend.adapters

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer

import com.demeth.massaudioplayer.backend.models.adapters.AudioPlayer
import com.demeth.massaudioplayer.backend.models.adapters.Database
import com.demeth.massaudioplayer.backend.models.adapters.EventManager
import com.demeth.massaudioplayer.backend.models.objects.Audio
import com.demeth.massaudioplayer.backend.models.objects.Event
import com.demeth.massaudioplayer.backend.models.objects.EventCodeMap
import com.demeth.massaudioplayer.backend.models.objects.Metadata

import java.io.IOException

class FileAudioPlayer(private val eventManager: EventManager, private val database: Database, private val context: Context) : AudioPlayer {
    private var mp: MediaPlayer = MediaPlayer()

    private var timestampAccessOk = false

    init{
        mp.setAudioAttributes(
                AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC) //music player
                        .setUsage(AudioAttributes.USAGE_MEDIA) //on media
                        .build()
        )

        mp.setOnCompletionListener {
            this.eventManager.trigger(Event(EventCodeMap.EVENT_AUDIO_COMPLETED))
        }
        mp.setOnPreparedListener {
            mp.start()
            timestampAccessOk = true
            this.eventManager.trigger(Event(EventCodeMap.EVENT_AUDIO_START))
        }
    }

    override fun play(audio: Audio) {
        // call prepare async
        //begin playing when prepare finish

        try {
            timestampAccessOk = false
            mp.reset()
            val metadata = database.getMetadata(audio) as Metadata.FileAudioMetadata
            metadata.uri?.let { mp.setDataSource(this.context, it) } //TODO find solution maybe Database
            mp.prepareAsync()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    override fun pause() = mp.pause()

    override fun resume() = mp.start()

    override fun setProgress(progress: Double) {
        mp.seekTo((progress * duration()).toInt())
    }

    override fun progress(): Double {
        // return 0 if unavailable
        return if(timestampAccessOk)
            mp.currentPosition.toDouble()
        else
            0.0
    }

    override fun duration(): Int {
        // return 0 if unavailable
        return if(timestampAccessOk)
            mp.duration
        else
            0
    }

    override fun stop() = mp.stop()
}
