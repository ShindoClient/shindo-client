package me.miki.shindo.management.autotext

data class AutoTextEntry(
    var id: String,
    var name: String,
    var textOrCommand: String,
    var keyCode: Int
)
