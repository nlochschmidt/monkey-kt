package parser

import lexer.Lexer
import parser.Precedence.LOWEST
import parser.Precedence.PREFIX
import token.Token
import token.TokenType
import token.TokenType.ASSIGN
import token.TokenType.BANG
import token.TokenType.EOF
import token.TokenType.IDENT
import token.TokenType.INT
import token.TokenType.LET
import token.TokenType.MINUS
import token.TokenType.RETURN
import token.TokenType.SEMICOLON
import ast.Expression
import ast.ExpressionStatement
import ast.Identifier
import ast.IntegerLiteral
import ast.LetStatement
import ast.PrefixExpression
import ast.Program
import ast.ReturnStatement
import ast.Statement

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

class Parser(private val lexer: Lexer) {
  private var currentToken: Token = lexer.nextToken()
  private var peekToken: Token = lexer.nextToken()

  private val prefixParseFunctions = mapOf<TokenType, PrefixParseFunction>(
    IDENT to ::parseIdentifier,
    INT to ::parseIntegerLiteral,
    BANG to ::parsePrefixExpression,
    MINUS to ::parsePrefixExpression
  )
  val infixParseFunctions = mapOf<TokenType, InfixParseFunction>()

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
    val prefixFunction = prefixParseFunctions.get(currentToken.type) ?: return run {
      _errors.add("No prefix parse function for ${currentToken.type} found")
      // TODO: Skipping the expresions until we encounter a semicolon
      while (!currentTokenIs(SEMICOLON)) {
        nextToken()
      }
      UnparsedExpression
    }

    val leftExpression = prefixFunction()

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

  private fun parsePrefixExpression(): Expression {
    val prefixToken = currentToken
    nextToken()
    val parseExpression = parseExpression(PREFIX)
    return PrefixExpression(prefixToken, prefixToken.literal, parseExpression)
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

  private fun peekError(type: TokenType) {
    _errors.add("expected next token to be $type, got ${peekToken.type} instead")
  }

  object UnparsedExpression : Expression {
    override val literal: String = "Not parsed"
  }
}
