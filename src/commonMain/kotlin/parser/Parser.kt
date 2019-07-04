package parser

import lexer.Lexer
import parser.Precedence.LOWEST
import token.Token
import token.TokenType
import token.TokenType.ASSIGN
import token.TokenType.EOF
import token.TokenType.IDENT
import token.TokenType.LET
import token.TokenType.RETURN
import token.TokenType.SEMICOLON
import kotlin.ast.Expression
import kotlin.ast.ExpressionStatement
import kotlin.ast.Identifier
import kotlin.ast.LetStatement
import kotlin.ast.Program
import kotlin.ast.ReturnStatement
import kotlin.ast.Statement

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
    IDENT to ::parseIdentifier
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

    val expression = parseExpression(LOWEST)
    return LetStatement(letToken, name, expression)
  }

  fun parseReturnStatement(): ReturnStatement {
    val returnToken = currentToken
    val expression = parseExpression(LOWEST)
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
