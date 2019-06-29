package lexer

import token.Token
import token.TokenType.*

class Lexer(val input: String) {

  var position: Int = 0
  var readPosition: Int = 0
  var currentChar: Char = NULL

  init {
    readChar()
  }
  
  fun nextToken(): Token {
    val token = when (currentChar) {
      '=' -> Token(ASSIGN, "$currentChar")
      ';' -> Token(SEMICOLON, "$currentChar")
      '(' -> Token(LPAREN, "$currentChar")
      ')' -> Token(RPAREN, "$currentChar")
      ',' -> Token(COMMA, "$currentChar")
      '+' -> Token(PLUS, "$currentChar")
      '{' -> Token(LBRACE, "$currentChar")
      '}' -> Token(RBRACE, "$currentChar")
      else -> Token(EOF, "")
    }
    readChar()
    return token
  }

  private fun readChar() {
    if (readPosition >= input.length) {
      currentChar = NULL
    } else {
      currentChar = input[readPosition]
    }
    position += readPosition
    readPosition += 1
  }

  companion object {
    const val NULL: Char = '\u0000'
  }
}
