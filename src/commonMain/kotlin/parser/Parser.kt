package parser

import lexer.Lexer
import token.Token
import token.TokenType
import token.TokenType.ASSIGN
import token.TokenType.EOF
import token.TokenType.IDENT
import token.TokenType.LET
import token.TokenType.RETURN
import token.TokenType.SEMICOLON
import kotlin.ast.Expression
import kotlin.ast.Identifier
import kotlin.ast.LetStatement
import kotlin.ast.Program
import kotlin.ast.ReturnStatement
import kotlin.ast.Statement

class Parser(val lexer: Lexer) {
  var currentToken: Token = lexer.nextToken()
  var peekToken: Token = lexer.nextToken()

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
      else -> null
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

    val expression = parseExpression()
    return LetStatement(letToken, name, expression)
  }

  fun parseReturnStatement(): ReturnStatement {
    val returnToken = currentToken
    val expression = parseExpression()
    return ReturnStatement(returnToken, expression)
  }

  fun parseExpression(): Expression {
    // TODO: Skipping the expresions until we encounter a semicolon
    while (!currentTokenIs(SEMICOLON)) {
      nextToken()
    }

    return UnparsedExpression
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
