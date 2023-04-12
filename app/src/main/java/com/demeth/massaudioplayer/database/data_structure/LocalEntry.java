package com.demeth.massaudioplayer.database.data_structure;

import android.net.Uri;

import com.demeth.massaudioplayer.database.DataType;
import com.demeth.massaudioplayer.database.IdProvider;
import com.demeth.massaudioplayer.database.IdentifiedEntry;

public class LocalEntry extends IdentifiedEntry {
    protected transient final Uri path;
    public LocalEntry(String name,Uri path) {
        super(name, path,IdProvider.provideId());
        this.path=path;
    }

    public Uri getPath() {
        return path;
    }

    @Override
    public DataType getType() {
        return DataType.LOCAL;
    }
}
