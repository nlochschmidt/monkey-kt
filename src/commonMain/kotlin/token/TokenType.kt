package token

enum class TokenType {
  ILLEGAL,
  EOF,

  // Identifiers + literals
  IDENT, // add, foobar, x, y, ...
  INT,   // 1343456

  // Operators
  ASSIGN,
  PLUS,
  MINUS,
  BANG,
  ASTERISK,
  SLASH,

  LT,
  GT,

  EQ,
  NOT_EQ,

  // Delimiters
  COMMA,
  SEMICOLON,

  LPAREN,
  RPAREN,
  LBRACE,
  RBRACE,

  // Keywords
  FUNCTION,
  LET,
  TRUE,
  FALSE,
  IF,
  ELSE,
  RETURN
}
