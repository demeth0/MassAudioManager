package com.demeth.massaudioplayer.backend.models.adapters;

import android.content.Context;

import com.demeth.massaudioplayer.backend.models.objects.Audio;
import com.demeth.massaudioplayer.backend.models.objects.Metadata;

/**
 * Provide specific implementations to load different categories of Audio. For example Spotify or Local...
 */
public interface DatabaseContentProvider {
    /**
     * This class is used as the adaptable return object of the database provider to return wrapped Audio and metadata objects.
     */
    final class Content{
        public final Audio audio;
        public final Metadata metadata;

        /**
         * Create a content object containing one audio. The metadata are set to null.
         * @param audio The audio to wrap in the container.
         */
        public Content(Audio audio){
            this.audio=audio;
            this.metadata=null;
        }

        /**
         * Create a content object containing one audio and his associated metadata.
         * @param audio The audio to wrap in the container.
         * @param metadata The metadata to wrap in the container.
         */
        public Content(Audio audio, Metadata metadata){
            this.audio=audio;
            this.metadata=metadata;
        }
    }

    /**
     * Iterator that provide loaded content for the database to save.
     * @return The next content loaded from this provider.
     */
    Content next();

    /**
     * Check if the provider still has data to return.
     * @return True if there is still data to load in the database.
     */
    boolean hasNext();

    /**
     * Open the provider for content extraction.
     * @param context The application context used to get authorization and access to the android API.
     */
    void open(Context context);

    /**
     * Close the provider after use to free resources.
     */
    void close();
}
