package com.demeth.massaudioplayer.database.data_structure;

import android.net.Uri;

import com.demeth.massaudioplayer.database.DataType;
import com.demeth.massaudioplayer.database.IdProvider;
import com.demeth.massaudioplayer.database.IdentifiedEntry;

/**
 * define a local track stocked on the phone as a file
 */
public class LocalEntry extends IdentifiedEntry {
    protected transient final Uri path;

    /**
     *
     * @param name name of the audio may not be unique
     * @param path path to the file
     */
    public LocalEntry(String name,Uri path) {
        super(name, path,IdProvider.provideId());
        this.path=path;
    }

    /**
     * @return get the file path
     */
    public Uri getPath() {
        return path;
    }

    /**
     * @return DataType.LOCAL
     */
    @Override
    public DataType getType() {
        return DataType.LOCAL;
    }
}
