package com.nghood.simplechess.evaluation;

import com.nghood.simplechess.io.MoveTransformer;
import com.nghood.simplechess.model.BoardState;
import com.nghood.simplechess.model.FollowupBoardStates;
import com.nghood.simplechess.model.Piece;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple6;
import reactor.util.function.Tuples;

import java.util.List;

// simple minimax here
public class BestMoveCalculatorSimpleAlphaBeta implements BestMoveCalculation {

    private static final int MAX_DEPTH = 4;

    @Override
    public Tuple2<String, BoardState> calculateBestMove(BoardState initialState) {
        FollowupBoardStates followupBoardStates = new FollowupBoardStates(initialState);
        var followupStates = followupBoardStates.getFollowupStates();
        int idx = minimax0(followupStates, initialState);
        var bestFollowup = followupStates.get(idx);
        String moveString = MoveTransformer.getMove(bestFollowup.getT1(), bestFollowup.getT2(), bestFollowup.getT3(), bestFollowup.getT4());
        return Tuples.of(moveString, bestFollowup.getT6());

    }

    // returns the index of the best state
    public int minimax0(List<Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState>> depth1States, BoardState initialState) {
        int bestScore = initialState.isWhitePlayerMove() ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int out = 0;

        for (int i = 0; i < depth1States.size(); i++) {
            int score = alphaBeta(depth1States.get(i).getT6(), MAX_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE);
            if ((initialState.isWhitePlayerMove() && score > bestScore) || (!initialState.isWhitePlayerMove() && score < bestScore)) {
                bestScore = score;
                out = i;

            }
        }
        System.out.println("Move immediate value: "+Evaluation.getBoardValue(depth1States.get(out).getT6()));
        System.out.println("Minimax value: "+bestScore);
        return out;
    }


    public int alphaBeta(BoardState currentState, int currentDepth, int alpha, int beta) {
        if (currentDepth == 0) {
            return Evaluation.getBoardValue(currentState);
        }

        FollowupBoardStates followupBoardStates = new FollowupBoardStates(currentState);
        var followupStates = followupBoardStates.getFollowupStates();
        if (currentState.isWhitePlayerMove()) {
            int bestValue = Integer.MIN_VALUE;
            for (var followupState : followupStates) {
                int value = alphaBeta(followupState.getT6(), currentDepth - 1, alpha, beta);
                bestValue = Math.max(bestValue, value);
                alpha = Math.max(alpha, bestValue);
                if (beta <= alpha) {
                    break;
                }
            }
            return bestValue;
        } else {
            int bestValue = Integer.MAX_VALUE;
            for (var followupState : followupStates) {
                int value = alphaBeta(followupState.getT6(), currentDepth - 1, alpha, beta);
                bestValue = Math.min(bestValue, value);
                beta = Math.min(beta, bestValue);
                if (beta <= alpha) {
                    break;
                }
            }
            return bestValue;
        }




    }


}
