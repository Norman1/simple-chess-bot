package com.nghood.simplechess.evaluation;

import com.nghood.simplechess.io.MoveTransformer;
import com.nghood.simplechess.model.*;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple6;
import reactor.util.function.Tuples;

import java.util.List;

// simple minimax here
public class BestMoveCalculatorAlphaBeta implements BestMoveCalculation {

    private static final int MAX_DEPTH = 4;
    private int amountTraversedNodes = 0;
    private static int allTraversedNoded = 0;

    @Override
    public Tuple2<String, BoardState> calculateBestMove(BoardState initialState) {
        long startTime = System.currentTimeMillis();
        FollowupBoardStates opponentFollowup = new FollowupBoardStates(initialState, null, true);
        AttackBoardState opponentAttack = new AttackBoardStateCalculator().calculateAttackBoardState(initialState, opponentFollowup.getFollowupStates());

        FollowupBoardStates followupBoardStates = new FollowupBoardStates(initialState, opponentAttack, false);
        var followupStates = followupBoardStates.getFollowupStates();
        int idx = minimax0(followupStates, initialState);
        var bestFollowup = followupStates.get(idx);
        String moveString = MoveTransformer.getMove(bestFollowup.getT1(), bestFollowup.getT2(), bestFollowup.getT3(), bestFollowup.getT4());
        long endTime = System.currentTimeMillis();
        long timeSpent = (endTime - startTime) / 1000;
        System.out.println("Time spent: " + timeSpent + " seconds");
        System.out.println("Traversed nodes: " + amountTraversedNodes);
        allTraversedNoded += amountTraversedNodes;
        System.out.println("All traversed nodes: " + allTraversedNoded);
        return Tuples.of(moveString, bestFollowup.getT6());
    }


    /**
     * 1. Give super high value for capturing the last piece which has moved. We do not keep track of historic moves since the last really
     * opponent played move was probably sound (perhaps would be better if we still did).
     * 2. For each capture add x10 the value of the captured piece and subtract the value of the capturing piece. x10 ensures that
     * capturing is not negative.
     * 3. For each non-capture add a subtraction if we are moving to a field which is under opponent attack from a piece which is worth less than our piece. Evaluating
     * the board value seems too expensive and does not seem to give benefits.
     *
     */
    private void sortMoves(List<Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState>> followupStates, Tuple2<Integer,Integer> lastMovedPiece,BoardState initialState,AttackBoardState attackBoardState) {
        /* Case no sort: 16 seconds, All traversed nodes: 6678706
         * Add sort last capture (no least valuable piece yet): 13 seconds All traversed nodes: 4192084
         * Add capturing pieces: 11 seconds, All traversed nodes: 2358361
         * Add non captures: 12 seconds, All traversed nodes: 2111608
         */


       followupStates.sort((followup1,followup2) -> Integer.compare(getSortValue(followup2,lastMovedPiece,initialState,attackBoardState),getSortValue(followup1,lastMovedPiece,initialState,attackBoardState)));
    }

    // first row, first column, second row, second column
    private int getSortValue(Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState> followupState, Tuple2<Integer,Integer> lastMovedPiece,BoardState initialState,AttackBoardState attackBoardState){
        // we fail to consider stuff like en passant captures but whatever
        int value = 0;
        // Prio 1: last moved piece captures
        if(lastMovedPiece != null){
            boolean isLastMoveCapture = followupState.getT3() == lastMovedPiece.getT1() && followupState.getT4() == lastMovedPiece.getT2();
            if(isLastMoveCapture){
                value += 1000000;
                Piece capturingPiece = followupState.getT6().getChessBoard()[followupState.getT3()][followupState.getT4()];
                int capturingPieceValue = Evaluation.getAbsolutePieceValue(capturingPiece);
               value -= capturingPieceValue;
            }
        }

        // Prio 2: captures
        Piece previousPieceOnPosition = initialState.getChessBoard()[followupState.getT3()][followupState.getT4()];
        Piece movingPiece = followupState.getT6().getChessBoard()[followupState.getT3()][followupState.getT4()];
        int movingPieceValue = Evaluation.getAbsolutePieceValue(movingPiece);
        if(previousPieceOnPosition != null){
            int capturedPieceValue = Evaluation.getAbsolutePieceValue(previousPieceOnPosition);
            value += 10* capturedPieceValue;
            value -= movingPieceValue;
            // Prio 3: non captures (TODO: prunes little branches and makes the bot even a tiny bit slower)
        }
            int opponentAttackValue = attackBoardState.getAttackValue(followupState.getT3(),followupState.getT4());
            if(opponentAttackValue != -1){
                    value -= movingPieceValue;
            }







        return value;

    }

    // returns the index of the best state
    public int minimax0(List<Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState>> depth1States, BoardState initialState) {
        int bestScore = initialState.isWhitePlayerMove() ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int out = 0;
        for (int i = 0; i < depth1States.size(); i++) {
            int score = alphaBeta(depth1States.get(i).getT6(), MAX_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE,null);
            if ((initialState.isWhitePlayerMove() && score > bestScore) || (!initialState.isWhitePlayerMove() && score < bestScore)) {
                bestScore = score;
                out = i;

            }
        }
        System.out.println("Move immediate value: " + Evaluation.getBoardValue(depth1States.get(out).getT6()));
        System.out.println("Minimax value: " + bestScore);
        return out;
    }

    private Tuple2<Integer,Integer> extractLastMovedPiece(Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState> state){
        Tuple2<Integer,Integer> out = Tuples.of(state.getT3(),state.getT4());
        return out;
    }


    public int alphaBeta(BoardState currentState, int currentDepth, int alpha, int beta, Tuple2<Integer,Integer>lastMovedPiece) {
        amountTraversedNodes++;
        if (currentDepth == 0) {
            return Evaluation.getBoardValue(currentState);
        }

        FollowupBoardStates opponentFollowup = new FollowupBoardStates(currentState, null, true);
        AttackBoardState opponentAttack = new AttackBoardStateCalculator().calculateAttackBoardState(currentState, opponentFollowup.getFollowupStates());
        FollowupBoardStates followupBoardStates = new FollowupBoardStates(currentState, opponentAttack, false);
        List<Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState>> followupStates = followupBoardStates.getFollowupStates();

        sortMoves(followupStates,lastMovedPiece,currentState,opponentAttack); // TODO hier

        if (currentState.isWhitePlayerMove()) {
            int bestValue = Integer.MIN_VALUE;
            for (var followupState : followupStates) {
                int value = alphaBeta(followupState.getT6(), currentDepth - 1, alpha, beta,extractLastMovedPiece(followupState));
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
                int value = alphaBeta(followupState.getT6(), currentDepth - 1, alpha, beta,extractLastMovedPiece(followupState));
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
