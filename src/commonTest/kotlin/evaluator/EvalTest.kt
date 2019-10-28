package evaluator

import `object`.Bool
import `object`.Integer
import `object`.Object
import lexer.Lexer
import parser.Parser
import kotlin.test.Test
import kotlin.test.assertEquals

class EvalTest {

  @Test
  fun `test eval integer expression`() {
   val testCases = listOf(
     "5" to Integer(5),
     "10" to Integer(10))

    testCases.forEach { (input, expected) ->
      assertEquals(expected, testEval(input))
    }
  }

  @Test
  fun `test eval bool expression`() {
    val testCases = listOf(
      "true" to Bool(true),
      "false" to Bool(false)
    )

    testCases.forEach { (input, expected) ->
      assertEquals(expected, testEval(input))
    }
  }

  private fun testEval(input: String): Object {
    val program = Parser(Lexer(input)).parseProgram()
    return eval(program)
  }

}
