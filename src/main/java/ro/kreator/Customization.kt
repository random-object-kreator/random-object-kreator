package ro.kreator

import ro.kreator.CreationLogic.ObjectFactory
import kotlin.reflect.KProperty

interface Customizer {
    fun register() = this::class.java.methods
            .filter { it.parameters.isEmpty() && it.name.startsWith("get") }
            .forEach { it.invoke(this) }
}

class customize<T> {
    fun using(fn: () -> T, g: Creator.(() -> T) -> T) = Delegate0(fn, g)
    fun <A> using(fn: (A) -> T, g: Creator.((A) -> T) -> T) = Delegate1(fn, g)
    fun <A, B> using(fn: (A, B) -> T, g: Creator.((A, B) -> T) -> T) = Delegate2(fn, g)
    fun <A, B, C> using(fn: (A, B, C) -> T, g: Creator.((A, B, C) -> T) -> T) = Delegate3(fn, g)
    fun <A, B, C, D> using(fn: (A, B, C, D) -> T, g: Creator.((A, B, C, D) -> T) -> T) = Delegate4(fn, g)
    fun <A, B, C, D, E> using(fn: (A, B, C, D, E) -> T, g: Creator.((A, B, C, D, E) -> T) -> T) = Delegate5(fn, g)
    fun <A, B, C, D, E, F> using(fn: (A, B, C, D, E, F) -> T, g: Creator.((A, B, C, D, E, F) -> T) -> T) = Delegate6(fn, g)

    class Delegate0<T>(val constructor: () -> T, val constructorBlock: Creator.(() -> T) -> T) {
        operator fun getValue(a: Any, property: KProperty<*>): T {
            ObjectFactory[property.returnType] = { _, _, token -> Creator(property.returnType, token).constructorBlock(constructor) as Any }
            return t()
        }
    }

    class Delegate1<A, T>(val constructor: (A) -> T, val constructorBlock: Creator.((A) -> T) -> T) {
        operator fun getValue(a: Any, property: KProperty<*>): T {
            ObjectFactory[property.returnType] = { _, _, token -> Creator(property.returnType, token).constructorBlock(constructor) as Any }
            return t()
        }
    }

    class Delegate2<A, B, T>(val constructor: (A, B) -> T, val constructorBlock: Creator.((A, B) -> T) -> T) {
        operator fun getValue(a: Any, property: KProperty<*>): T {
            ObjectFactory[property.returnType] = { _, _, token -> Creator(property.returnType, token).constructorBlock(constructor) as Any }
            return t()
        }
    }

    class Delegate3<A, B, C, T>(val constructor: (A, B, C) -> T, val constructorBlock: Creator.((A, B, C) -> T) -> T) {
        operator fun getValue(a: Any, property: KProperty<*>): T {
            ObjectFactory[property.returnType] = { _, _, token -> Creator(property.returnType, token).constructorBlock(constructor) as Any }
            return t()
        }
    }

    class Delegate4<A, B, C, D, T>(val constructor: (A, B, C, D) -> T, val constructorBlock: Creator.((A, B, C, D) -> T) -> T) {
        operator fun getValue(a: Any, property: KProperty<*>): T {
            ObjectFactory[property.returnType] = { _, _, token -> Creator(property.returnType, token).constructorBlock(constructor) as Any }
            return t()
        }
    }

    class Delegate5<A, B, C, D, E, T>(val constructor: (A, B, C, D, E) -> T, val constructorBlock: Creator.((A, B, C, D, E) -> T) -> T) {
        operator fun getValue(a: Any, property: KProperty<*>): T {
            ObjectFactory[property.returnType] = { _, _, token -> Creator(property.returnType, token).constructorBlock(constructor) as Any }
            return t()
        }
    }

    class Delegate6<A, B, C, D, E, F, T>(val constructor: (A, B, C, D, E, F) -> T, val constructorBlock: Creator.((A, B, C, D, E, F) -> T) -> T) {
        operator fun getValue(a: Any, property: KProperty<*>): T {
            ObjectFactory[property.returnType] = { _, _, token -> Creator(property.returnType, token).constructorBlock(constructor) as Any }
            return t()
        }
    }
}

private fun <R> t() = null as R
