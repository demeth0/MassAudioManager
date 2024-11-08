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

    public static final int EVENT_RANDOM_MODE_CHANGED = 3;

    public static final int EVENT_LOOP_MODE_CHANGED = 4;

    public static final int EVENT_DATABASE_RELOADED = 5;

    /**
     * Triggered when an audio was paused but resume.
     */
    public static final int EVENT_AUDIO_RESUME = 6;

    public static final int EVENT_AUDIO_PAUSED = 7;


}
