package com.demeth.massaudioplayer.backend.models.objects;

/**
 * List of Constants that are used to identify all {@link Event} that can be triggered.
 */
public class EventCodeMap {

    /**
     * This event trigger when an audio track finished playing. This event is not triggered by a temporary pause.
     */
    public static final int EVENT_AUDIO_COMPLETED = 1;

    /**
     * This event is triggered when an audio start to be diffused. This event is not triggered when an audio resume after being paused.
     */
    public static final int EVENT_AUDIO_START = 2;
}
