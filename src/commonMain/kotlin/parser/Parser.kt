package parser

import lexer.Lexer
import token.Token
import token.TokenType
import token.TokenType.ASSIGN
import token.TokenType.EOF
import token.TokenType.IDENT
import token.TokenType.LET
import token.TokenType.SEMICOLON
import kotlin.ast.Expression
import kotlin.ast.Identifier
import kotlin.ast.LetStatement
import kotlin.ast.Program
import kotlin.ast.Statement

class Parser(val lexer: Lexer) {
  var currentToken: Token = lexer.nextToken()
  var peekToken: Token = lexer.nextToken()

  fun nextToken() {
    currentToken = peekToken
    peekToken = lexer.nextToken()
  }

  fun parseProgram(): Program {
    val statements = mutableListOf<Statement?>()

    while (currentToken.type != EOF) {
      statements.add(parseStatement())
      nextToken()
    }

    return Program(statements.filterNotNull())
  }

  fun parseStatement(): Statement? {
    return when(currentToken.type) {
      LET -> parseLetStatement()
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

    // TODO: Skupping the expresions until we encounter a semicolon
    while (!currentTokenIs(SEMICOLON)) {
      nextToken()
    }

    return LetStatement(letToken, name, object : Expression {
      override val literal: String = "Not parsed"
    })
  }

  fun currentTokenIs(type: TokenType): Boolean = currentToken.type == type

  fun peekTokenIs(type: TokenType): Boolean = peekToken.type == type

  fun expectPeek(type: TokenType): Boolean {
    return if (peekTokenIs(type)) {
      nextToken()
      true
    } else {
      false
    }
  }
}
