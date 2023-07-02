package com.nghood.simplechess.evaluation;

import com.nghood.simplechess.io.MoveTransformer;
import com.nghood.simplechess.model.BoardState;
import com.nghood.simplechess.model.FollowupBoardStates;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * Calculates the best moves however only by looking at the next state without any minimax.
 */
public class SimpleBestMoveCalculator implements  BestMoveCalculation {

    @Override
    public Tuple2<String,BoardState> calculateBestMove(BoardState initialState) {
        FollowupBoardStates followupBoardStates = new FollowupBoardStates(initialState);
        var followupStates = followupBoardStates.getFollowupStates();
        var bestState = followupStates.get(0);
        int bestValue = Evaluation.getBoardValue(bestState.getT6());
        for (var followupState : followupStates) {
            int testValue = Evaluation.getBoardValue(followupState.getT6());
            if (initialState.isWhitePlayerMove() && testValue > bestValue) {
                bestValue = testValue;
                bestState = followupState;
            } else if (!initialState.isWhitePlayerMove() && testValue < bestValue) {
                bestValue = testValue;
                bestState = followupState;
            }
        }
        System.out.println("Calculated best move with the value: " + bestValue);
        String moveString = MoveTransformer.getMove(bestState.getT1(),bestState.getT2(),bestState.getT3(),bestState.getT4());
        return Tuples.of(moveString,bestState.getT6());
    }

    // first row, first column, second row, second column
  //  private List<Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState>> followupStates = new ArrayList<>();

}
