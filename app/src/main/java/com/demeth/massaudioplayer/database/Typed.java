package com.demeth.massaudioplayer.database;

/**
 * typed entry can be identified by the AudioPlayer
 *
 * @see com.demeth.massaudioplayer.audio_player.AudioPlayer
 */
public interface Typed {
    DataType getType();
}
