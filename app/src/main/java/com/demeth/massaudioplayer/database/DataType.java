package com.demeth.massaudioplayer.database;

/**
 * list all tye of entry possibles
 * PLAYLIST that designate a subset of entries
 * LOCAL that design a local entry saved on the device
 * NONE undefined entry that should be ignored
 */
public enum DataType {
    LOCAL,
    PLAYLIST,
    NONE;
}
