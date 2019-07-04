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

class Parser(val lexer: Lexer) {
  var currentToken: Token = lexer.nextToken()
  var peekToken: Token = lexer.nextToken()

  val prefixParseFunctions = mapOf<TokenType, PrefixParseFunction>(
    IDENT to ::parseIdentifier,
    INT to ::parseIntegerLiteral,
    BANG to ::parsePrefixExpression,
    MINUS to ::parsePrefixExpression
  )
  val infixParseFunctions = mapOf<TokenType, InfixParseFunction>()

  val _errors = mutableListOf<String>()

  val errors: List<String>
    get() = _errors.toList()

  fun nextToken() {
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

  fun parseStatement(): Statement? {
    return when(currentToken.type) {
      LET -> parseLetStatement()
      RETURN -> parseReturnStatement()
      else -> parseExpressionStatement()
    }
  }

  fun parseLetStatement(): LetStatement? {
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

  fun parseReturnStatement(): ReturnStatement {
    val returnToken = currentToken
    nextToken()
    val expression = parseExpression(LOWEST)
    if (peekTokenIs(SEMICOLON)) {
      nextToken()
    }
    return ReturnStatement(returnToken, expression)
  }

  fun parseExpressionStatement(): ExpressionStatement {
    val expressionStatement = ExpressionStatement(currentToken, parseExpression(LOWEST))

    if (peekTokenIs(SEMICOLON)) {
      nextToken()
    }

    return expressionStatement
  }

  fun parseExpression(@Suppress("UNUSED_PARAMETER") precedence: Precedence): Expression {
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

  fun parseIdentifier(): Expression {
    return Identifier(currentToken, currentToken.literal)
  }

  fun parseIntegerLiteral(): Expression {
    val value = try {
      currentToken.literal.toInt()
    } catch (ex: NumberFormatException) {
      return UnparsedExpression
    }
    return IntegerLiteral(currentToken, value)
  }

  fun parsePrefixExpression(): Expression {
    val prefixToken = currentToken
    nextToken()
    val parseExpression = parseExpression(PREFIX)
    return PrefixExpression(prefixToken, prefixToken.literal, parseExpression)
  }

  fun currentTokenIs(type: TokenType): Boolean = currentToken.type == type

  fun peekTokenIs(type: TokenType): Boolean = peekToken.type == type

  fun expectPeek(type: TokenType): Boolean {
    return if (peekTokenIs(type)) {
      nextToken()
      true
    } else {
      peekError(type)
      false
    }
  }

  fun peekError(type: TokenType) {
    _errors.add("expected next token to be $type, got ${peekToken.type} instead")
  }

  object UnparsedExpression : Expression {
    override val literal: String = "Not parsed"
  }
}
