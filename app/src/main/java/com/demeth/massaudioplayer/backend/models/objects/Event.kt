package com.demeth.massaudioplayer.backend.models.objects

/**
 * List of Constants that are used to identify all [Event] that can be triggered.
 */
object EventCodeMap {
    /**
     * This event trigger when an audio track finished playing. This event is not triggered by a temporary pause.
     */
    const val EVENT_AUDIO_COMPLETED: Int = 1

    /**
     * This event is triggered when an audio start to be diffused. This event is not triggered when an audio resume after being paused.
     */
    const val EVENT_AUDIO_START: Int = 2

    const val EVENT_RANDOM_MODE_CHANGED: Int = 3

    const val EVENT_LOOP_MODE_CHANGED: Int = 4

    const val EVENT_DATABASE_RELOADED: Int = 5

    /**
     * Triggered when an audio was paused but resume.
     */
    const val EVENT_AUDIO_RESUME: Int = 6

    const val EVENT_AUDIO_PAUSED: Int = 7
}
/**
 * The event class represent a bundle that can be passed through the {@link com.demeth.massaudioplayer.backend.models.adapters.EventManager EventManager} when an event
 * is triggered. It is used to identify the type of event triggered and to pass data that could be relevant for the corresponding handlers.
 * Create an event with an identification code and a data bundle.
 * @param code The unique identifier used to designate the event to process.
 * @param data The data relevant to the event.
 */
data class Event(val code: Int = 1,val data: Any? = null)
