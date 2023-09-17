package com.demeth.massaudioplayer.backend.models.adapters;

import com.demeth.massaudioplayer.backend.models.objects.Audio;
import com.demeth.massaudioplayer.backend.models.objects.Timestamp;

import java.util.List;

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
     * Play the list of audio loaded from the index 0 or resume a paused audio.
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
     *
     * @return get the list of audio loaded
     */
    List<Audio> get();

    /**
     * Set the list of audio to play.
     * @param list
     */
    void set(List<Audio> list);

    Audio current();

    /**
     * Set the shuffling mode
     * @param mode
     */
    void shuffle(boolean mode);

    /**
     *
     * @return If the play list is shuffled.
     */
    boolean isShuffled();

    /**
     * Set the loop mode (between none, single and list)
     * @param mode the mode
     */
    void loop(int mode);
    int getLoopMode();

    /**
     * PLay the previous audio in the play list or restart from the begining if the audio file have played for more than 4 seconds or if it's the first audio in the play list.
     */
    void play_previous();

    /**
     * Play the next audio in the playlist or restart from the begining of the play list.
     */
    void play_next();

    /**
     * Play the audio in the playlist at the position given by the index.
     * @param index The audio to play in the playlist.
     */
    void play(int index);
}
