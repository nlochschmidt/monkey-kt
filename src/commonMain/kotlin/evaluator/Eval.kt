package evaluator

import `object`.Bool
import `object`.Integer
import `object`.Null
import `object`.Object
import ast.*

val TRUE = Bool(true)
val FALSE = Bool(false)

fun eval(node: Node): Object {
  return when(node) {
    is Program -> evalStatements(node.statements)
    is ExpressionStatement -> eval(node.expression)
    is IntegerLiteral -> Integer(node.value)
    is BooleanLiteral -> node.nativeBooleanToBoolObject()
    is PrefixExpression -> evalPrefixExpression(node.operator, eval(node.right))
    else -> Null
  }
}

fun evalPrefixExpression(operator: String, right: Object): Object {
  return when (operator) {
    "!" -> evalBangOperatorExpression(right)
    else -> Null
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

fun evalStatements(statements: List<Statement>): Object {
  return statements.fold(Null) { _: Object, statement -> eval(statement) }
}

private fun BooleanLiteral.nativeBooleanToBoolObject() =
  if (value) TRUE else FALSE
