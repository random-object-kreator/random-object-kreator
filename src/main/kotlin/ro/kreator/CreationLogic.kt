package ro.kreator

import org.apache.commons.lang3.RandomStringUtils
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import java.io.File
import java.lang.reflect.Array.newInstance
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.lang.reflect.TypeVariable
import java.security.MessageDigest
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.internal.ReflectProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.jvmErasure

typealias Token = Long

internal object CreationLogic : Reify() {
    init {
        Seed.seed
        fun list(type: KType, nonEmpty: Boolean, token: Token, past: Set<KClass<*>>) = aList(type.arguments.first().type!!, token, past.plus(type.jvmErasure), nonEmpty = nonEmpty)
        fun <T : Any> list(klass: KClass<T>, nonEmpty: Boolean, token: Token, past: Set<KClass<*>>): List<T> = aList(klass.createType(), token, past, nonEmpty = nonEmpty) as List<T>
        fun map(type: KType, nonEmpty: Boolean, token: Token, past: Set<KClass<*>>) = list(type, nonEmpty, token, past)
                .map { Pair(it, instantiateRandomClass(type.arguments[1].type!!, token, nonEmpty = nonEmpty)) }.toMap()

        val o = ObjectFactory

        o[kotlin.String::class().type] = { _, _, nonEmpty, token -> aString(nonEmpty, token) }
        o[kotlin.String::class().type] = { _, _, nonEmpty, token -> aString(nonEmpty, token) }
        o[kotlin.Byte::class().type] = { _, _, _, token -> aByte(token) }
        o[kotlin.Int::class().type] = { _, _, _, token -> anInt(token) }
        o[kotlin.Long::class().type] = { _, _, _,token -> aLong(token) }
        o[kotlin.Double::class().type] = { _, _, _,token -> aDouble(token) }
        o[kotlin.Short::class().type] = { _, _, _,token -> aShort(token) }
        o[kotlin.Float::class().type] = { _, _, _,token -> aFloat(token) }
        o[kotlin.Boolean::class().type] = { _, _, _,token -> aBoolean(token) }
        o[kotlin.Char::class().type] = { _, _, _,token -> aChar(token) }
        o[IntArray::class(Int::class()).type] = { _, past, nonEmpty, token -> list(Int::class, nonEmpty, token,past).toIntArray() }
        o[Array<Int>::class(Int::class()).type] = { _, past, nonEmpty, token -> list(Int::class, nonEmpty, token,past).toTypedArray() }
        o[ShortArray::class(Short::class()).type] = { _, past, nonEmpty, token -> list(Short::class, nonEmpty, token,past).toShortArray() }
        o[Array<Short>::class(Short::class()).type] = { _, past, nonEmpty, token -> list(Short::class, nonEmpty, token,past).toTypedArray() }
        o[LongArray::class(Long::class()).type] = { _, past, nonEmpty, token -> list(Long::class, nonEmpty, token,past).toLongArray() }
        o[Array<Long>::class(Long::class()).type] = { _, past, nonEmpty, token -> list(Long::class, nonEmpty, token,past).toTypedArray() }
        o[FloatArray::class(Float::class()).type] = { _, past, nonEmpty, token -> list(Float::class, nonEmpty, token,past).toFloatArray() }
        o[Array<Float>::class(Float::class()).type] = { _, past, nonEmpty, token -> list(Float::class, nonEmpty, token,past).toTypedArray() }
        o[DoubleArray::class(Double::class()).type] = { _, past, nonEmpty, token -> list(Double::class, nonEmpty, token,past).toDoubleArray() }
        o[Array<Double>::class(Double::class()).type] = { _, past, nonEmpty, token -> list(Double::class, nonEmpty, token,past).toTypedArray() }
        o[BooleanArray::class(Boolean::class()).type] = { _, past, nonEmpty, token -> list(Boolean::class, nonEmpty, token,past).toBooleanArray() }
        o[Array<Boolean>::class(Boolean::class()).type] = { _, past, nonEmpty, token -> list(Boolean::class, nonEmpty, token,past).toTypedArray() }
        o[ByteArray::class(Byte::class()).type] = { _, past, nonEmpty, token -> list(Byte::class, nonEmpty, token,past).toByteArray() }
        o[Array<Byte>::class(Byte::class()).type] = { _, past, nonEmpty, token -> list(Byte::class, nonEmpty, token,past).toTypedArray() }
        o[CharArray::class(Char::class()).type] = { _, past, nonEmpty, token -> list(Char::class, nonEmpty, token,past).toCharArray() }
        o[Array<Char>::class(Char::class()).type] = { _, past, nonEmpty, token -> list(Char::class, nonEmpty, token,past).toTypedArray() }

        o[List::class.starProjectedType] = { type, past, nonEmpty, token -> list(type, nonEmpty, token, past) }
        o[Set::class.starProjectedType] = { type, past, nonEmpty, token -> list(type, nonEmpty, token, past).toSet() }
        o[kotlin.collections.Map::class.starProjectedType] = { type, past, nonEmpty, token -> map(type, nonEmpty, token, past) }

        o[File::class.starProjectedType] = { _, _, _, token -> File(aString(true, token)) }
        o[Date::class.starProjectedType] = {_, _, _,token -> Date(aLong(token)) }
    }

    internal object ObjectFactory {
        private val objectFactories = mutableMapOf<KType, (KType, Set<KClass<*>>, Boolean, Token) -> Any>()

        operator fun set(type: KType, factory: (KType, Set<KClass<*>>, Boolean, Token) -> Any): ObjectFactory {
            objectFactories[type] = factory
            return this
        }

        operator fun get(type: KType): ((KType, Set<KClass<*>>, Boolean, Token) -> Any)? {
            return objectFactories[type] ?: objectFactories[type.jvmErasure.starProjectedType]
        }

        operator fun contains(type: KType): Boolean {
            return get(type)?.let { true } ?: false
        }
    }

    private val maxChar = 59319
    private val maxStringLength = 20

    private fun aChar(token: Long): Char = pseudoRandom(token).nextInt(maxChar).toChar()
    private fun anInt(token: Long, max: Int? = null): Int = max?.let { pseudoRandom(token).nextInt(it) } ?: pseudoRandom(token).nextInt()
    private fun aLong(token: Long): Long = pseudoRandom(token).nextLong()
    private fun aDouble(token: Long): Double = pseudoRandom(token).nextDouble()
    private fun aShort(token: Long): Short = pseudoRandom(token).nextInt(Short.MAX_VALUE.toInt()).toShort()
    private fun aFloat(token: Long): Float = pseudoRandom(token).nextFloat()
    private fun aByte(token: Long): Byte = pseudoRandom(token).nextInt(255).toByte()
    private fun aBoolean(token: Long): Boolean = pseudoRandom(token).nextBoolean()
    private fun aString(nonEmpty: Boolean = false, token: Long): String = pseudoRandom(token).let {
        RandomStringUtils.random(Math.max(if (nonEmpty) 1 else 0, it.nextInt(maxStringLength)), 0, maxChar, true, true, null, it)
    }

    private val md = MessageDigest.getInstance("MD5")

    internal val Any.hash: Long get() {
        val array = md.digest(toString().toByteArray())

        var hash = 7L
        for (i in array) {
            hash = hash * 31 + i.toLong()
        }
        return hash
    }

    internal infix fun Long.with(other: Long): Long {
        return this * 31 + other
    }

    internal fun aList(type: KType, token: Long, parentClasses: Set<KClass<*>>, size: Int? = null, nonEmpty: Boolean): List<*> {
        val klass = type.jvmErasure

        parentClasses.shouldNotContain(klass)

        val maxSize = size ?: pseudoRandom(token).nextInt(5)

        val items = if (nonEmpty) 0 .. maxSize else 0 until maxSize

        return items.map {
            if (klass == List::class) {
                aList(type.arguments.first().type!!, token.hash with it.hash, parentClasses, nonEmpty = nonEmpty)
            } else instantiateRandomClass(type, token.hash with it.hash, parentClasses, nonEmpty)
        }
    }

    internal fun instantiateRandomClass(
            type: KType, token: Token = 0, parentClasses: Set<KClass<*>> = emptySet(),
            nonEmpty: Boolean
            ): Any? {
        val klass = type.jvmErasure
        parentClasses.shouldNotContain(klass)

        fun KClass<out Any>.isAnInterfaceOrSealed() = this.java.isInterface || this.isSealed
        fun KClass<out Any>.isAnArray() = this.java.isArray
        fun KClass<out Any>.isAnEnum() = this.java.isEnum
        fun KClass<out Any>.isAnObject() = this.objectInstance != null
        fun thereIsACustomFactory() = type in ObjectFactory
        fun isNullable(): Boolean = type.isMarkedNullable && pseudoRandom(token).nextInt() % 2 == 0

        return when {
            isNullable() -> null
            thereIsACustomFactory() -> ObjectFactory[type]?.invoke(type, parentClasses, nonEmpty, token)
            klass.isAnObject() -> klass.objectInstance
            klass.isAnEnum() -> klass.java.enumConstants[anInt(token, max = klass.java.enumConstants.size)]
            klass.isAnArray() -> instantiateArray(type, token, parentClasses, klass, nonEmpty)
            klass.isAnInterfaceOrSealed() -> instantiateInterface(type, nonEmpty, token, parentClasses)
            else -> instantiateArbitraryClass(klass, token, type, parentClasses, nonEmpty)
        }
    }

    private fun instantiateArray(type: KType, token: Token, past: Set<KClass<*>>, klass: KClass<out Any>, nonEmpty: Boolean): Array<Any?> {
        val genericType = type.arguments.first().type!!
        val list = aList(genericType, token, past.plus(klass), nonEmpty = nonEmpty)
        val array = newInstance(genericType.jvmErasure!!.java, list.size) as Array<Any?>
        return array.apply { list.forEachIndexed { index, any -> array[index] = any } }
    }

    private fun instantiateInterface(type: KType, nonEmpty: Boolean, token: Token, past: Set<KClass<*>>): Any {
        val allClassesInModule = if (classes.isEmpty())
            Reflections("", SubTypesScanner(false)).getSubTypesOf(Any::class.java).apply { classes.addAll(this) }
        else classes

        val klass = type.jvmErasure

        val allImplementationsInModule = classesMap[klass] ?: allClassesInModule
                .filter { klass.java != it && klass.java.isAssignableFrom(it) }
                .apply { classesMap.put(klass, this) }

        return allImplementationsInModule.getOrNull(pseudoRandom(token).int(allImplementationsInModule.size))
                ?.let {
                    val params = it.kotlin.typeParameters.map { KTypeProjection(it.variance, it.starProjectedType) }
                    instantiateRandomClass(it.kotlin.createType(params), token.hash with it.name.hash, nonEmpty = nonEmpty)
                }
                ?: instantiateNewInterface(type, nonEmpty, token, past)
    }

    private fun instantiateNewInterface(type: KType, nonEmpty: Boolean, token: Token, past: Set<KClass<*>>): Any {

        val klass = type.jvmErasure
        val genericTypeNameToConcreteTypeMap = klass.typeParameters.map { it.name }.zip(type.arguments).toMap()

        fun degenerify(kType: KType): KType {
            return if (kType.arguments.isEmpty()) genericTypeNameToConcreteTypeMap[kType.javaType.typeName]?.type ?: kType
            else {
                val argumentField = kType::class.java.declaredFields.find { it.name == "${kType::arguments.name}\$delegate" }!!
                argumentField.isAccessible = true
                val degenerifiedArguments = kType.arguments.map { KTypeProjection(it.variance, degenerify(it.type!!))}
                val newFieldValue = ReflectProperties.lazySoft { degenerifiedArguments  }
                argumentField.set(kType, newFieldValue)
                kType
            }
        }

        val javaMethods: Array<Method> = klass.java.methods + Any::class.java.methods

        val methodReturnTypes = javaMethods.map { method ->
            val returnType = klass.members.find { member ->
                fun hasNameName(): Boolean = (method.name == member.name || method.name == "get${member.name.capitalize()}")
                fun hasSameArguments() = method.parameters.map { it.parameterizedType } == member.valueParameters.map { it.type.javaType }
                hasNameName() && hasSameArguments()
            }?.returnType?.let { degenerify(it) }

            val type1 = genericTypeNameToConcreteTypeMap[returnType?.jvmErasure?.simpleName]?.type
            method to (type1 ?: returnType)
        }.toMap()

        return Proxy.newProxyInstance(klass.java.classLoader, arrayOf(klass.java)) { proxy, method, obj ->
            when (method.name) {
                Any::hashCode.javaMethod?.name -> proxy.toString().hashCode()
                Any::equals.javaMethod?.name -> proxy.toString() == obj[0].toString()
                Any::toString.javaMethod?.name -> "\$RandomImplementation$${klass.simpleName}"
                else -> methodReturnTypes[method]?.let { instantiateRandomClass(it, token, past, nonEmpty) } ?:
                        instantiateRandomClass(method.returnType.kotlin.createType().print(), token, past, nonEmpty)
            }
        }
    }

    private fun Set<KClass<*>>.shouldNotContain(klass: KClass<*>) {
        if (isAllowedCyclic(klass) && this.contains(klass)) throw CyclicException()
    }

    private fun instantiateArbitraryClass(klass: KClass<out Any>, token: Token, type: KType, past: Set<KClass<*>>, nonEmpty: Boolean): Any? {
        val constructors = klass.constructors.filter { !it.parameters.any { (it.type.jvmErasure == klass) } }.toList()
        if (constructors.isEmpty() && klass.constructors.any { it.parameters.any { (it.type.jvmErasure == klass) } }) throw CyclicException()
        val defaultConstructor = constructors[pseudoRandom(token).int(constructors.size)] as KFunction<*>
        defaultConstructor.isAccessible = true
        val constructorTypeParameters = defaultConstructor.valueParameters.map { it.type.toString().replace("!", "").replace("?", "") }.toMutableList()
        val typeMap = type.jvmErasure.typeParameters.map { it.name }.zip(type.arguments).toMap()
        val pairedConstructor = defaultConstructor.parameters.map { if (it.type.javaType is TypeVariable<*>) constructorTypeParameters.get(it.index) to it else "" to it }
        val parameters = (pairedConstructor.map {
            fun isTypeVariable() = it.second.type.javaType is TypeVariable<*>
            val tpe = if (isTypeVariable()) typeMap[it.first]?.type ?: it.second.type else it.second.type
            instantiateRandomClass(tpe, token.hash with tpe.jvmErasure.hash with it.second.hash, past.plus(klass), nonEmpty)
        }).toTypedArray()
        try {
            val res = defaultConstructor.call(*parameters)
            return res
        } catch (e: Throwable) {
            val namedParameters = parameters.zip(defaultConstructor.parameters.map { it.name }).map { "${it.second}=${it.first}" }
            throw CreationException("""Something went wrong when trying to instantiate class ${klass}
         using constructor: $defaultConstructor
         with values: $namedParameters""", e.cause)
        }
    }

    private fun Random.int(bound: Int) = if (bound == 0) 0 else nextInt(bound)
    private fun isAllowedCyclic(klass: KClass<out Any>) = klass != List::class && klass != Set::class && klass != Map::class && !klass.java.isArray
    private val classes: MutableSet<Class<out Any>> = mutableSetOf()
    private val classesMap: MutableMap<KClass<out Any>, List<Class<out Any>>> = mutableMapOf()
    private fun pseudoRandom(token: Long): Random = Random(Seed.seed with token)
}