package com.demeth.massaudioplayer.audio_player;

import androidx.annotation.NonNull;

import com.demeth.massaudioplayer.database.DataType;
import com.demeth.massaudioplayer.database.IdentifiedEntry;

public abstract class AbstractAudioPlayer<T extends IdentifiedEntry> implements Playable{
    final DataType type;
    protected T audio=null;
    protected PlayersManager manager;
    public AbstractAudioPlayer(@NonNull PlayersManager manager, @NonNull DataType type){
        this.manager = manager;
        this.type = type;
    }

    @SuppressWarnings("UnusedReturnValue")
    public abstract boolean open();
    public abstract void close();

    @SuppressWarnings("unchecked")
    public void load(IdentifiedEntry audio){
        this.audio = (T) audio;
    }
}