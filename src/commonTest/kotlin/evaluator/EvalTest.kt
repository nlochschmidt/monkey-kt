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

  @Test
  fun `test bang operator`() {
    val testCases = listOf(
      "!true" to Bool(false),
      "!false" to Bool(true),
      "!5" to Bool(false),
      "!!true" to Bool(true),
      "!!false" to Bool(false),
      "!!5" to Bool(true)
    )

    testCases.forEach { (input, expected) ->
      val result = testEval(input)
      assertEquals(expected, result, "evaluating $input was not equal to $expected")
    }
  }

  private fun testEval(input: String): Object {
    val program = Parser(Lexer(input)).parseProgram()
    return eval(program)
  }

}
