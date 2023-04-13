package com.demeth.massaudioplayer.audio_player;

/**
 * interface for basing audio control to implement in classes that should control the AudioPlayer
 */
public interface Playable {
    boolean play();
    boolean pause();
    boolean resume();
    boolean seekTo(int pos);
    int getDuration();
    int getPosition();
}
