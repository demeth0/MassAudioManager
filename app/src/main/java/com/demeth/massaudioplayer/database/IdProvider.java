package com.demeth.massaudioplayer.database;

public class IdProvider {
    private static int current_id=0;
    private static int current_playlist = 0;
    public static int provideId(){
        current_id++;
        return current_id;
    }

    public static int providePlaylistId(){
        current_playlist++;
        return current_playlist;
    }
}
