package com.demeth.massaudioplayer.backend.models.objects;

import androidx.annotation.NonNull;

/**
 * Model for audio object
 */
public class Audio implements Comparable<Audio>{
    /**
     * Identify which audio player to use.
     */
    public final AudioType type;

    /**
     * Path string that give access to the source data, can be a spotify Id, local file path or youtube video Id.
     */
    public final String path;

    /**
     * String that represent the display name of the audio.
     */
    public final String display_name;

    public Audio(String display_name,String path,AudioType type){
        this.path=path;
        this.type=type;
        this.display_name=display_name;
    }


    @Override
    public int compareTo(Audio audio) {
        return this.display_name.compareTo(audio.display_name);
    }

    @NonNull
    @Override
    public String toString() {
        return "Audio ["+type.name()+":"+path+"]";
    }
}
