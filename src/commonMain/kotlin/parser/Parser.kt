package parser

import ast.*
import lexer.Lexer
import token.Token
import token.TokenType
import parser.Precedence.*
import token.TokenType.*

typealias PrefixParseFunction = () -> Expression
typealias InfixParseFunction = (Expression) -> Expression

/**
 * Order of enum instances is important since `ordinal` is used as precedence
 */
enum class Precedence {
  LOWEST,
  EQUALS,
  LESSGREATER,
  SUM,
  PRODUCT,
  PREFIX,
  CALL
}

val precedences= mapOf(
  EQ to EQUALS,
  NOT_EQ to EQUALS,
  LT to LESSGREATER,
  GT to LESSGREATER,
  PLUS to SUM,
  MINUS to SUM,
  SLASH to PRODUCT,
  ASTERISK to PRODUCT
)

class Parser(private val lexer: Lexer) {
  private var currentToken: Token = lexer.nextToken()
  private var peekToken: Token = lexer.nextToken()

  private val prefixParseFunctions = mapOf<TokenType, PrefixParseFunction>(
    IDENT to ::parseIdentifier,
    INT to ::parseIntegerLiteral,
    TRUE to ::parseBooleanLiteral,
    FALSE to ::parseBooleanLiteral,
    BANG to ::parsePrefixExpression,
    MINUS to ::parsePrefixExpression
  )
  val infixParseFunctions = mapOf(
    EQ to ::parseInfixExpression,
    NOT_EQ to ::parseInfixExpression,
    LT to ::parseInfixExpression,
    GT to ::parseInfixExpression,
    PLUS to ::parseInfixExpression,
    MINUS to ::parseInfixExpression,
    SLASH to ::parseInfixExpression,
    ASTERISK to ::parseInfixExpression
  )

  private val _errors = mutableListOf<String>()

  val errors: List<String>
    get() = _errors.toList()

  private fun nextToken() {
    currentToken = peekToken
    peekToken = lexer.nextToken()
  }

  fun parseProgram(): Program {
    val statements = mutableListOf<Statement?>()

    while (!currentTokenIs(EOF)) {
      statements.add(parseStatement())
      nextToken()
    }

    return Program(statements.filterNotNull())
  }

  private fun parseStatement(): Statement? {
    return when(currentToken.type) {
      LET -> parseLetStatement()
      RETURN -> parseReturnStatement()
      else -> parseExpressionStatement()
    }
  }

  private fun parseLetStatement(): LetStatement? {
    val letToken = currentToken

    if (!expectPeek(IDENT)) {
      return null
    }

    val name = Identifier(currentToken, currentToken.literal)

    if (!expectPeek(ASSIGN)) {
      return null
    }
    nextToken()
    val expression = parseExpression(LOWEST)
    if (peekTokenIs(SEMICOLON)) {
      nextToken()
    }
    return LetStatement(letToken, name, expression)
  }

  private fun parseReturnStatement(): ReturnStatement {
    val returnToken = currentToken
    nextToken()
    val expression = parseExpression(LOWEST)
    if (peekTokenIs(SEMICOLON)) {
      nextToken()
    }
    return ReturnStatement(returnToken, expression)
  }

  private fun parseExpressionStatement(): ExpressionStatement {
    val expressionStatement = ExpressionStatement(currentToken, parseExpression(LOWEST))

    if (peekTokenIs(SEMICOLON)) {
      nextToken()
    }

    return expressionStatement
  }

  private fun parseExpression(@Suppress("UNUSED_PARAMETER") precedence: Precedence): Expression {
    val prefixFunction = prefixParseFunctions[currentToken.type] ?: run {
      _errors.add("No prefix parse function for ${currentToken.type} found")
      return UnparsedExpression
    }
    var leftExpression = prefixFunction()

    while (!peekTokenIs(SEMICOLON) && precedence < peekPrecedence()) {
      val infixFunction = infixParseFunctions[peekToken.type] ?: run {
        return leftExpression
      }
      nextToken()
      leftExpression = infixFunction(leftExpression)
    }

    return leftExpression
  }

  private fun parseIdentifier(): Expression {
    return Identifier(currentToken, currentToken.literal)
  }

  private fun parseIntegerLiteral(): Expression {
    val value = try {
      currentToken.literal.toInt()
    } catch (ex: NumberFormatException) {
      return UnparsedExpression
    }
    return IntegerLiteral(currentToken, value)
  }

  private fun parseBooleanLiteral(): Expression {
    return BooleanLiteral(currentToken,  currentTokenIs(TRUE))
  }

  private fun parsePrefixExpression(): Expression {
    val prefixToken = currentToken
    nextToken()
    val parseExpression = parseExpression(PREFIX)
    return PrefixExpression(prefixToken, prefixToken.literal, parseExpression)
  }

  private fun parseInfixExpression(left: Expression): Expression {
    val operatorToken = currentToken
    val precedence = curentPrecedence()
    nextToken()
    val right = parseExpression(precedence)
    return InfixExpression(operatorToken, left, operatorToken.literal, right)
  }

  private fun currentTokenIs(type: TokenType): Boolean = currentToken.type == type

  private fun peekTokenIs(type: TokenType): Boolean = peekToken.type == type

  private fun expectPeek(type: TokenType): Boolean {
    return if (peekTokenIs(type)) {
      nextToken()
      true
    } else {
      peekError(type)
      false
    }
  }

  fun peekPrecedence(): Precedence = precedences[peekToken.type] ?: LOWEST

  fun curentPrecedence(): Precedence = precedences[currentToken.type] ?: LOWEST

  private fun peekError(type: TokenType) {
    _errors.add("expected next token to be $type, got ${peekToken.type} instead")
  }

  object UnparsedExpression : Expression {
    override val literal: String = "Not parsed"
  }
}
