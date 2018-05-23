package ro.kreator

import ro.kreator.CreationLogic.hash
import ro.kreator.CreationLogic.instantiateRandomClass
import ro.kreator.CreationLogic.with
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.jvmErasure

fun forAll(numberOfRuns: Int = 100, block: PropertyBased.() -> Unit) {
    CreationLogic
    val currentMethod = Thread.currentThread().stackTrace[2]
    val map = mutableMapOf<KType, Int>()
    fun Int.times(blk: (Int) -> Unit) = (1..this).forEach(blk)

    numberOfRuns.times { Property(currentMethod.methodName.hash with it.hash, map).block() }
}

internal class Property(token: Token, countOfInvocations: MutableMap<KType, Int>) : PropertyBased(token,countOfInvocations)

sealed class PropertyBased(val token: Token, val countOfInvocations: MutableMap<KType, Int>): Reify() {

    fun KType.store() = this.apply { countOfInvocations[this]?.let { countOfInvocations[this] = it.inc() } ?: { countOfInvocations[this] = 0 }() }

    fun KType.tokenize(token: Token) = token with (countOfInvocations[this]?.hash ?: 0)

    inline fun <reified T : Any> a(): T {
        val type = T::class().type.store()
        return new(type, type.tokenize(token))
    }

    inline fun <reified T : Any> a(t: TypedKType<*>): T {
        val type = T::class(t).type.store()
        return new(type, type.tokenize(token))
    }

    inline fun <reified T : Any> a(t: TypedKType<*>, t2: TypedKType<*>): T {
        val type = T::class(t, t2).type.store()
        return new(type, type.tokenize(token))
    }

    inline fun <reified T : Any> a(t: TypedKType<*>, t2: TypedKType<*>, t3: TypedKType<*>): T {
        val type = T::class(t, t2, t3).type.store()
        return new(type, type.tokenize(token))
    }

    inline fun <reified T : Any> a(t: TypedKType<*>, t2: TypedKType<*>, t3: TypedKType<*>, t4: TypedKType<*>): T {
        val type = T::class(t, t2, t3, t4).type.store()
        return new(type, type.tokenize(token))
    }

    inline fun <reified T : Any> a(t: TypedKType<*>, t2: TypedKType<*>, t3: TypedKType<*>, t4: TypedKType<*>, t5: TypedKType<*>): T {
        val type = T::class(t, t2, t3, t4, t5).type.store()
        return new(type, type.tokenize(token))
    }

    fun <A> new(type: KType, token: Token): A {
        return instantiateRandomClass(type, token.hash with type.hash) as A
    }
}

data class TypedKType<T : Any>(val t: KClass<T>, val type: KType)

abstract class Reify {
    inline operator fun <reified T : Any> KClass<out T>.invoke(vararg types: TypedKType<*>): TypedKType<T> =
            TypedKType(T::class, this.createType(
                    types.zip(this.typeParameters.map { it.variance })
                            .map { KTypeProjection(it.second, it.first.type) }
            ))
}

class Creator(val type: KType, token: Token): PropertyBased(token, mutableMapOf()) {

    object any
    fun <T> any(): T = any as T

    operator inline fun <reified A, reified R : Any> ((A) -> R).get(a: A = any()): R {
        val params = getParameters(type, A::class)
        return invoke(a or new<A>(params[0], token))
    }

    operator inline fun <reified A, reified B, reified R : Any> ((A, B) -> R).get(a: A = any(), b: B = any()): R {
        val params = getParameters(type, A::class, B::class)
        return invoke(
                a.or(new<A>(params[0], token)),
                b.or(new<B>(params[1], token))
        )
    }

    operator inline fun <reified A, reified B, reified C, reified R : Any> ((A, B, C) -> R).get(a: A = any(), b: B = any(), c: C = any()): R {
        val params = getParameters(type, A::class, B::class, C::class)
        return invoke(
                a.or(new<A>(params[0], token)),
                b.or(new<B>(params[1], token)),
                c.or(new<C>(params[2], token))
        )
    }

    operator inline fun <reified A, reified B, reified C, reified D, reified R : Any> ((A, B, C, D) -> R).get(a: A = any(), b: B = any(), c: C = any(), d: D = any()): R {
        val params = getParameters(type, A::class, B::class, C::class, D::class)
        return invoke(
                a.or(new<A>(params[0], token)),
                b.or(new<B>(params[1], token)),
                c.or(new<C>(params[2], token)),
                d.or(new<D>(params[3], token))
        )
    }

    operator inline fun <reified A, reified B, reified C, reified D, reified E, reified R : Any>
            ((A, B, C, D, E) -> R).get(a: A = any(), b: B = any(), c: C = any(), d: D = any(), e: E = any()): R {
        val params = getParameters(type, A::class, B::class, C::class, D::class, E::class)
        return invoke(
                a.or(new<A>(params[0], token)),
                b.or(new<B>(params[1], token)),
                c.or(new<C>(params[2], token)),
                d.or(new<D>(params[3], token)),
                e.or(new<E>(params[4], token))
        )
    }

    operator inline fun <reified A, reified B, reified C, reified D, reified E, reified F, reified R : Any>
            ((A, B, C, D, E, F) -> R).get(a: A = any(), b: B = any(), c: C = any(), d: D = any(), e: E = any(), f: F): R {
        val params = getParameters(type, A::class, B::class, C::class, D::class, E::class, F::class)
        return invoke(
                a or new<A>(params[0], token),
                b or new<B>(params[1], token),
                c or new<C>(params[2], token),
                d or new<D>(params[3], token),
                e or new<E>(params[4], token),
                f or new<F>(params[5], token)
        )
    }

    fun <R : Any> getConstructor(klass: KClass<R>) = klass.constructors.filter { !it.parameters.any { (it.type.jvmErasure == klass) } }.toList()

    infix fun <T> T.or(other: T) = if (this == any) other else this

    fun getParameters(type: KType, vararg classes: KClass<*>): List<KType> {
        val klass = type.jvmErasure
        val generics = type.arguments.map { it.type }
        val constructors = getConstructor(klass)
        if (constructors.isEmpty() && klass.constructors.any { it.parameters.any { (it.type.jvmErasure == klass) } }) throw CyclicException()
        val defaultConstructor: KFunction<*> = constructors.filter {
            it.parameters.size == classes.size &&
                    it.parameters
                            .zip(classes)
                            .all {
                                it.first.type.jvmErasure == it.second ||
                                        it.first.type.jvmErasure.java.isAssignableFrom(it.second.java)
                            }
        }.first()
        defaultConstructor.isAccessible = true
        val constructorParameters = defaultConstructor.parameters.map { it.type }.map { param ->
            if (param.jvmErasure == Any::class) {
                generics[defaultConstructor.typeParameters.map { it.name }.indexOf(param.classifier?.starProjectedType.toString())]!!
            } else {
                param
            }
        }
        return constructorParameters
    }
}
