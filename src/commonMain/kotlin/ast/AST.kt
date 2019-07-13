package ast

import token.Token

interface Node {
  val literal: String
}

interface Statement : Node

interface Expression : Node

data class Program(val statements: List<Statement>) : Node {
  override val literal: String
    get() = statements.firstOrNull()?.literal ?: ""

  override fun toString(): String = statements.joinToString("\n")
}

data class LetStatement(
  val token: Token,
  val name: Identifier,
  val value: Expression
) : Statement {
  override val literal: String
    get() = token.literal

  override fun toString() = "$literal $name = $value;"
}

data class Identifier(
  val token: Token,
  val value: String
): Expression {
  override val literal: String
    get() = token.literal

  override fun toString() = value
}

data class ReturnStatement(
  val token: Token,
  val returnValue: Expression
) : Statement {
  override val literal: String
    get() = token.literal

  override fun toString() = "$literal $returnValue;"
}

data class ExpressionStatement(
  val token: Token,
  val expression: Expression
) : Statement {
  override val literal: String
    get() = token.literal

  override fun toString() = expression.toString()
}

data class IntegerLiteral(
  val token: Token,
  val value: Int
): Expression {
  override val literal: String
    get() = token.literal

  override fun toString(): String = token.literal
}

data class BooleanLiteral(
  val token: Token,
  val value: Boolean
): Expression {
  override val literal: String
    get() = token.literal

  override fun toString(): String = token.literal
}

data class PrefixExpression(
  val token: Token,
  val operator: String,
  val right: Expression
): Expression {
  override val literal: String
    get() = token.literal

  override fun toString(): String = "($operator$right)"
}

data class InfixExpression(
  val token: Token,
  val left: Expression,
  val operator: String,
  val right: Expression
): Expression {
  override val literal: String
    get() = token.literal

  override fun toString(): String ="($left $operator $right)"
}

data class IfExpression(
  val token: Token,
  val condition: Expression,
  val consequence: BlockStatement,
  val alternative: BlockStatement?
): Expression {
  override val literal: String
    get() = token.literal

  override fun toString(): String {
    val elseString = alternative?.let { "else $it"} ?: ""
    return "if $condition $consequence$elseString"
  }
}

data class BlockStatement(
  val token: Token,
  val statements: List<Statement>
) : Statement {
  override val literal: String
    get() = token.literal

  override fun toString(): String = statements.joinToString("")
}

data class FunctionLiteral(
  val token: Token,
  val parameters: List<Identifier>,
  val body: BlockStatement
) : Expression {
  override val literal: String
    get() = token.literal

  override fun toString(): String = "$literal(${parameters.joinToString(", ")}) $body"
}
