package evaluator

import `object`.*
import `object`.Bool.Companion.FALSE
import `object`.Bool.Companion.TRUE
import ast.*

fun eval(node: Node, env: Environment): Object {
  return when (node) {
    is Program -> evalProgram(node, env)
    is ExpressionStatement -> eval(node.expression, env)
    is IntegerLiteral -> Integer(node.value)
    is BooleanLiteral -> Bool(node.value)
    is PrefixExpression ->
      eval(node.right, env).unlessError { value ->
        evalPrefixExpression(node.operator, value)
      }
    is InfixExpression ->
      eval(node.left, env).unlessError { left ->
        eval(node.right, env).unlessError { right ->
          evalInfixExpression(node.operator, left, right)
        }
      }
    is BlockStatement -> evalBlockStatement(node, env)
    is IfExpression -> evalIfExpression(node, env)
    is ReturnStatement -> {
      eval(node.returnValue, env).unlessError { value ->
        ReturnValue(value)
      }
    }
    is LetStatement -> {
      eval(node.value, env).unlessError { value ->
        env[node.name.value] = value
        value
      }
    }
    is Identifier -> {
      env[node.value] ?: Error.create("identifier not found: %s", node.value)
    }
    else -> Null
  }
}

private fun Object.unlessError(handler: (Object) -> Object): Object {
  return when (this) {
    is Error -> this
    else -> handler(this)
  }
}

fun evalPrefixExpression(operator: String, right: Object): Object {
  return when (operator) {
    "!" -> evalBangOperatorExpression(right)
    "-" -> evalMinusPrefixOperatorExpression(right)
    else -> Error.create("unknown operator: %s%s", operator, right::class.simpleName)
  }
}

fun evalBangOperatorExpression(right: Object): Object {
  return when (right) {
    TRUE -> FALSE
    FALSE -> TRUE
    Null -> TRUE
    else -> FALSE
  }
}

fun evalMinusPrefixOperatorExpression(right: Object): Object {
  return when (right) {
    is Integer -> Integer(-right.value)
    else -> Error.create("unknown operator: -%s", right.type)
  }
}

fun evalInfixExpression(operator: String, left: Object, right: Object): Object {
  return when {
    left is Integer && right is Integer -> evalIntegerInfixExpression(operator, left, right)
    operator == "==" -> Bool(left == right)
    operator == "!=" -> Bool(left != right)
    left.type != right.type -> Error.create(
      "type mismatch: %s %s %s",
      left.type, operator, right.type
    )
    else -> Error.create(
      "unknown operator: %s %s %s",
      left.type, operator, right.type
    )
  }
}

fun evalIntegerInfixExpression(operator: String, left: Integer, right: Integer): Object {
  return when (operator) {
    "+" -> Integer(left.value + right.value)
    "-" -> Integer(left.value - right.value)
    "*" -> Integer(left.value * right.value)
    "/" -> Integer(left.value / right.value)
    "<" -> Bool(left.value < right.value)
    ">" -> Bool(left.value > right.value)
    "==" -> Bool(left.value == right.value)
    "!=" -> Bool(left.value != right.value)
    else -> Error.create(
      "unknown operator: %s %s %s",
      left.type, operator, right.type
    )
  }
}

fun evalIfExpression(node: IfExpression, env: Environment): Object {
  return eval(node.condition, env).unlessError { condition ->
    when {
      isTruthy(condition) -> eval(node.consequence, env)
      node.alternative != null -> eval(node.alternative, env)
      else -> Null
    }
  }
}

fun isTruthy(condition: Object): Boolean {
  return !(condition == Null || condition == FALSE)
}

fun evalProgram(program: Program, env: Environment): Object {
  return program.statements.fold<Statement, Object>(Null) { _, statement ->
    when (val result = eval(statement, env)) {
      is ReturnValue -> return result.value
      is Error -> return result
      else -> result
    }
  }
}

fun evalBlockStatement(block: BlockStatement, env: Environment): Object {
  return block.statements.fold<Statement, Object>(Null) { _, statement ->
    when (val result = eval(statement, env)) {
      is ReturnValue -> return result
      is Error -> return result
      else -> result
    }
  }
}
