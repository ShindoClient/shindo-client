package com.shindoclient.shindo.types

class CircQueue<T>
    @SafeVarargs
    constructor(
        vararg arr: T,
    ) {
        private val arr: Array<T>
        private var head = 0
        private var size = 0

        fun peek(): T = arr[head]

        fun poll(): T {
            val `val` = arr[head]
            head = (head + 1) % size
            return `val`
        }

        init {
            this.arr = arr as Array<T>
            size = arr.size
        }
    }
