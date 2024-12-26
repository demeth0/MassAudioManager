package com.demeth.massaudioplayer.backend.models.objects;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * Object that stock information related to an audio track.
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

    /**
     *
     * @param display_name The name that should if needed be printed for human reading.
     * @param path A string identifier that should uniquely identify the track.
     * @param type The source of the audio track from which it was loaded.
     */
    public Audio(String display_name,String path,AudioType type){
        this.path=path;
        this.type=type;
        this.display_name=display_name;
    }


    /**
     * The comparison system was override to allow two audio track coming from different media to be considered the same.
     *
     * @param audio
     * @return
     */
    @Override
    public int compareTo(Audio audio) {
        return this.display_name.compareTo(audio.display_name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Audio audio = (Audio) o;
        return type == audio.type && Objects.equals(path, audio.path) && Objects.equals(display_name, audio.display_name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, path, display_name);
    }

    @NonNull
    @Override
    public String toString() {
        return "Audio ["+type.name()+":"+path+"]";
    }
}
