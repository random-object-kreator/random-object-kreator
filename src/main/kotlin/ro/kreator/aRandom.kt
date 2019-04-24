package ro.kreator

import ro.kreator.CreationLogic.aList
import ro.kreator.CreationLogic.hash
import ro.kreator.CreationLogic.with
import java.util.Collections.*
import kotlin.reflect.KProperty
import kotlin.reflect.KType

/**
 * A delegate which creates a random list of the specified type. It must be used as a delegate
 * using the delegate property syntax:
 *
 * val randomUsers by aRandomListOf<User>()
 *
 * It works with generic types as well.
 */
class aRandomListOf<out T : Any>(
        private val size: Int? = null,
        private val minSize: Int = 1,
        private val maxSize: Int = 5,
        private val customization: List<T>.() -> List<T> = { this }) {

    init {
        CreationLogic
    }

    private var t: List<T>? = null
    private var lastSeed = Seed.seed

    operator fun getValue(host: Any, property: KProperty<*>): List<T> {
        return if (t != null && lastSeed == Seed.seed) t!!
        else {
            val typeOfListItems = property.returnType.arguments.first().type!!
            val hostClassName = host::class.java.canonicalName
            val propertyName = property.name
            val list = aList(
                    typeOfListItems,
                    hostClassName.hash with propertyName.hash,
                    emptySet(),
                    kProperty = property,
                    size = size?.dec(),
                    minSize = minSize,
                    maxSize = maxSize
            )
            (list as List<T>).let {
                lastSeed = Seed.seed
                val res = it
                t = customization(res)
                t as List<T>
            }
        }
    }
}

/**
 * A delegate which creates a random object of the specified type. It must be used as a delegate
 * using the delegate property syntax:
 *
 * val aUser by aRandom<User>()
 *
 * It works with generic types as well.
 */
class aRandom<out T : Any>(private val customization: T.() -> T = { this }) {

    init {
        CreationLogic
    }

    private var t: T? = null
    private var lastSeed = Seed.seed

    operator fun getValue(hostClass: Any, property: KProperty<*>): T {
        return if (t != null && lastSeed == Seed.seed) t!!
        else instantiateRandomClass(property.returnType, property, hostClass::class.java.canonicalName.hash with property.name.hash).let {
            lastSeed = Seed.seed
            val res = it as T
            t = customization(res)
            return t as T
        }
    }
}

/**
 * A delegate which creates a random object of the specified type. It must be used as a delegate
 * using the delegate property syntax:
 *
 * val aUser by aRandom<User>()
 *
 * It works with generic types as well.
 */
class aRandomFromType<out T : Any>(private val type: KType, private val customization: T.() -> T = { this }) {

    init {
        CreationLogic
    }

    private var t: T? = null
    private var lastSeed = Seed.seed

    operator fun getValue(hostClass: Any, property: KProperty<*>): T {
        return if (t != null && lastSeed == Seed.seed) t!!
        else instantiateRandomClass(type, property, hostClass::class.java.canonicalName.hash with property.name.hash).let {
            lastSeed = Seed.seed
            val res = it as T
            t = customization(res)
            return t as T
        }
    }
}

fun instantiateRandomClass(type: KType, kProperty: KProperty<*>?, token: Long = 0): Any? = CreationLogic.instantiateRandomClass(type, token, kProperty = kProperty)

