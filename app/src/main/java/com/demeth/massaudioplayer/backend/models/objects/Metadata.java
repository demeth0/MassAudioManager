package com.demeth.massaudioplayer.backend.models.objects;

import android.net.Uri;

/**
 * This object is used to save data specific to each {@link Audio} that could be used to for example load the album cover or get the streaming data...
 * Metadata is the base class and doesn't contain any useful methods or data, it should always be casted depending on the audio type used.
 *
 * @see FileAudioMetadata
 */
public class Metadata {
    /**
     * {@link Metadata} used with an audio of the type {@link AudioType#LOCAL}.
     */
    public static class FileAudioMetadata extends Metadata{
        // private String display_name;
        private Uri uri;

        /**
         * Create a local audio file metadata object that will stock the uri to the file for loading the audio and album cover.
         * @param uri Uri of the audio file following Android path convention.
         */
        public FileAudioMetadata(Uri uri){
            this.uri=uri;
        }

        /**
         * Create a local file metadata object uninitialized.
         */
        public FileAudioMetadata(){}

        /**
         * @param uri New uri of the audio file.
         */
        public void setUri(Uri uri) {
            this.uri = uri;
        }

        /**
         * @return The uri saved in this metadata that should point to an audio file.
         */
        public Uri getUri() {
            return uri;
        }
    }
}
