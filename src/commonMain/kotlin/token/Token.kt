package token

data class Token(
  val type: TokenType,
  val literal: String
) {
  override fun toString(): String = "{Type:${type}, Literal:${literal}}"
}
