package com.demeth.massaudioplayer.backend.models.adapters

import com.demeth.massaudioplayer.backend.models.objects.Audio

interface AudioPlayer {
    /**
     * Start audio listening, Replace currently playing or reset to beginning.
     * @param audio - audio to play
     */
    fun play(audio: Audio)

    /**
     * Pause listening.
     */
    fun pause()

    /**
     * Resume paused audio.
     */
    fun resume()

    /**
     * Set the current audio time progress.
     * @param progress Value between 0.0 and 1.0.
     */
    fun setProgress(progress : Double)

    /**
     * @return The progress of the audio (1.0 mean finished).
     */
    fun progress(): Double

    /**
     * @return The duration of the audio in seconds.
     */
    fun duration(): Int

    /**
     * Stop the listening.
     */
    fun stop()
}
