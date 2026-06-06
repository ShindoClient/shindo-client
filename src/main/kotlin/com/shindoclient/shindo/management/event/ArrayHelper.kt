package com.shindoclient.shindo.management.event

@Suppress("UNCHECKED_CAST")
class ArrayHelper<T>(
    private var elements: Array<T>,
) : MutableIterable<T> {
    constructor() : this(emptyArray<Any?>() as Array<T>)

    fun add(t: T?) {
        if (t != null) {
            val array = arrayOfNulls<Any>(size() + 1)
            for (i in array.indices) {
                if (i < size()) {
                    array[i] = get(i)
                } else {
                    array[i] = t
                }
            }
            set(array as Array<T>)
        }
    }

    fun contains(t: T): Boolean {
        val array = array()
        for (entry in array) {
            if (entry == t) {
                return true
            }
        }
        return false
    }

    fun remove(t: T) {
        if (contains(t)) {
            val array = arrayOfNulls<Any>(size() - 1)
            var b = true
            for (i in 0 until size()) {
                if (b && get(i) == t) {
                    b = false
                } else {
                    array[if (b) i else i - 1] = get(i)
                }
            }
            set(array as Array<T>)
        }
    }

    fun array(): Array<T> = elements

    fun size(): Int = array().size

    fun set(array: Array<T>) {
        elements = array
    }

    fun get(index: Int): T = array()[index]

    fun clear() {
        elements = emptyArray<Any?>() as Array<T>
    }

    fun isEmpty(): Boolean = size() == 0

    override fun iterator(): MutableIterator<T> =
        object : MutableIterator<T> {
            private var index = 0

            override fun hasNext(): Boolean = index < this@ArrayHelper.size() && this@ArrayHelper.get(index) != null

            override fun next(): T = this@ArrayHelper.get(index++)

            override fun remove() {
                this@ArrayHelper.remove(this@ArrayHelper.get(index))
            }
        }
}
