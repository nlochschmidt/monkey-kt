package lexer

import token.Token
import token.TokenType
import token.TokenType.*

class Lexer(val input: String) {

  var position: Int = 0
  var readPosition: Int = 0
  var currentChar: Char = NULL

  init {
    readChar()
  }

  fun nextToken(): Token {
    skipWhitespace()
    val token = when (currentChar) {
      '=' -> Token(ASSIGN, "$currentChar")
      '+' -> Token(PLUS, "$currentChar")
      '-' -> Token(MINUS, "$currentChar")
      '!' -> Token(BANG, "$currentChar")
      '*' -> Token(ASTERISK, "$currentChar")
      '/' -> Token(SLASH, "$currentChar")
      '<' -> Token(LT, "$currentChar")
      '>' -> Token(GT, "$currentChar")
      ';' -> Token(SEMICOLON, "$currentChar")
      '(' -> Token(LPAREN, "$currentChar")
      ')' -> Token(RPAREN, "$currentChar")
      ',' -> Token(COMMA, "$currentChar")
      '{' -> Token(LBRACE, "$currentChar")
      '}' -> Token(RBRACE, "$currentChar")
      NULL -> Token(EOF, "")
      else -> {
        if (currentChar.isLetter()) {
          val identifier = readIdentifier()
          return lookupIdentifierToken(identifier)
        } else if (currentChar.isDigit()) {
          return Token(INT, readNumber())
        } else {
          Token(ILLEGAL, "$currentChar")
        }
      }
    }
    readChar()
    return token
  }

  private fun readIdentifier(): String {
    val startPosition = position
    while(currentChar.isLetter() || currentChar == '_') {
      readChar()
    }
    return input.substring(startPosition, position)
  }

  private fun lookupIdentifierToken(identifier: String): Token {
    return Token(
      type = KEYWORDS.getOrElse(identifier) { IDENT },
      literal = identifier
    )
  }

  private fun readNumber(): String {
    val startPosition = position
    while (currentChar.isDigit()) {
      readChar()
    }
    return input.substring(startPosition, position)
  }

  private fun readChar() {
    if (readPosition >= input.length) {
      currentChar = NULL
    } else {
      currentChar = input[readPosition]
    }
    position = readPosition
    readPosition += 1
  }

  private fun skipWhitespace() {
    while (currentChar.isWhitespace() && currentChar != NULL) {
      readChar()
    }
  }

  companion object {
    const val NULL: Char = '\u0000'

    val KEYWORDS : Map<String, TokenType>  = mapOf(
      "fn" to FUNCTION,
      "let" to LET,
      "true" to TRUE,
      "false" to FALSE,
      "if" to IF,
      "else" to ELSE,
      "return" to RETURN
      )
  }
}
