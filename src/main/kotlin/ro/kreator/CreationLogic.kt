package ro.kreator

import javassist.util.proxy.ProxyFactory
import javassist.util.proxy.ProxyObject
import org.apache.commons.lang3.RandomStringUtils
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.FilterBuilder
import java.io.File
import java.lang.reflect.Array.newInstance
import java.lang.reflect.Method
import java.lang.reflect.TypeVariable
import java.security.MessageDigest
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.createType
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.*
import kotlin.reflect.jvm.internal.ReflectProperties



typealias Token = Long

internal object CreationLogic : Reify() {
    init {
        Seed.seed
        fun list(type: KType, token: Token, past: Set<KClass<*>>) = aList(type.arguments.first().type!!, token, past.plus(type.jvmErasure))
        fun <T : Any> list(klass: KClass<T>, token: Token, past: Set<KClass<*>>): List<T> = aList(klass.createType(), token, past) as List<T>
        fun map(type: KType, token: Token, past: Set<KClass<*>>) = list(type, token, past)
                .map { Pair(it, instantiateRandomClass(type.arguments[1].type!!, token)) }.toMap()

        val o = ObjectFactory

        o[kotlin.String::class().type] = { _, _, token -> aString(token) }
        o[kotlin.String::class().type] = { _, _, token -> aString(token) }
        o[kotlin.Byte::class().type] = { _, _, token -> aByte(token) }
        o[kotlin.Int::class().type] = { _, _, token -> anInt(token) }
        o[kotlin.Long::class().type] = { _, _, token -> aLong(token) }
        o[kotlin.Double::class().type] = { _, _, token -> aDouble(token) }
        o[kotlin.Short::class().type] = { _, _, token -> aShort(token) }
        o[kotlin.Float::class().type] = { _, _, token -> aFloat(token) }
        o[kotlin.Boolean::class().type] = { _, _, token -> aBoolean(token) }
        o[kotlin.Char::class().type] = { _, _, token -> aChar(token) }
        o[IntArray::class(Int::class()).type] = { _, past, token -> list(Int::class, token, past).toIntArray() }
        o[Array<Int>::class(Int::class()).type] = { _, past, token -> list(Int::class, token, past).toTypedArray() }
        o[ShortArray::class(Short::class()).type] = { _, past, token -> list(Short::class, token, past).toShortArray() }
        o[Array<Short>::class(Short::class()).type] = { _, past, token -> list(Short::class, token, past).toTypedArray() }
        o[LongArray::class(Long::class()).type] = { _, past, token -> list(Long::class, token, past).toLongArray() }
        o[Array<Long>::class(Long::class()).type] = { _, past, token -> list(Long::class, token, past).toTypedArray() }
        o[FloatArray::class(Float::class()).type] = { _, past, token -> list(Float::class, token, past).toFloatArray() }
        o[Array<Float>::class(Float::class()).type] = { _, past, token -> list(Float::class, token, past).toTypedArray() }
        o[DoubleArray::class(Double::class()).type] = { _, past, token -> list(Double::class, token, past).toDoubleArray() }
        o[Array<Double>::class(Double::class()).type] = { _, past, token -> list(Double::class, token, past).toTypedArray() }
        o[BooleanArray::class(Boolean::class()).type] = { _, past, token -> list(Boolean::class, token, past).toBooleanArray() }
        o[Array<Boolean>::class(Boolean::class()).type] = { _, past, token -> list(Boolean::class, token, past).toTypedArray() }
        o[ByteArray::class(Byte::class()).type] = { _, past, token -> list(Byte::class, token, past).toByteArray() }
        o[Array<Byte>::class(Byte::class()).type] = { _, past, token -> list(Byte::class, token, past).toTypedArray() }
        o[CharArray::class(Char::class()).type] = { _, past, token -> list(Char::class, token, past).toCharArray() }
        o[Array<Char>::class(Char::class()).type] = { _, past, token -> list(Char::class, token, past).toTypedArray() }

        o[List::class.starProjectedType] = { type, past, token -> list(type, token, past) }
        o[Set::class.starProjectedType] = { type, past, token -> list(type, token, past).toSet() }
        o[kotlin.collections.Map::class.starProjectedType] = { type, past, token -> map(type, token, past) }

        o[File::class.starProjectedType] = { _, _, token -> File(aString(token)) }
        o[Date::class.starProjectedType] = {_, _, token -> Date(aLong(token)) }
    }

    internal object ObjectFactory {
        private val objectFactories = mutableMapOf<KType, (KType, Set<KClass<*>>, Token) -> Any>()

        operator fun set(type: KType, factory: (KType, Set<KClass<*>>, Token) -> Any): ObjectFactory {
            objectFactories[type] = factory
            return this
        }

        operator fun get(type: KType): ((KType, Set<KClass<*>>, Token) -> Any)? {
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
    private fun aString(token: Long): String = pseudoRandom(token).let {
        RandomStringUtils.random(Math.max(1, it.nextInt(maxStringLength)), 0, maxChar, true, true, null, it)
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

    internal fun aList(type: KType, token: Long, parentClasses: Set<KClass<*>>,
                       size: Int? = null,
                       minSize: Int = 1,
                       maxSize: Int = 5): List<*> {
        val klass = type.jvmErasure

        parentClasses.shouldNotContain(klass)

        val items = 0..(size ?: (pseudoRandom(token).nextInt(maxSize-minSize) + minSize))

        return items.map {
            if (klass == List::class) {
                aList(type.arguments.first().type!!, token.hash with it.hash, parentClasses)
            } else instantiateRandomClass(type, token.hash with it.hash, parentClasses)
        }
    }

    internal fun instantiateRandomClass(type: KType, token: Token = 0, parentClasses: Set<KClass<*>> = emptySet()): Any? {
        val klass = type.jvmErasure
        parentClasses.shouldNotContain(klass)

        fun KClass<out Any>.isAnInterfaceOrSealed() = this.java.isInterface || this.isSealed || this.isAbstract
        fun KClass<out Any>.isAnArray() = this.java.isArray
        fun KClass<out Any>.isAnEnum() = this.java.isEnum
        fun KClass<out Any>.isAnObject() = this.objectInstance != null
        fun thereIsACustomFactory() = type in ObjectFactory
        fun isNullable(): Boolean = type.isMarkedNullable && pseudoRandom(token).nextInt() % 2 == 0

        return when {
            isNullable() -> null
            thereIsACustomFactory() -> ObjectFactory[type]?.invoke(type, parentClasses, token)
            klass.isAnObject() -> klass.objectInstance
            klass.isAnEnum() -> klass.java.enumConstants[anInt(token, max = klass.java.enumConstants.size)]
            klass.isAnArray() -> instantiateArray(type, token, parentClasses, klass)
            klass.isAnInterfaceOrSealed() -> instantiateAbstract(type, token, parentClasses)
            else -> instantiateArbitraryClass(klass, token, type, parentClasses)
        }
    }

    private fun instantiateArray(type: KType, token: Token, past: Set<KClass<*>>, klass: KClass<out Any>): Array<Any?> {
        val genericType = type.arguments.first().type!!
        val list = aList(genericType, token, past.plus(klass))
        val array = newInstance(genericType.jvmErasure!!.java, list.size) as Array<Any?>
        return array.apply { list.forEachIndexed { index, any -> array[index] = any } }
    }

    private fun instantiateAbstract(type: KType, token: Token, past: Set<KClass<*>>): Any {
        val allClassesInModule = if (classes.isEmpty()) {
            getAndCacheClasses(type)
        } else classes

        val klass = type.jvmErasure

        val allImplementationsInModule = classesMap[klass] ?: allClassesInModule
                .filter { klass.java != it && klass.java.isAssignableFrom(it) }
                .let { if (it.isEmpty()) getAndCacheClasses(type)
                        .filter { klass.java != it && klass.java.isAssignableFrom(it) }
                    else it
                }
                .apply { classesMap.put(klass, this) }

        return allImplementationsInModule.getOrNull(pseudoRandom(token).int(allImplementationsInModule.size))
                ?.let {
                    val params = it.kotlin.typeParameters.map { KTypeProjection(it.variance, it.starProjectedType) }
                    instantiateRandomClass(it.kotlin.createType(params), token.hash with it.name.hash)
                }
                ?: instantiateNewInterface(type, token, past)
    }

    private fun getAndCacheClasses(type: KType): Set<Class<out Any>> {
        val packages = (type.jvmErasure.allSuperclasses + type.jvmErasure).map { it.qualifiedName?.split(".")?.take(2)?.joinToString(".") }
        val reflections = Reflections(ConfigurationBuilder()
                .setScanners(SubTypesScanner(false))
                .setUrls(ClasspathHelper.forClassLoader())
                .filterInputsBy(FilterBuilder().includePackage(*packages.toTypedArray())))
        return reflections.getSubTypesOf(Any::class.java).apply { classes.addAll(this) }
    }

    private fun instantiateNewInterface(type: KType, token: Token, past: Set<KClass<*>>): Any {

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
                fun hasSameArguments() = method.parameterTypes.map { it.name } == member.valueParameters.map { it.type.jvmErasure.jvmName }
                hasNameName() && hasSameArguments()
            }?.returnType?.let { degenerify(it) }

            val type1 = genericTypeNameToConcreteTypeMap[returnType?.jvmErasure?.simpleName]?.type
            method to (type1 ?: returnType)
        }.toMap()

        val factory = ProxyFactory()
        if (klass.java.isInterface) {
            factory.interfaces = arrayOf(klass.java)
        } else {
            factory.superclass = klass.java
        }
        val proxy = factory.createClass().newInstance()

        (proxy as ProxyObject).setHandler({ proxy, method, proceed, obj ->
            when (method.name) {
                Any::hashCode.javaMethod?.name -> proxy.toString().hashCode()
                Any::equals.javaMethod?.name -> proxy.toString() == obj[0].toString()
                Any::toString.javaMethod?.name -> "\$RandomImplementation$${klass.simpleName}"
                else -> methodReturnTypes[method]?.let { instantiateRandomClass(it, token, past) } ?:
                        instantiateRandomClass(method.returnType.kotlin.createType(), token, past)
            }
        })
        return proxy
    }

    private fun Set<KClass<*>>.shouldNotContain(klass: KClass<*>) {
        if (isAllowedCyclic(klass) && this.contains(klass)) throw CyclicException()
    }

    private fun instantiateArbitraryClass(klass: KClass<out Any>, token: Token, type: KType, past: Set<KClass<*>>): Any? {
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
            instantiateRandomClass(tpe, token.hash with tpe.jvmErasure.hash with it.second.hash, past.plus(klass))
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