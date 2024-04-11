package com.demeth.massaudioplayer.backend.models.objects;

import android.net.Uri;

public class Metadata {
    public static class FileAudioMetadata extends Metadata{
        // private String display_name;
        private Uri uri;

        public FileAudioMetadata(Uri uri){
            this.uri=uri;
        }
        public FileAudioMetadata(){}

        public void setUri(Uri uri) {
            this.uri = uri;
        }

        public Uri getUri() {
            return uri;
        }
    }
}
