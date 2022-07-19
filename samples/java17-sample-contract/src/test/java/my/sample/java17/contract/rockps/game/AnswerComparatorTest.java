package my.sample.java17.contract.rockps.game;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnswerComparatorTest {

    @ParameterizedTest
    @MethodSource("values")
    public void testAnswerComparator(AnswerType answer1, AnswerType answer2, int result) {
        Game.AnswerComparator comparator = new Game.AnswerComparator();
        assertEquals(result, comparator.compare(answer1, answer2));
    }

    private static Stream<Arguments> values() {
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
        );
    }

}