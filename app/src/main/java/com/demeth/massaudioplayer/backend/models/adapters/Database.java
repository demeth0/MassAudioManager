package com.demeth.massaudioplayer.backend.models.adapters;


import android.content.Context;

import com.demeth.massaudioplayer.backend.models.objects.Audio;
import com.demeth.massaudioplayer.backend.models.objects.Metadata;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public interface Database {
    /**
     * Get the metadata related to this specific Audio.
     * @param audio The audio from which we retrieve the metadata.
     * @return The audio metadata.
     */
    Metadata getMetadata(Audio audio);

    /**
     *
     * @return All the audio entries in the database.
     */
    Collection<Audio> getEntries();

    /**
     * Get the entries filtered.
     * @param filter The filter.
     * @return The list of entries selected by the filter.
     */
    Collection<Audio> getEntries(Predicate<Audio> filter);


    /**
     * Reload the database.
     * @param context The context to use to reload the data.
     */
    void reload(Context context);
}
