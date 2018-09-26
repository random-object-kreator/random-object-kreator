package ro.kreator

import ro.kreator.CreationLogic.hash
import ro.kreator.CreationLogic.instantiateRandomClass
import ro.kreator.CreationLogic.with
import javax.swing.text.html.HTML.Tag.I
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

internal class Property(token: Token, countOfInvocations: MutableMap<KType, Int>) : PropertyBased(token, countOfInvocations)

sealed class PropertyBased(val token: Token, val countOfInvocations: MutableMap<KType, Int>) : Reify() {

    fun KType.store() = this.apply {
        countOfInvocations[this]?.let { countOfInvocations[this] = it.inc() } ?: { countOfInvocations[this] = 0 }()
    }

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

class Creator(val type: KType, token: Token) : PropertyBased(token, mutableMapOf()) {

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

    operator inline fun <reified A, reified B, reified C, reified D, reified E, reified F, reified G, reified RR : Any>
            ((A, B, C, D, E, F, G) -> RR).get(a: A = any(), b: B = any(), c: C = any(), d: D = any(), e: E = any(), f: F = any(), g: G = any()): RR {
        val params = getParameters(type, A::class, B::class, C::class, D::class, E::class, F::class, G::class)
        return invoke(
                a or new<A>(params[0], token),
                b or new<B>(params[1], token),
                c or new<C>(params[2], token),
                d or new<D>(params[3], token),
                e or new<E>(params[4], token),
                f or new<F>(params[5], token),
                g or new<G>(params[6], token)
        )
    }


    operator inline fun <reified A, reified B, reified C, reified D, reified E, reified F, reified G, reified H, reified RR : Any>
            ((A, B, C, D, E, F, G, H) -> RR).get(a: A = any(), b: B = any(), c: C = any(), d: D = any(), e: E = any(), f: F = any(), g: G = any(), h: H = any()): RR {
        val params = getParameters(type, A::class, B::class, C::class, D::class, E::class, F::class, G::class, H::class)
        return invoke(
                a or new<A>(params[0], token),
                b or new<B>(params[1], token),
                c or new<C>(params[2], token),
                d or new<D>(params[3], token),
                e or new<E>(params[4], token),
                f or new<F>(params[5], token),
                g or new<G>(params[6], token),
                h or new<H>(params[7], token)
        )
    }

    operator inline fun <reified A, reified B, reified C, reified D, reified E, reified F, reified G, reified H, reified I, reified J, reified RR : Any>
            ((A, B, C, D, E, F, G, H, I, J) -> RR).get(a: A = any(), b: B = any(), c: C = any(), d: D = any(), e: E = any(), f: F = any(), g: G = any(), h: H = any(), i: I = any(), j: J = any()): RR {
        val params = getParameters(type, A::class, B::class, C::class, D::class, E::class, F::class, G::class, H::class, I::class, J::class)
        return invoke(
                a or new<A>(params[0], token),
                b or new<B>(params[1], token),
                c or new<C>(params[2], token),
                d or new<D>(params[3], token),
                e or new<E>(params[4], token),
                f or new<F>(params[5], token),
                g or new<G>(params[6], token),
                h or new<H>(params[7], token),
                i or new<I>(params[8], token),
                j or new<J>(params[9], token)
        )
    }

    operator inline fun <reified A, reified B, reified C, reified D, reified E, reified F, reified G, reified H, reified I, reified J, reified K, reified RR : Any>
            ((A, B, C, D, E, F, G, H, I, J, K) -> RR).get(a: A = any(), b: B = any(), c: C = any(), d: D = any(), e: E = any(), f: F = any(), g: G = any(), h: H = any(), i: I = any(), j: J = any(), k: K = any()): RR {
        val params = getParameters(type, A::class, B::class, C::class, D::class, E::class, F::class, G::class, H::class, I::class, J::class, K::class)
        return invoke(
                a or new<A>(params[0], token),
                b or new<B>(params[1], token),
                c or new<C>(params[2], token),
                d or new<D>(params[3], token),
                e or new<E>(params[4], token),
                f or new<F>(params[5], token),
                g or new<G>(params[6], token),
                h or new<H>(params[7], token),
                i or new<I>(params[8], token),
                j or new<J>(params[9], token),
                k or new<K>(params[10], token)
        )
    }

    operator inline fun <reified A, reified B, reified C, reified D, reified E, reified F, reified G, reified H, reified I, reified J, reified K, reified L, reified RR : Any>
            ((A, B, C, D, E, F, G, H, I, J, K, L) -> RR).get(a: A = any(), b: B = any(), c: C = any(), d: D = any(), e: E = any(), f: F = any(), g: G = any(), h: H = any(), i: I = any(), j: J = any(), k: K = any(), l: L = any()): RR {
        val params = getParameters(type, A::class, B::class, C::class, D::class, E::class, F::class, G::class, H::class, I::class, J::class, K::class, L::class)
        return invoke(
                a or new<A>(params[0], token),
                b or new<B>(params[1], token),
                c or new<C>(params[2], token),
                d or new<D>(params[3], token),
                e or new<E>(params[4], token),
                f or new<F>(params[5], token),
                g or new<G>(params[6], token),
                h or new<H>(params[7], token),
                i or new<I>(params[8], token),
                j or new<J>(params[9], token),
                k or new<K>(params[10], token),
                l or new<L>(params[11], token)
        )
    }

    operator inline fun <reified A, reified B, reified C, reified D, reified E, reified F, reified G, reified H, reified I, reified J, reified K, reified L, reified M, reified RR : Any>
            ((A, B, C, D, E, F, G, H, I, J, K, L, M) -> RR).get(a: A = any(), b: B = any(), c: C = any(), d: D = any(), e: E = any(), f: F = any(), g: G = any(), h: H = any(), i: I = any(), j: J = any(), k: K = any(), l: L = any(), m: M = any()): RR {
        val params = getParameters(type, A::class, B::class, C::class, D::class, E::class, F::class, G::class, H::class, I::class, J::class, K::class, L::class, M::class)
        return invoke(
                a or new<A>(params[0], token),
                b or new<B>(params[1], token),
                c or new<C>(params[2], token),
                d or new<D>(params[3], token),
                e or new<E>(params[4], token),
                f or new<F>(params[5], token),
                g or new<G>(params[6], token),
                h or new<H>(params[7], token),
                i or new<I>(params[8], token),
                j or new<J>(params[9], token),
                k or new<K>(params[10], token),
                l or new<L>(params[11], token),
                m or new<M>(params[12], token)
        )
    }

    operator inline fun <reified A, reified B, reified C, reified D, reified E, reified F, reified G, reified H, reified I, reified J, reified K, reified L, reified M, reified N, reified RR : Any>
            ((A, B, C, D, E, F, G, H, I, J, K, L, M, N) -> RR).get(a: A = any(), b: B = any(), c: C = any(), d: D = any(), e: E = any(), f: F = any(), g: G = any(), h: H = any(), i: I = any(), j: J = any(), k: K = any(), l: L = any(), m: M = any(), n: N = any()): RR {
        val params = getParameters(type, A::class, B::class, C::class, D::class, E::class, F::class, G::class, H::class, I::class, J::class, K::class, L::class, M::class, N::class)
        return invoke(
                a or new<A>(params[0], token),
                b or new<B>(params[1], token),
                c or new<C>(params[2], token),
                d or new<D>(params[3], token),
                e or new<E>(params[4], token),
                f or new<F>(params[5], token),
                g or new<G>(params[6], token),
                h or new<H>(params[7], token),
                i or new<I>(params[8], token),
                j or new<J>(params[9], token),
                k or new<K>(params[10], token),
                l or new<L>(params[11], token),
                m or new<M>(params[12], token),
                n or new<N>(params[13], token)
        )
    }

    operator inline fun <reified A, reified B, reified C, reified D, reified E, reified F, reified G, reified H, reified I, reified J, reified K, reified L, reified M, reified N, reified O, reified RR : Any>
            ((A, B, C, D, E, F, G, H, I, J, K, L, M, N, O) -> RR).get(a: A = any(), b: B = any(), c: C = any(), d: D = any(), e: E = any(), f: F = any(), g: G = any(), h: H = any(), i: I = any(), j: J = any(), k: K = any(), l: L = any(), m: M = any(), n: N = any(), o: O = any()): RR {
        val params = getParameters(type, A::class, B::class, C::class, D::class, E::class, F::class, G::class, H::class, I::class, J::class, K::class, L::class, M::class, N::class, O::class)
        return invoke(
                a or new<A>(params[0], token),
                b or new<B>(params[1], token),
                c or new<C>(params[2], token),
                d or new<D>(params[3], token),
                e or new<E>(params[4], token),
                f or new<F>(params[5], token),
                g or new<G>(params[6], token),
                h or new<H>(params[7], token),
                i or new<I>(params[8], token),
                j or new<J>(params[9], token),
                k or new<K>(params[10], token),
                l or new<L>(params[11], token),
                m or new<M>(params[12], token),
                n or new<N>(params[13], token),
                o or new<O>(params[14], token)
        )
    }

    operator inline fun <reified A, reified B, reified C, reified D, reified E, reified F, reified G, reified H, reified I, reified J, reified K, reified L, reified M, reified N, reified O, reified P, reified RR : Any>
            ((A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P) -> RR).get(a: A = any(), b: B = any(), c: C = any(), d: D = any(), e: E = any(), f: F = any(), g: G = any(), h: H = any(), i: I = any(), j: J = any(), k: K = any(), l: L = any(), m: M = any(), n: N = any(), o: O = any(), p: P = any()): RR {
        val params = getParameters(type, A::class, B::class, C::class, D::class, E::class, F::class, G::class, H::class, I::class, J::class, K::class, L::class, M::class, N::class, O::class, P::class)
        return invoke(
                a or new<A>(params[0], token),
                b or new<B>(params[1], token),
                c or new<C>(params[2], token),
                d or new<D>(params[3], token),
                e or new<E>(params[4], token),
                f or new<F>(params[5], token),
                g or new<G>(params[6], token),
                h or new<H>(params[7], token),
                i or new<I>(params[8], token),
                j or new<J>(params[9], token),
                k or new<K>(params[10], token),
                l or new<L>(params[11], token),
                m or new<M>(params[12], token),
                n or new<N>(params[13], token),
                o or new<O>(params[14], token),
                p or new<P>(params[15], token)
        )
    }

    operator inline fun <reified A, reified B, reified C, reified D, reified E, reified F, reified G, reified H, reified I, reified J, reified K, reified L, reified M, reified N, reified O, reified P, reified Q, reified RR : Any>
            ((A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q) -> RR).get(a: A = any(), b: B = any(), c: C = any(), d: D = any(), e: E = any(), f: F = any(), g: G = any(), h: H = any(), i: I = any(), j: J = any(), k: K = any(), l: L = any(), m: M = any(), n: N = any(), o: O = any(), p: P = any(), q: Q = any()): RR {
        val params = getParameters(type, A::class, B::class, C::class, D::class, E::class, F::class, G::class, H::class, I::class, J::class, K::class, L::class, M::class, N::class, O::class, P::class, Q::class)
        return invoke(
                a or new<A>(params[0], token),
                b or new<B>(params[1], token),
                c or new<C>(params[2], token),
                d or new<D>(params[3], token),
                e or new<E>(params[4], token),
                f or new<F>(params[5], token),
                g or new<G>(params[6], token),
                h or new<H>(params[7], token),
                i or new<I>(params[8], token),
                j or new<J>(params[9], token),
                k or new<K>(params[10], token),
                l or new<L>(params[11], token),
                m or new<M>(params[12], token),
                n or new<N>(params[13], token),
                o or new<O>(params[14], token),
                p or new<P>(params[15], token),
                q or new<Q>(params[16], token)
        )
    }

    operator inline fun <reified A, reified B, reified C, reified D, reified E, reified F, reified G, reified H, reified I, reified J, reified K, reified L, reified M, reified N, reified O, reified P, reified Q, reified R, reified RR : Any>
            ((A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R) -> RR).get(a: A = any(), b: B = any(), c: C = any(), d: D = any(), e: E = any(), f: F = any(), g: G = any(), h: H = any(), i: I = any(), j: J = any(), k: K = any(), l: L = any(), m: M = any(), n: N = any(), o: O = any(), p: P = any(), q: Q = any(), r: R = any()): RR {
        val params = getParameters(type, A::class, B::class, C::class, D::class, E::class, F::class, G::class, H::class, I::class, J::class, K::class, L::class, M::class, N::class, O::class, P::class, Q::class, R::class)
        return invoke(
                a or new<A>(params[0], token),
                b or new<B>(params[1], token),
                c or new<C>(params[2], token),
                d or new<D>(params[3], token),
                e or new<E>(params[4], token),
                f or new<F>(params[5], token),
                g or new<G>(params[6], token),
                h or new<H>(params[7], token),
                i or new<I>(params[8], token),
                j or new<J>(params[9], token),
                k or new<K>(params[10], token),
                l or new<L>(params[11], token),
                m or new<M>(params[12], token),
                n or new<N>(params[13], token),
                o or new<O>(params[14], token),
                p or new<P>(params[15], token),
                q or new<Q>(params[16], token),
                r or new<R>(params[17], token)
        )
    }


    operator inline fun <reified A, reified B, reified C, reified D, reified E, reified F, reified G, reified H, reified I, reified J, reified K, reified L, reified M, reified N, reified O, reified P, reified Q, reified R, reified S, reified RR : Any>
            ((A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S) -> RR).get(a: A = any(), b: B = any(), c: C = any(), d: D = any(), e: E = any(), f: F = any(), g: G = any(), h: H = any(), i: I = any(), j: J = any(), k: K = any(), l: L = any(), m: M = any(), n: N = any(), o: O = any(), p: P = any(), q: Q = any(), r: R = any(), s: S = any()): RR {
        val params = getParameters(type, A::class, B::class, C::class, D::class, E::class, F::class, G::class, H::class, I::class, J::class, K::class, L::class, M::class, N::class, O::class, P::class, Q::class, R::class, S::class)
        return invoke(
                a or new<A>(params[0], token),
                b or new<B>(params[1], token),
                c or new<C>(params[2], token),
                d or new<D>(params[3], token),
                e or new<E>(params[4], token),
                f or new<F>(params[5], token),
                g or new<G>(params[6], token),
                h or new<H>(params[7], token),
                i or new<I>(params[8], token),
                j or new<J>(params[9], token),
                k or new<K>(params[10], token),
                l or new<L>(params[11], token),
                m or new<M>(params[12], token),
                n or new<N>(params[13], token),
                o or new<O>(params[14], token),
                p or new<P>(params[15], token),
                q or new<Q>(params[16], token),
                r or new<R>(params[17], token),
                s or new<S>(params[18], token)
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
