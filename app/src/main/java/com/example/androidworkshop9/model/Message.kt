package com.example.androidworkshop9.model

data class Message(
    val value: String,
    val aligment: Aligment
) {
    enum class Aligment {
        Start, End, Center
    }
}
