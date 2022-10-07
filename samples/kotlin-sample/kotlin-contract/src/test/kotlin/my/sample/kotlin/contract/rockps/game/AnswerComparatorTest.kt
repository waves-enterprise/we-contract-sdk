package my.sample.kotlin.contract.rockps.game

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class AnswerComparatorTest {

    @ParameterizedTest
    @MethodSource("values")
    fun testAnswerComparator(
        answer1: AnswerType,
        answer2: AnswerType,
        result: Int
    ) {
        val comparator = Game.AnswerComparator()
        assertEquals(result, comparator.compare(answer1, answer2))
    }

    companion object {
        @JvmStatic
        private fun values(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(AnswerType.PAPER, AnswerType.PAPER, 0),
                Arguments.of(AnswerType.PAPER, AnswerType.ROCK, 1),
                Arguments.of(AnswerType.ROCK, AnswerType.PAPER, -1),
                Arguments.of(AnswerType.ROCK, AnswerType.ROCK, 0),
                Arguments.of(AnswerType.SCISSORS, AnswerType.ROCK, -2),
                Arguments.of(AnswerType.ROCK, AnswerType.SCISSORS, 2),
                Arguments.of(AnswerType.SCISSORS, AnswerType.SCISSORS, 0),
                Arguments.of(AnswerType.SCISSORS, AnswerType.PAPER, 1),
                Arguments.of(AnswerType.PAPER, AnswerType.SCISSORS, -1),
                Arguments.of(AnswerType.PAPER, AnswerType.PAPER, 0)
            )
        }
    }
}
