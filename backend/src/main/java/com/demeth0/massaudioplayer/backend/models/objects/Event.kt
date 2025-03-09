package com.demeth0.massaudioplayer.backend.models.objects

/**
 * List of Constants that are used to identify all [Event] that can be triggered.
 */
enum class EventCodeMap(v : Int) {
    /**
     * This event trigger when an audio track finished playing. This event is not triggered by a temporary pause.
     */
    EVENT_AUDIO_COMPLETED(1),

    /**
     * This event is triggered when an audio start to be diffused. This event is not triggered when an audio resume after being paused.
     */
    EVENT_AUDIO_START(2),

    EVENT_RANDOM_MODE_CHANGED(3),

    EVENT_LOOP_MODE_CHANGED(4),

    EVENT_DATABASE_RELOADED(5),

    /**
     * Triggered when an audio was paused but resume.
     */
    EVENT_AUDIO_RESUME(6),

    EVENT_AUDIO_PAUSED(7)
}
/**
 * The event class represent a bundle that can be passed through the {@link com.demeth0.massaudioplayer.backend.models.adapters.EventManager EventManager} when an event
 * is triggered. It is used to identify the type of event triggered and to pass data that could be relevant for the corresponding handlers.
 * Create an event with an identification code and a data bundle.
 * @param code The unique identifier used to designate the event to process.
 * @param data The data relevant to the event.
 */
data class Event(val code: EventCodeMap, val data: Any? = null) {

    //TODO temporary for java porting
    constructor(eventCodeMap: EventCodeMap) : this(eventCodeMap, null)
}
