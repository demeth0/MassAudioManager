package com.demeth.massaudioplayer.backend.models.adapters;

import com.demeth.massaudioplayer.backend.models.objects.Audio;
import com.demeth.massaudioplayer.backend.models.objects.LoopMode;
import com.demeth.massaudioplayer.backend.models.objects.Playlist;

import java.util.List;

/**
 * determine the audio to play and manage the list of audios
 */
public interface AudioProvider {
    /*Queue get_queue(); //TODO may be removed for better coding practices
    Playlist get_playlist();*/

    /**
     * Clear any actual playlist and replace it with the new given playlist.
     * @param p The new playlist to use for the audio diffusion.
     */
    void set_playlist(Playlist p);

    /**
     * The random mode shuffle the playlist play order but do not affect the queue.
     * @param mode The new value of the random mode.
     */
    void set_random(boolean mode);

    /**
     * The random mode shuffle the playlist play order but do not affect the queue.
     * @return The current value of the random mode.
     */
    boolean get_random();

    /**
     * Set the new loop option for audio reading.
     * @param mode Loop mode to set.
     */
    void set_loop(LoopMode mode);

    /**
     *
     * @return The current state of the loop option.
     */
    LoopMode get_loop();

    /**
     * Add an audio in the queue.
     * @param audio Audio to add to the queue.
     */
    void add_to_queue(Audio audio);

    /**
     * Add a list of audio to the playlist.
     * @param audios The list of audio to add.
     */
    void add_to_playlist(List<Audio> audios);

    /**
     * @return The queue audio content.
     */
    List<Audio> view_queue();

    /**
     * @return The playlist audio content.
     */
    List<Audio> view_playlist();

    /**
     * @return The currently selected audio.
     */
    Audio get_audio();

    /**
     * Use the index to select the next audio to play from the queue.
     * @param audio_index The index in the queue audio list.
     */
    void set_audio_from_queue(int audio_index);

    /**
     * Use the index to select the next audio to play from the playlist.
     * @param audio_index The index in the playlist audio list.
     */
    void set_audio_from_playlist(int audio_index);

    /**
     * When user skip an audio.
     */
    void move_to_next();

    /**
     * when an audio is completed and need to load the next.
     */
    void advance_to_next();
    void move_to_prev();

    /**
     * Clear the queue of any pending audio to play.
     */
    void clear_queue();
}
