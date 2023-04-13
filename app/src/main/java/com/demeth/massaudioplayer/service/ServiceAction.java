package com.demeth.massaudioplayer.service;

/**
 * a set of action that the service can process in the intent, names are self explanatory.
 */
public final class ServiceAction {
    public static final String NEXT_AUDIO="massaudioplayer.NEXT_AUDIO";
    public static final String PAUSE_AUDIO="massaudioplayer.PAUSE_AUDIO";
    public static final String PREV_AUDIO="massaudioplayer.PREV_AUDIO";
    public static final String END_SERVICE="massaudioplayer.END_SERVICE";

    public static final String START_SERVICE="massaudioplayer.START_SERVICE";
    public static final String DEVICE_UNPLUGGED="massaudioplayer.DEVICE_UNPLUGGED";


    public static final int HANDLER_STOP_SERVICE=1;
}
