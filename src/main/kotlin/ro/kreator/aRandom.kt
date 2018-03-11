package ro.kreator

import ro.kreator.CreationLogic.aList
import ro.kreator.CreationLogic.hash
import ro.kreator.CreationLogic.instantiateRandomClass
import ro.kreator.CreationLogic.with
import kotlin.reflect.KProperty

/**
 * A delegate which creates a random list of the specified type. It must be used as a delegate
 * using the delegate property syntax:
 *
 * val randomUsers by aRandomListOf<User>()
 *
 * It works with generic types as well.
 */
class aRandomListOf<out T : Any>(private val size: Int? = null) {

    init {
        CreationLogic
    }

    operator fun getValue(host: Any, property: KProperty<*>): List<T> {
        val typeOfListItems = property.returnType.arguments.first().type!!
        val hostClassName = host::class.java.canonicalName
        val propertyName = property.name
        val list = aList(typeOfListItems, hostClassName.hash with propertyName.hash , emptySet(), size?.dec())
        return list as List<T>
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
        else instantiateRandomClass(property.returnType, hostClass::class.java.canonicalName.hash with property.name.hash).let {
            lastSeed = Seed.seed
            val res = it as T
            t = customization(res)
            return t as T
        }
    }
}
