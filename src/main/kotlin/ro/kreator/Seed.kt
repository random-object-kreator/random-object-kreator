package ro.kreator

import java.util.*

object Seed : Reify() {
    var testing = false

    @JvmStatic
    var seed = Random().nextLong()
        set(value) {
            field = value
            if (!testing) println("Random-Object-Kreator - Overriding seed: $value")
        }

    init {
        println("Random-Object-Kreator - Setting seed: $seed")
    }
}
