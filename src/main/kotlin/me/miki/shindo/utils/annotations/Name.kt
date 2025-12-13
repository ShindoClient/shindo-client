package me.miki.shindo.utils.annotations

@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class Name(val value: String)
