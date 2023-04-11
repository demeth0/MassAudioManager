package com.demeth.massaudioplayer.audio_player;

public interface Playable {
    boolean play();
    boolean pause();
    boolean resume();
    boolean seekTo(int pos);
    int getDuration();
    int getPosition();
}
