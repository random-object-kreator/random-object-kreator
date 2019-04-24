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
import java.lang.reflect.Array.*
import java.lang.reflect.Method
import java.lang.reflect.TypeVariable
import java.security.MessageDigest
import java.util.*
import java.util.Collections.*
import kotlin.collections.set
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.createType
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.internal.ReflectProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.jvmName


typealias Token = Long

internal object CreationLogic : Reify() {
    init {
        Seed.seed
        fun list(type: KType, kProperty: KProperty<*>?, token: Token, past: Set<KClass<*>>) = aList(type.arguments.first().type!!, token, past.plus(type.jvmErasure), kProperty)
        fun <T : Any> list(klass: KClass<T>, kProperty: KProperty<*>?, token: Token, past: Set<KClass<*>>): List<T> = aList(klass.createType(), token, past, kProperty) as List<T>
        fun map(type: KType, kProperty: KProperty<*>?, token: Token, past: Set<KClass<*>>) = list(type, kProperty, token, past)
                .map { Pair(it, instantiateRandomClass(type.arguments[1].type!!, kProperty, token)) }.toMap()

        val o = ObjectFactory

        o[kotlin.String::class().type] = { _, _, kproperty, token -> aString(token) }
        o[kotlin.String::class().type] = { _, _, kproperty, token -> aString(token) }
        o[kotlin.Byte::class().type] = { _, _, kproperty, token -> aByte(token) }
        o[kotlin.Int::class().type] = { _, _, kproperty, token -> anInt(token) }
        o[kotlin.Long::class().type] = { _, _, kproperty, token -> aLong(token) }
        o[kotlin.Double::class().type] = { _, _, kproperty, token -> aDouble(token) }
        o[kotlin.Short::class().type] = { _, _, kproperty, token -> aShort(token) }
        o[kotlin.Float::class().type] = { _, _, kproperty, token -> aFloat(token) }
        o[kotlin.Boolean::class().type] = { _, _, kproperty, token -> aBoolean(token) }
        o[kotlin.Char::class().type] = { _, _, kproperty, token -> aChar(token) }
        o[IntArray::class(Int::class()).type] = { _, past, kproperty, token -> list(Int::class, kproperty, token, past).toIntArray() }
        o[Array<Int>::class(Int::class()).type] = { _, past, kproperty, token -> list(Int::class, kproperty, token, past).toTypedArray() }
        o[ShortArray::class(Short::class()).type] = { _, past, kproperty, token -> list(Short::class, kproperty, token, past).toShortArray() }
        o[Array<Short>::class(Short::class()).type] = { _, past, kproperty, token -> list(Short::class, kproperty, token, past).toTypedArray() }
        o[LongArray::class(Long::class()).type] = { _, past, kproperty, token -> list(Long::class, kproperty, token, past).toLongArray() }
        o[Array<Long>::class(Long::class()).type] = { _, past, kproperty, token -> list(Long::class, kproperty, token, past).toTypedArray() }
        o[FloatArray::class(Float::class()).type] = { _, past, kproperty, token -> list(Float::class, kproperty, token, past).toFloatArray() }
        o[Array<Float>::class(Float::class()).type] = { _, past, kproperty, token -> list(Float::class, kproperty, token, past).toTypedArray() }
        o[DoubleArray::class(Double::class()).type] = { _, past, kproperty, token -> list(Double::class, kproperty, token, past).toDoubleArray() }
        o[Array<Double>::class(Double::class()).type] = { _, past, kproperty, token -> list(Double::class, kproperty, token, past).toTypedArray() }
        o[BooleanArray::class(Boolean::class()).type] = { _, past, kproperty, token -> list(Boolean::class, kproperty, token, past).toBooleanArray() }
        o[Array<Boolean>::class(Boolean::class()).type] = { _, past, kproperty, token -> list(Boolean::class, kproperty, token, past).toTypedArray() }
        o[ByteArray::class(Byte::class()).type] = { _, past, kproperty, token -> list(Byte::class, kproperty, token, past).toByteArray() }
        o[Array<Byte>::class(Byte::class()).type] = { _, past, kproperty, token -> list(Byte::class, kproperty, token, past).toTypedArray() }
        o[CharArray::class(Char::class()).type] = { _, past, kproperty, token -> list(Char::class, kproperty, token, past).toCharArray() }
        o[Array<Char>::class.invoke(Char::class()).type] = { _, past, kproperty, token -> list(Char::class, kproperty, token, past).toTypedArray() }

        o[List::class.starProjectedType] = { type, past, kproperty, token -> list(type, kproperty, token, past) }
        o[Set::class.starProjectedType] = { type, past, kproperty, token -> list(type, kproperty, token, past).toSet() }
        o[kotlin.collections.Map::class.starProjectedType] = { type, past, kproperty, token -> map(type, kproperty, token, past) }

        o[File::class.starProjectedType] = { _, _, kproperty, token -> File(aString(token)) }
        o[Date::class.starProjectedType] = { _, _, kproperty, token -> Date(aLong(token)) }
    }

    internal object ObjectFactory {
        private val objectFactories = mutableMapOf<KType, (KType, Set<KClass<*>>, KProperty<*>?, Token) -> Any?>()

        operator fun set(type: KType, factory: (KType, Set<KClass<*>>, KProperty<*>?, Token) -> Any?): ObjectFactory {
            objectFactories[type] = factory
            return this
        }

        fun putIfAbsent(type: KType, factory: (KType, Set<KClass<*>>, KProperty<*>?, Token) -> Any?) = objectFactories.putIfAbsent(type, factory)

        operator fun get(type: KType): ((KType, Set<KClass<*>>, KProperty<*>?, Token) -> Any?)? {
            return objectFactories[type]
                    ?: if (type.arguments.isNotEmpty()) objectFactories[type.jvmErasure.starProjectedType] else null
        }

        operator fun contains(type: KType): Boolean {
            return get(type)?.let { true } ?: false
        }
    }

    private val maxChar = 59319
    private val maxStringLength = 5

    private fun aChar(token: Long): Char = pseudoRandom(token).nextInt(maxChar).toChar()
    private fun anInt(token: Long, max: Int? = null): Int = max?.let { pseudoRandom(token).nextInt(it) }
            ?: pseudoRandom(token).nextInt()

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

    internal val Any.hash: Long
        get() {
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

    internal fun aList(type: KType,
                       token: Long, parentClasses: Set<KClass<*>>,
                       kProperty: KProperty<*>?,
                       size: Int? = null,
                       minSize: Int = 1,
                       maxSize: Int = 5): List<*> {
        val klass = type.jvmErasure

        parentClasses.shouldNotContain(klass)

        val items = 0..(size ?: (pseudoRandom(token).nextInt(maxSize - minSize) + minSize))

        return items.map {
            if (klass == List::class) {
                aList(type.arguments.first().type!!, token.hash with it.hash, parentClasses, kProperty)
            } else instantiateRandomClass(type, token.hash with it.hash, parentClasses, kProperty)
        }
    }

    internal fun instantiateRandomClass(type: KType, token: Token = 0, parentClasses: Set<KClass<*>> = emptySet(), kProperty: KProperty<*>?): Any? {
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
            thereIsACustomFactory() -> ObjectFactory[type]?.invoke(type, parentClasses, kProperty, token)
            klass.isAnEnum() -> klass.java.enumConstants[anInt(token, max = klass.java.enumConstants.size)]
            klass.isAnArray() -> instantiateArray(type, token, parentClasses, klass, kProperty)
            klass.isAnInterfaceOrSealed() -> instantiateAbstract(type, token, parentClasses, kProperty)
            klass.isAnObject() -> klass.objectInstance
            else -> instantiateArbitraryClass(klass, token, type, parentClasses, kProperty)
        }
    }

    private fun instantiateArray(type: KType, token: Token, past: Set<KClass<*>>, klass: KClass<out Any>, kProperty: KProperty<*>?): Array<Any?> {
        val genericType = type.arguments.first().type!!
        val list = aList(genericType, token, past.plus(klass), kProperty)
        val array = newInstance(genericType.jvmErasure!!.java, list.size) as Array<Any?>
        return array.apply { list.forEachIndexed { index, any -> array[index] = any } }
    }

    private fun instantiateAbstract(type: KType, token: Token, past: Set<KClass<*>>, kProperty: KProperty<*>?): Any {
        val allClassesInModule = if (classes.isEmpty()) {
            getAndCacheClasses(type)
        } else classes

        val klass = type.jvmErasure

        val allImplementationsInModule = classesMap[klass] ?: allClassesInModule
                .filter { klass.java != it && klass.java.isAssignableFrom(it) }
                .let {
                    if (it.isEmpty()) getAndCacheClasses(type)
                            .filter { klass.java != it && klass.java.isAssignableFrom(it) }
                    else it
                }
                .apply { classesMap.put(klass, this) }

        return allImplementationsInModule.getOrNull(pseudoRandom(token).int(allImplementationsInModule.size))
                ?.let {
                    val params = it.kotlin.typeParameters.map { KTypeProjection(it.variance, it.starProjectedType) }
                    instantiateRandomClass(it.kotlin.createType(params), kProperty, token.hash with it.name.hash)
                }
                ?: instantiateNewInterface(type, token, past, kProperty)
    }

    private fun getAndCacheClasses(type: KType): Set<Class<out Any>> {
        val packages = (type.jvmErasure.allSuperclasses + type.jvmErasure).map { it.qualifiedName?.split(".")?.take(2)?.joinToString(".") }
        val reflections = Reflections(ConfigurationBuilder()
                .setScanners(SubTypesScanner(false))
                .setUrls(ClasspathHelper.forClassLoader())
                .filterInputsBy(FilterBuilder().includePackage(*packages.toTypedArray())))
        return reflections.getSubTypesOf(Any::class.java).apply { classes.addAll(this) }
    }

    private fun instantiateNewInterface(type: KType, token: Token, past: Set<KClass<*>>, kProperty: KProperty<*>?): Any {

        val klass = type.jvmErasure
        val genericTypeNameToConcreteTypeMap = klass.typeParameters.map { it.name }.zip(type.arguments).toMap()

        fun degenerify(kType: KType): KType {
            return if (kType.arguments.isEmpty()) genericTypeNameToConcreteTypeMap[kType.javaType.typeName]?.type
                    ?: kType
            else {
                val argumentField = kType::class.java.declaredFields.find { it.name == "${kType::arguments.name}\$delegate" }!!
                argumentField.isAccessible = true
                val degenerifiedArguments = kType.arguments.map { KTypeProjection(it.variance, degenerify(it.type!!)) }
                val newFieldValue = ReflectProperties.lazySoft { degenerifiedArguments }
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
                else -> methodReturnTypes[method]?.let { instantiateRandomClass(it, token, past, kProperty) }
                        ?: instantiateRandomClass(method.returnType.kotlin.createType(), token, past, kProperty)
            }
        })
        return proxy
    }

    private fun Set<KClass<*>>.shouldNotContain(klass: KClass<*>) {
        if (isAllowedCyclic(klass) && this.contains(klass)) throw CyclicException()
    }

    private fun instantiateArbitraryClass(klass: KClass<out Any>, token: Token, type: KType, past: Set<KClass<*>>, kProperty: KProperty<*>?): Any? {
        val constructors = klass.constructors.filter { !it.parameters.any { (it.type.jvmErasure == klass) } }.toList()
        if (constructors.isEmpty() && klass.constructors.any { it.parameters.any { (it.type.jvmErasure == klass) } }) throw CyclicException()
        val defaultConstructor = constructors[pseudoRandom(token).int(constructors.size)] as KFunction<*>
        if (!defaultConstructor.isAccessible) {
            defaultConstructor.isAccessible = true
        }
        val constructorTypeParameters by lazy { defaultConstructor.valueParameters.map { it.type.toString().replace("!", "").replace("?", "") }.toMutableList() }
        val typeMap by lazy { type.jvmErasure.typeParameters.map { it.name }.zip(type.arguments).toMap() }
        val pairedConstructor = defaultConstructor.parameters.map { if (it.type.javaType is TypeVariable<*>) constructorTypeParameters.get(it.index) to it else "" to it }
        val parameters by lazy {
            (pairedConstructor.map { (first, second: KParameter) ->
                fun isTypeVariable() = second.type.javaType is TypeVariable<*>
                val tpe = if (isTypeVariable()) typeMap[first]?.type ?: second.type else second.type
                instantiateRandomClass(tpe, token.hash with tpe.jvmErasure.jvmName.hash with second.name!!.hash, past.plus(klass), kProperty)
            }).toTypedArray()
        }
        try {
            val res = defaultConstructor.call(*parameters)

            ObjectFactory.putIfAbsent(type, { type, past, prop, token ->
                defaultConstructor.call(*
                (pairedConstructor.map { (first, second: KParameter) ->
                    fun isTypeVariable() = second.type.javaType is TypeVariable<*>
                    val tpe = if (isTypeVariable()) typeMap[first]?.type ?: second.type else second.type
                    instantiateRandomClass(tpe, token.hash with tpe.jvmErasure.jvmName.hash with second.name!!.hash, past.plus(klass), prop)
                }).toTypedArray()
                )
            })
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
