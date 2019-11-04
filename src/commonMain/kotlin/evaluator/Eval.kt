package evaluator

import `object`.*
import `object`.Bool.Companion.FALSE
import `object`.Bool.Companion.TRUE
import ast.*

fun eval(node: Node): Object {
  return when(node) {
    is Program -> evalProgram(node)
    is ExpressionStatement -> eval(node.expression)
    is IntegerLiteral -> Integer(node.value)
    is BooleanLiteral -> Bool(node.value)
    is PrefixExpression -> evalPrefixExpression(node.operator, eval(node.right))
    is InfixExpression -> {
      val left = eval(node.left)
      val right = eval(node.right)
      evalInfixExpression(node.operator, left, right)
    }
    is BlockStatement -> evalBlockStatement(node)
    is IfExpression -> evalIfExpression(node)
    is ReturnStatement -> ReturnValue(eval(node.returnValue))
    else -> Null
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
      left.type, operator, right.type)
    else -> Error.create(
      "unknown operator: %s %s %s",
      left.type, operator, right.type)
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
      left.type, operator, right.type)
  }
}

fun evalIfExpression(node: IfExpression): Object {
  val condition = eval(node.condition)
  return when {
      isTruthy(condition) -> eval(node.consequence)
      node.alternative != null -> eval(node.alternative)
      else -> Null
  }
}

fun isTruthy(condition: Object): Boolean {
  return !(condition == Null || condition == FALSE)
}

fun evalProgram(program: Program): Object {
  return program.statements.fold<Statement, Object>(Null) { _, statement ->
    when (val result = eval(statement)) {
        is ReturnValue -> return result.value
        is Error -> return result
        else -> result
    }
  }
}

fun evalBlockStatement(block: BlockStatement): Object {
  return block.statements.fold<Statement, Object>(Null) { _, statement ->
    when (val result = eval(statement)) {
      is ReturnValue -> return result
      is Error -> return result
      else -> result
    }
  }
}
