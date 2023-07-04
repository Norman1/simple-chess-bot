package com.nghood.simplechess.evaluation;

import com.nghood.simplechess.io.MoveTransformer;
import com.nghood.simplechess.model.*;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple4;
import reactor.util.function.Tuple6;
import reactor.util.function.Tuples;

import java.util.List;
import java.util.Optional;

// simple minimax here
public class BestMoveCalculatorAlphaBeta implements BestMoveCalculation {

    private static final int MAX_DEPTH = 3;
    private int amountTraversedNodes = 0;
    private static int allTraversedNoded = 0;

    @Override
    public Tuple2<String, BoardState> calculateBestMove(BoardState initialState) {
        long startTime = System.currentTimeMillis();
        FollowupBoardStates opponentFollowup = new FollowupBoardStates(initialState, null, true);
        AttackBoardState opponentAttack = new AttackBoardStateCalculator().calculateAttackBoardState(initialState, opponentFollowup.getFollowupStates());
        FollowupBoardStates followupBoardStates = new FollowupBoardStates(initialState, opponentAttack, false);
      //  var followupStates = followupBoardStates.getFollowupStates();
        // int idx = minimax0(followupStates, initialState);


        AlphaBetaTree alphaBetaTree = new AlphaBetaTree();


        alphaBetaTree.setCurrentDepth(MAX_DEPTH);

        // TODO debug, test if 6 depth helps
        if(!initialState.isWhitePlayerMove()){
            alphaBetaTree.setCurrentDepth(MAX_DEPTH+3);
        }

        alphaBetaTree.setCurrentState(initialState);
        alphaBetaTree.setAlpha(Integer.MIN_VALUE);
        alphaBetaTree.setBeta(Integer.MAX_VALUE);
        alphaBeta(alphaBetaTree);

        int treeValue = alphaBetaTree.getTreeValue();
        Optional<AlphaBetaTree> bestChildTreeOpt = alphaBetaTree.getChildTrees().stream().filter(child -> child.getTreeValue() == treeValue).findAny();

       String moveString = null;
        AlphaBetaTree bestChildTree = null;
        if(bestChildTreeOpt.isPresent()){
            bestChildTree = bestChildTreeOpt.get();
            Tuple4<Integer, Integer, Integer, Integer> moves = bestChildTree.getMovesToGetToPosition();
            moveString = MoveTransformer.getMove(moves.getT1(), moves.getT2(), moves.getT3(), moves.getT4());
        }
        long endTime = System.currentTimeMillis();
        long timeSpent = (endTime - startTime) / 1000;
        System.out.println("Time spent: " + timeSpent + " seconds");
        System.out.println("Traversed nodes: " + amountTraversedNodes);
        allTraversedNoded += amountTraversedNodes;
        System.out.println("All traversed nodes: " + allTraversedNoded);
        System.out.println("MiniMax value: "+treeValue);
        if(bestChildTreeOpt.isEmpty()){
            return null;
        }

        System.out.println("Immediate value: "+Evaluation.getBoardValue(bestChildTree.getCurrentState()));

        return Tuples.of(moveString, bestChildTree.getCurrentState());

    }


    /**
     * 1. Give super high value for capturing the last piece which has moved. We do not keep track of historic moves since the last really
     * opponent played move was probably sound (perhaps would be better if we still did).
     * 2. For each capture add x10 the value of the captured piece and subtract the value of the capturing piece. x10 ensures that
     * capturing is not negative.
     * 3. For each non-capture add a subtraction if we are moving to a field which is under opponent attack from a piece which is worth less than our piece. Evaluating
     * the board value seems too expensive and does not seem to give benefits.
     */
    private void sortMoves(List<Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState>> followupStates, Tuple2<Integer, Integer> lastMovedPiece, BoardState initialState, AttackBoardState attackBoardState) {
        /* Case no sort: 16 seconds, All traversed nodes: 6678706
         * Add sort last capture (no least valuable piece yet): 13 seconds All traversed nodes: 4192084
         * Add capturing pieces: 11 seconds, All traversed nodes: 2358361
         * Add non captures: 12 seconds, All traversed nodes: 2111608
         */


        followupStates.sort((followup1, followup2) -> Integer.compare(getSortValue(followup2, lastMovedPiece, initialState, attackBoardState), getSortValue(followup1, lastMovedPiece, initialState, attackBoardState)));
    }

    // first row, first column, second row, second column
    private int getSortValue(Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState> followupState, Tuple2<Integer, Integer> lastMovedPiece, BoardState initialState, AttackBoardState attackBoardState) {
        // we fail to consider stuff like en passant captures but whatever
        int value = 0;
        // Prio 1: last moved piece captures
        if (lastMovedPiece != null) {
            boolean isLastMoveCapture = followupState.getT3() == lastMovedPiece.getT1() && followupState.getT4() == lastMovedPiece.getT2();
            if (isLastMoveCapture) {
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
        if (previousPieceOnPosition != null) {
            int capturedPieceValue = Evaluation.getAbsolutePieceValue(previousPieceOnPosition);
            value += 10 * capturedPieceValue;
            value -= movingPieceValue;
            // Prio 3: non captures (TODO: prunes little branches and makes the bot even a tiny bit slower)
        }
        int opponentAttackValue = attackBoardState.getAttackValue(followupState.getT3(), followupState.getT4());
        if (opponentAttackValue != -1) {
            value -= movingPieceValue;
        }

        return value;

    }



    private Tuple2<Integer, Integer> extractLastMovedPiece(Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState> state) {
        Tuple2<Integer, Integer> out = Tuples.of(state.getT3(), state.getT4());
        return out;
    }

    private boolean isKingTaken(Piece[][] board){
        int kingCount = 0;
        for(int row = 0; row < 8; row++){
            for(int column = 0; column < 8; column++){
                if(board[row][column] != null){
                    if(board[row][column] == Piece.WHITE_KING || board[row][column] ==Piece.BLACK_KING){
                        kingCount++;
                    }
                }
            }
        }

        return kingCount != 2;
    }


    public void alphaBeta(AlphaBetaTree alphaBetaTree) {
        amountTraversedNodes++;
        if (alphaBetaTree.getCurrentDepth() == 0 || isKingTaken(alphaBetaTree.getCurrentState().getChessBoard())) {
            alphaBetaTree.setTreeValue(Evaluation.getBoardValue(alphaBetaTree.getCurrentState()));
            return;
        }
        FollowupBoardStates opponentFollowup = new FollowupBoardStates(alphaBetaTree.getCurrentState(), null, true);
        AttackBoardState opponentAttack = new AttackBoardStateCalculator().calculateAttackBoardState(alphaBetaTree.getCurrentState(), opponentFollowup.getFollowupStates());
        FollowupBoardStates followupBoardStates = new FollowupBoardStates(alphaBetaTree.getCurrentState(), opponentAttack, false);
        List<Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState>> followupStates = followupBoardStates.getFollowupStates();
        sortMoves(followupStates, alphaBetaTree.getLastMovedPiece(), alphaBetaTree.getCurrentState(), opponentAttack);

        if (alphaBetaTree.getCurrentState().isWhitePlayerMove()) {
            int bestValue = Integer.MIN_VALUE;
            for (var followupState : followupStates) {
                AlphaBetaTree childTree = new AlphaBetaTree();
                alphaBetaTree.getChildTrees().add(childTree);
                childTree.setBeta(alphaBetaTree.getBeta());
                childTree.setAlpha(alphaBetaTree.getAlpha());
                childTree.setCurrentDepth(alphaBetaTree.getCurrentDepth() - 1);
                childTree.setCurrentState(followupState.getT6());
                childTree.setLastMovedPiece(extractLastMovedPiece(followupState));
                childTree.setMovesToGetToPosition(Tuples.of(followupState.getT1(), followupState.getT2(), followupState.getT3(), followupState.getT4()));
                alphaBeta(childTree);
                int value = childTree.getTreeValue();
                bestValue = Math.max(bestValue, value);
                alphaBetaTree.setTreeValue(bestValue);
                alphaBetaTree.setAlpha(Math.max(alphaBetaTree.getAlpha(), bestValue));
                if (alphaBetaTree.getBeta() <= alphaBetaTree.getAlpha()) {
                    break;
                }
            }
        } else {
            int bestValue = Integer.MAX_VALUE;

            for (var followupState : followupStates) {
                AlphaBetaTree childTree = new AlphaBetaTree();
                alphaBetaTree.getChildTrees().add(childTree);
                childTree.setBeta(alphaBetaTree.getBeta());
                childTree.setAlpha(alphaBetaTree.getAlpha());
                childTree.setCurrentDepth(alphaBetaTree.getCurrentDepth() - 1);
                childTree.setCurrentState(followupState.getT6());
                childTree.setLastMovedPiece(extractLastMovedPiece(followupState));
                childTree.setMovesToGetToPosition(Tuples.of(followupState.getT1(), followupState.getT2(), followupState.getT3(), followupState.getT4()));
                alphaBeta(childTree);
                int value = childTree.getTreeValue();
                bestValue = Math.min(bestValue, value);
                alphaBetaTree.setTreeValue(bestValue);
                alphaBetaTree.setBeta(Math.min(alphaBetaTree.getBeta(), bestValue));

                if (alphaBetaTree.getBeta() <= alphaBetaTree.getAlpha()) {
                    break;
                }
            }
        }


    }




}
