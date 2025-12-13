package me.miki.shindo.utils.annotations

@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class Author(vararg val value: String)
