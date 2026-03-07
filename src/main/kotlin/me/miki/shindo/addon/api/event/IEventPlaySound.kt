package me.miki.shindo.addon.api.event

/** Som tocado. */
interface IEventPlaySound : IEvent {
    fun getSoundName(): String
    fun getVolume(): Float
    fun setVolume(volume: Float)
    fun getPitch(): Float
    fun setPitch(pitch: Float)
}
