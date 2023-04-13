package com.demeth.massaudioplayer.database;

/**
 * provide unique Ids for entry and playlists loading
 */
public class IdProvider {
    private static int current_id=0;
    private static int current_playlist = 0;

    /**
     * @return a unused entry Id
     */
    public static int provideId(){
        current_id++;
        return current_id;
    }

    /**
     * @return a unused playlist Id
     */
    public static int providePlaylistId(){
        current_playlist++;
        return current_playlist;
    }
}
