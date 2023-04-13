package com.demeth.massaudioplayer.audio_player;

import androidx.annotation.NonNull;

import com.demeth.massaudioplayer.database.DataType;
import com.demeth.massaudioplayer.database.IdentifiedEntry;

/**
 * base class of any AudioPlayer implementation to manage various type of audio from local to remotely stocked audio accessed using API's (ex: Spotify, Youtube...)
 * @param <T> the type of entry managed
 */
public abstract class AbstractAudioPlayer<T extends IdentifiedEntry> implements Playable{
    final DataType type;
    protected T audio=null;
    protected PlayersManager manager;

    /**
     * default constructor
     * @param manager player manager that regroup all players
     * @param type type of data managed
     */
    public AbstractAudioPlayer(@NonNull PlayersManager manager, @NonNull DataType type){
        this.manager = manager;
        this.type = type;
    }

    /**
     * should allocate heavy resource here
     * @return if the operation succeed
     */
    @SuppressWarnings("UnusedReturnValue")
    public abstract boolean open();

    /**
     * free resources of the audio player. Assume the player can be re-opened later
     */
    public abstract void close();

    @SuppressWarnings("unchecked")
    public void load(IdentifiedEntry audio){
        this.audio = (T) audio;
    }
}