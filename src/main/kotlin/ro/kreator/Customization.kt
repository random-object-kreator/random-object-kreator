package ro.kreator

import ro.kreator.CreationLogic.GenericObjectFactory
import ro.kreator.customize.*
import kotlin.reflect.KProperty

interface Customizer {
    fun register() = this::class.java.methods
            .filter { it.parameters.isEmpty() && it.name.startsWith("get") }
            .forEach { it.invoke(this) }
}

fun <T> customize(constructorBlock: Creator.() -> T) = DelegateNoArg(constructorBlock)

fun registerCustomizations(vararg customizations: Customization) = listOf(customizations)

class customize<T> {


    fun using(fn: () -> T, g: Creator.(() -> T) -> T) = Delegate0(fn, g)
    fun <A> using(fn: (A) -> T, g: Creator.((A) -> T) -> T) = Delegate1(fn, g)
    fun <A, B> using(fn: (A, B) -> T, g: Creator.((A, B) -> T) -> T) = Delegate2(fn, g)
    fun <A, B, C> using(fn: (A, B, C) -> T, g: Creator.((A, B, C) -> T) -> T) = Delegate3(fn, g)
    fun <A, B, C, D> using(fn: (A, B, C, D) -> T, g: Creator.((A, B, C, D) -> T) -> T) = Delegate4(fn, g)
    fun <A, B, C, D, E> using(fn: (A, B, C, D, E) -> T, g: Creator.((A, B, C, D, E) -> T) -> T) = Delegate5(fn, g)
    fun <A, B, C, D, E, F> using(fn: (A, B, C, D, E, F) -> T, g: Creator.((A, B, C, D, E, F) -> T) -> T) = Delegate6(fn, g)
    fun <A, B, C, D, E, F, G> using(fn: (A, B, C, D, E, F, G) -> T, g: Creator.((A, B, C, D, E, F, G) -> T) -> T) = Delegate7(fn, g)
    fun <A, B, C, D, E, F, G, H> using(fn: (A, B, C, D, E, F, G, H) -> T, g: Creator.((A, B, C, D, E, F, G, H) -> T) -> T) = Delegate8(fn, g)
    fun <A, B, C, D, E, F, G, H, I> using(fn: (A, B, C, D, E, F, G, H, I) -> T, g: Creator.((A, B, C, D, E, F, G, H, I) -> T) -> T) = Delegate9(fn, g)
    fun <A, B, C, D, E, F, G, H, I, J> using(fn: (A, B, C, D, E, F, G, H, I, J) -> T, g: Creator.((A, B, C, D, E, F, G, H, I, J) -> T) -> T) = Delegate10(fn, g)
    fun <A, B, C, D, E, F, G, H, I, J, K> using(fn: (A, B, C, D, E, F, G, H, I, J, K) -> T, g: Creator.((A, B, C, D, E, F, G, H, I, J, K) -> T) -> T) = Delegate11(fn, g)
    fun <A, B, C, D, E, F, G, H, I, J, K, L> using(fn: (A, B, C, D, E, F, G, H, I, J, K, L) -> T, g: Creator.((A, B, C, D, E, F, G, H, I, J, K, L) -> T) -> T) = Delegate12(fn, g)
    fun <A, B, C, D, E, F, G, H, I, J, K, L, M> using(fn: (A, B, C, D, E, F, G, H, I, J, K, L, M) -> T, g: Creator.((A, B, C, D, E, F, G, H, I, J, K, L, M) -> T) -> T) = Delegate13(fn, g)
    fun <A, B, C, D, E, F, G, H, I, J, K, L, M, N> using(fn: (A, B, C, D, E, F, G, H, I, J, K, L, M, N) -> T, g: Creator.((A, B, C, D, E, F, G, H, I, J, K, L, M, N) -> T) -> T) = Delegate14(fn, g)
    fun <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O> using(fn: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O) -> T, g: Creator.((A, B, C, D, E, F, G, H, I, J, K, L, M, N, O) -> T) -> T) = Delegate15(fn, g)
    fun <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P> using(fn: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P) -> T, g: Creator.((A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P) -> T) -> T) = Delegate16(fn, g)
    fun <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q> using(fn: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q) -> T, g: Creator.((A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q) -> T) -> T) = Delegate17(fn, g)
    fun <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R> using(fn: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R) -> T, g: Creator.((A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R) -> T) -> T) = Delegate18(fn, g)
    fun <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S> using(fn: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S) -> T, g: Creator.((A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S) -> T) -> T) = Delegate19(fn, g)


    interface Customization {
        operator fun getValue(a: Any, property: KProperty<*>): Customization
    }

    class DelegateNoArg<T>(val constructorBlock: Creator.() -> T) : Customization {

        override operator fun getValue(a: Any, property: KProperty<*>): DelegateNoArg<T> {
            val type = property.returnType.arguments.last().type!!
            GenericObjectFactory[type] = { _, _, kproperty, token -> Creator(type, token, kproperty).constructorBlock() as Any }
            return this
        }
    }

    class Delegate0<T>(val constructor: () -> T, val constructorBlock: Creator.(() -> T) -> T) : Customization {

        override operator fun getValue(a: Any, property: KProperty<*>): Delegate0<T> {
            val type = property.returnType.arguments.last().type!!
            GenericObjectFactory[type] = { _, _, kproperty, token -> Creator(type, token, kproperty).constructorBlock(constructor) as Any }
            return this
        }
    }

    class Delegate1<A, T>(val constructor: (A) -> T, val constructorBlock: Creator.((A) -> T) -> T) : Customization {
        override operator fun getValue(a: Any, property: KProperty<*>): Delegate1<A, T> {
            val type = property.returnType.arguments.last().type!!
            GenericObjectFactory[type] = { _, _, kproperty, token -> Creator(type, token, kproperty).constructorBlock(constructor) as Any }
            return this
        }
    }

    class Delegate2<A, B, T>(val constructor: (A, B) -> T, val constructorBlock: Creator.((A, B) -> T) -> T) : Customization {
        override operator fun getValue(a: Any, property: KProperty<*>): Delegate2<A, B, T> {
            val type = property.returnType.arguments.last().type!!
            GenericObjectFactory[type] = { _, _, kproperty, token -> Creator(type, token, kproperty).constructorBlock(constructor) as Any }
            return this
        }
    }

    class Delegate3<A, B, C, T>(val constructor: (A, B, C) -> T, val constructorBlock: Creator.((A, B, C) -> T) -> T) : Customization {
        override operator fun getValue(a: Any, property: KProperty<*>): Delegate3<A, B, C, T> {
            val type = property.returnType.arguments.last().type!!
            GenericObjectFactory[type] = { _, _, kproperty, token -> Creator(type, token, kproperty).constructorBlock(constructor) as Any }
            return this
        }
    }

    class Delegate4<A, B, C, D, T>(val constructor: (A, B, C, D) -> T, val constructorBlock: Creator.((A, B, C, D) -> T) -> T) : Customization {
        override operator fun getValue(a: Any, property: KProperty<*>): Delegate4<A, B, C, D, T> {
            val type = property.returnType.arguments.last().type!!
            GenericObjectFactory[type] = { _, _, kproperty, token -> Creator(type, token, kproperty).constructorBlock(constructor) as Any }
            return this
        }
    }

    class Delegate5<A, B, C, D, E, T>(val constructor: (A, B, C, D, E) -> T, val constructorBlock: Creator.((A, B, C, D, E) -> T) -> T) : Customization {
        override operator fun getValue(a: Any, property: KProperty<*>): Delegate5<A, B, C, D, E, T> {
            val type = property.returnType.arguments.last().type!!
            GenericObjectFactory[type] = { _, _, kproperty, token -> Creator(property.returnType.arguments.last().type!!, token, kproperty).constructorBlock(constructor) as Any }
            return this
        }
    }

    class Delegate6<A, B, C, D, E, F, T>(val constructor: (A, B, C, D, E, F) -> T, val constructorBlock: Creator.((A, B, C, D, E, F) -> T) -> T) : Customization {
        override operator fun getValue(a: Any, property: KProperty<*>): Delegate6<A, B, C, D, E, F, T> {
            val type = property.returnType.arguments.last().type!!
            GenericObjectFactory[type] = { _, _, kproperty, token -> Creator(property.returnType.arguments.last().type!!, token, kproperty).constructorBlock(constructor) as Any }
            return this
        }
    }

    class Delegate7<A, B, C, D, E, F, G, T>(val constructor: (A, B, C, D, E, F, G) -> T, val constructorBlock: Creator.((A, B, C, D, E, F, G) -> T) -> T) : Customization {
        override operator fun getValue(a: Any, property: KProperty<*>): Delegate7<A, B, C, D, E, F, G, T> {
            val type = property.returnType.arguments.last().type!!
            GenericObjectFactory[type] = { _, _, kproperty, token -> Creator(property.returnType.arguments.last().type!!, token, property).constructorBlock(constructor) as Any }
            return this
        }
    }

    class Delegate8<A, B, C, D, E, F, G, H, T>(val constructor: (A, B, C, D, E, F, G, H) -> T, val constructorBlock: Creator.((A, B, C, D, E, F, G, H) -> T) -> T) : Customization {
        override operator fun getValue(a: Any, property: KProperty<*>): Delegate8<A, B, C, D, E, F, G, H, T> {
            val type = property.returnType.arguments.last().type!!
            GenericObjectFactory[type] = { _, _, kproperty, token -> Creator(property.returnType.arguments.last().type!!, token, kproperty).constructorBlock(constructor) as Any }
            return this
        }
    }

    class Delegate9<A, B, C, D, E, F, G, H, I, T>(val constructor: (A, B, C, D, E, F, G, H, I) -> T, val constructorBlock: Creator.((A, B, C, D, E, F, G, H, I) -> T) -> T) : Customization {
        override operator fun getValue(a: Any, property: KProperty<*>): Delegate9<A, B, C, D, E, F, G, H, I, T> {
            val type = property.returnType.arguments.last().type!!
            GenericObjectFactory[type] = { _, _, kproperty, token -> Creator(property.returnType.arguments.last().type!!, token, kproperty).constructorBlock(constructor) as Any }
            return this
        }
    }

    class Delegate10<A, B, C, D, E, F, G, H, I, J, T>(val constructor: (A, B, C, D, E, F, G, H, I, J) -> T, val constructorBlock: Creator.((A, B, C, D, E, F, G, H, I, J) -> T) -> T) : Customization {
        override operator fun getValue(a: Any, property: KProperty<*>): Delegate10<A, B, C, D, E, F, G, H, I, J, T> {
            val type = property.returnType.arguments.last().type!!
            GenericObjectFactory[type] = { _, _, kproperty, token -> Creator(property.returnType.arguments.last().type!!, token, kproperty).constructorBlock(constructor) as Any }
            return this
        }
    }

    class Delegate11<A, B, C, D, E, F, G, H, I, J, K, T>(val constructor: (A, B, C, D, E, F, G, H, I, J, K) -> T, val constructorBlock: Creator.((A, B, C, D, E, F, G, H, I, J, K) -> T) -> T) : Customization {
        override operator fun getValue(a: Any, property: KProperty<*>): Delegate11<A, B, C, D, E, F, G, H, I, J, K, T> {
            val type = property.returnType.arguments.last().type!!
            GenericObjectFactory[type] = { _, _, kproperty, token -> Creator(property.returnType.arguments.last().type!!, token, kproperty).constructorBlock(constructor) as Any }
            return this
        }
    }

    class Delegate12<A, B, C, D, E, F, G, H, I, J, K, L, T>(val constructor: (A, B, C, D, E, F, G, H, I, J, K, L) -> T, val constructorBlock: Creator.((A, B, C, D, E, F, G, H, I, J, K, L) -> T) -> T) : Customization {
        override operator fun getValue(a: Any, property: KProperty<*>): Delegate12<A, B, C, D, E, F, G, H, I, J, K, L, T> {
            val type = property.returnType.arguments.last().type!!
            GenericObjectFactory[type] = { _, _, kproperty, token -> Creator(property.returnType.arguments.last().type!!, token, kproperty).constructorBlock(constructor) as Any }
            return this
        }
    }

    class Delegate13<A, B, C, D, E, F, G, H, I, J, K, L, M, T>(val constructor: (A, B, C, D, E, F, G, H, I, J, K, L, M) -> T, val constructorBlock: Creator.((A, B, C, D, E, F, G, H, I, J, K, L, M) -> T) -> T) : Customization {
        override operator fun getValue(a: Any, property: KProperty<*>): Delegate13<A, B, C, D, E, F, G, H, I, J, K, L, M, T> {
            val type = property.returnType.arguments.last().type!!
            GenericObjectFactory[type] = { _, _, kproperty, token -> Creator(property.returnType.arguments.last().type!!, token, property).constructorBlock(constructor) as Any }
            return this
        }
    }

    class Delegate14<A, B, C, D, E, F, G, H, I, J, K, L, M, N, T>(val constructor: (A, B, C, D, E, F, G, H, I, J, K, L, M, N) -> T, val constructorBlock: Creator.((A, B, C, D, E, F, G, H, I, J, K, L, M, N) -> T) -> T) : Customization {
        override operator fun getValue(a: Any, property: KProperty<*>): Delegate14<A, B, C, D, E, F, G, H, I, J, K, L, M, N, T> {
            val type = property.returnType.arguments.last().type!!
            GenericObjectFactory[type] = { _, _, kproperty, token -> Creator(property.returnType.arguments.last().type!!, token, kproperty).constructorBlock(constructor) as Any }
            return this
        }
    }

    class Delegate15<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, T>(val constructor: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O) -> T, val constructorBlock: Creator.((A, B, C, D, E, F, G, H, I, J, K, L, M, N, O) -> T) -> T) : Customization {
        override operator fun getValue(a: Any, property: KProperty<*>): Delegate15<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, T> {
            val type = property.returnType.arguments.last().type!!
            GenericObjectFactory[type] = { _, _, kproperty, token -> Creator(property.returnType.arguments.last().type!!, token, kproperty).constructorBlock(constructor) as Any }
            return this
        }
    }

    class Delegate16<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, T>(val constructor: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P) -> T, val constructorBlock: Creator.((A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P) -> T) -> T) : Customization {
        override operator fun getValue(a: Any, property: KProperty<*>): Delegate16<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, T> {
            val type = property.returnType.arguments.last().type!!
            GenericObjectFactory[type] = { _, _, kproperty, token -> Creator(property.returnType.arguments.last().type!!, token, kproperty).constructorBlock(constructor) as Any }
            return this
        }
    }

    class Delegate17<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, T>(val constructor: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q) -> T, val constructorBlock: Creator.((A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q) -> T) -> T) : Customization {
        override operator fun getValue(a: Any, property: KProperty<*>): Delegate17<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, T> {
            val type = property.returnType.arguments.last().type!!
            GenericObjectFactory[type] = { _, _, kproperty, token -> Creator(property.returnType.arguments.last().type!!, token, kproperty).constructorBlock(constructor) as Any }
            return this
        }
    }

    class Delegate18<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, T>(val constructor: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R) -> T, val constructorBlock: Creator.((A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R) -> T) -> T) : Customization {
        override fun getValue(a: Any, property: KProperty<*>): Delegate18<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, T> {
            val type = property.returnType.arguments.last().type!!
            GenericObjectFactory[type] = { _, _, kproperty, token -> Creator(property.returnType.arguments.last().type!!, token, kproperty).constructorBlock(constructor) as Any }
            return this
        }
    }

    class Delegate19<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T>(val constructor: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S) -> T, val constructorBlock: Creator.((A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S) -> T) -> T) : Customization {
        override operator fun getValue(a: Any, property: KProperty<*>): Delegate19<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T> {
            val type = property.returnType.arguments.last().type!!
            GenericObjectFactory[type] = { _, _, kproperty, token -> Creator(property.returnType.arguments.last().type!!, token, kproperty).constructorBlock(constructor) as Any }
            return this
        }
    }
}
