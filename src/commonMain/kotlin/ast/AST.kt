package ast

import token.Token
import token.TokenType

interface Node {
  val token: Token
  val literal: String get() = token.literal
}

interface Statement : Node

interface Expression : Node

data class Program(val statements: List<Statement>): Node {
  override val token: Token = Token(TokenType.EOF, "")
  override fun toString(): String = statements.joinToString("\n")
}

data class LetStatement(
  override val token: Token,
  val name: Identifier,
  val value: Expression
) : Statement {
  override fun toString() = "$literal $name = $value;"
}

data class Identifier(
  override val token: Token,
  val value: String
) : Expression {
  override fun toString() = value
}

data class ReturnStatement(
  override val token: Token,
  val returnValue: Expression
) : Statement {
  override fun toString() = "$literal $returnValue;"
}

data class ExpressionStatement(
  override val token: Token,
  val expression: Expression
) : Statement {
  override fun toString() = expression.toString()
}

data class IntegerLiteral(
  override val token: Token,
  val value: Int
) : Expression {
  override fun toString(): String = token.literal
}

data class BooleanLiteral(
  override val token: Token,
  val value: Boolean
) : Expression {
  override fun toString(): String = token.literal
}

data class StringLiteral(
  override val token: Token,
  val value: String
): Expression {
  override fun toString(): String = token.literal
}

data class PrefixExpression(
  override val token: Token,
  val operator: String,
  val right: Expression
) : Expression {
  override fun toString(): String = "($operator$right)"
}

data class InfixExpression(
  override val token: Token,
  val left: Expression,
  val operator: String,
  val right: Expression
) : Expression {
  override fun toString(): String = "($left $operator $right)"
}

data class IfExpression(
  override val token: Token,
  val condition: Expression,
  val consequence: BlockStatement,
  val alternative: BlockStatement?
) : Expression {
  override fun toString(): String {
    val elseString = alternative?.let { "else $it" } ?: ""
    return "if $condition $consequence$elseString"
  }
}

data class BlockStatement(
  override val token: Token,
  val statements: List<Statement>
) : Statement {
  override fun toString(): String = statements.joinToString("")
}

data class FunctionLiteral(
  override val token: Token,
  val parameters: List<Identifier>,
  val body: BlockStatement
) : Expression {
  override fun toString(): String = "$literal(${parameters.joinToString(", ")}) $body"
}

data class CallExpression(
  override val token: Token,
  val function: Expression,
  val arguments: List<Expression>
) : Expression {
  override fun toString(): String ="$function(${arguments.joinToString(", ")})"
}
