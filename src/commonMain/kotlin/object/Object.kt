package `object`

import `object`.ObjectType.*
import ast.BlockStatement
import ast.Identifier

interface Object {
  val type: ObjectType
}

// FIXME: I might be able to get away without the ObjectType. Let's see

// FIXME: The wrapper classes might be convertible to inline classes
data class Integer(val value: Int) : Object {
  override val type: ObjectType = INTEGER

  override fun toString(): String = value.toString()
}

class Bool private constructor(val value: Boolean) : Object {
  override val type: ObjectType = BOOLEAN

  override fun toString(): String = value.toString()

  companion object {
    val TRUE = Bool(true)
    val FALSE = Bool(false)

    operator fun invoke(value: Boolean): Bool {
      return if (value) TRUE else FALSE
    }
  }
}

object Null : Object {
  override val type: ObjectType = NULL

  override fun toString(): String = "null"
}

data class ReturnValue(val value: Object) : Object {
  override val type: ObjectType = RETURN_VALUE

  override fun toString(): String = value.toString()
}

data class Function(
  val parameters: List<Identifier>,
  val body: BlockStatement,
  val env: Environment
) : Object {
  override val type: ObjectType = FUNCTION

  override fun toString(): String = "fn ${parameters.joinToString(", ", "(", ")")} {\n$body\n}"
}

data class Error(val message: String) : Object {
  override val type: ObjectType = ERROR

  override fun toString(): String = "ERROR: $message"

  companion object {
    fun create(format: String, vararg args: Any?): Error {
      // TODO: Implement real format
      val message = args.fold(format) {acc: String, any: Any? ->
        acc.replaceFirst("%s", any.toString())
      }
      return Error(message)
    }
  }
}

data class Environment(val map: MutableMap<String, Object> = mutableMapOf(), val outer: Environment? = null) {
  operator fun get(name: String): Object? = map[name] ?: outer?.get(name)
  operator fun set(name: String, value: Object) {
    map[name] = value
  }
  fun scoped() = Environment(outer = this)
}
