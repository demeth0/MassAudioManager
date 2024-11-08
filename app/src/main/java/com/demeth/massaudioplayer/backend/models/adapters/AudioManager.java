package com.demeth.massaudioplayer.backend.models.adapters;

import com.demeth.massaudioplayer.backend.models.objects.Timestamp;

/**
 * Manage a list of playable audios. can change the mode of ordering with loop options or shuffling. Will play the audio in chain.
 */
public interface AudioManager {

    /**
     * @return The time progress of the audio playing
     */
    Timestamp timestamp();

    /**
     * Set the progress of the current audio playing. A value of 1.0d mean the end of the audio and 0 the beginning.
     * @param progress as a scalar between 0 and 1. Undefined behavior for other values.
     */
    void setTimestampProgress(double progress);

    /**
     * Play the list of audio loaded with audio provider.
     */
    void play();

    /**
     * Pause the list of audio playing.
     */
    void pause();

    /**
     *
     * @return true if paused
     */
    boolean isPaused();

    /**
     * PLay the previous audio in the play list or restart from the begining if the audio file have played for more than 4 seconds or if it's the first audio in the play list.
     */
    void play_previous();

    /**
     * Play the next audio in the playlist or restart from the begining of the play list.
     */
    void play_next();
}
