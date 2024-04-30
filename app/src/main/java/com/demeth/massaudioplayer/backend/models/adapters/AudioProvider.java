package com.demeth.massaudioplayer.backend.models.adapters;

import com.demeth.massaudioplayer.backend.models.objects.Audio;
import com.demeth.massaudioplayer.backend.models.objects.LoopMode;
import com.demeth.massaudioplayer.backend.models.objects.Playlist;

import java.util.List;

/**
 * determine the audio to play and manage the list of audios
 */
public interface AudioProvider {
    /*Queue get_queue(); //TODO may be removed for better coding habits
    Playlist get_playlist();*/
    void set_playlist(Playlist p);
    void set_random(boolean mode);
    boolean get_random();
    void set_loop(LoopMode mode);
    LoopMode get_loop();
    void add_to_queue(Audio audio);
    void add_to_playlist(List<Audio> audios);

    List<Audio> view_queue();

    List<Audio> view_playlist();

    Audio get_audio();
    void set_audio_from_queue(int audio_index);

    void set_audio_from_playlist(int audio_index);

    /**
     * When user skip an audio
     */
    void move_to_next();

    /**
     * when an audio is completed and need to load the next
     */
    void advance_to_next();
    void move_to_prev();
}
