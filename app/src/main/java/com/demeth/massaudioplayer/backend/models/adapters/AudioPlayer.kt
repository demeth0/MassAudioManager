package com.demeth.massaudioplayer.backend.models.adapters;

import com.demeth.massaudioplayer.backend.models.objects.Audio;

public interface AudioPlayer {
    /**
     * Start audio listening, Replace currently playing or reset to beginning.
     * @param audio - audio to play
     */
    void play(Audio audio);

    /**
     * Pause listening.
     */
    void pause();

    /**
     * Resume paused audio.
     */
    void resume();

    /**
     * Set the current audio time progress.
     * @param progress Value between 0.0 and 1.0.
     */
    void set_progress(double progress);

    /**
     * @return The progress of the audio (1.0 mean finished).
     */
    double progress();

    /**
     * @return The duration of the audio in seconds.
     */
    int duration();

    /**
     * Stop the listening.
     */
    void stop();
}
