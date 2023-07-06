package com.nghood.simplechess.evaluation;

import com.nghood.simplechess.io.MoveTransformer;
import com.nghood.simplechess.model.*;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple4;
import reactor.util.function.Tuple6;
import reactor.util.function.Tuples;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BestMoveCalculatorAlphaBeta implements BestMoveCalculation {

    private static final int MAX_DEPTH = 4;
    private int amountTraversedNodes = 0;
    private static int allTraversedNoded = 0;

    @Override
    public Tuple2<String, BoardState> calculateBestMove(BoardState initialState) {
        long startTime = System.currentTimeMillis();

        AlphaBetaTree alphaBetaTree = new AlphaBetaTree();


        alphaBetaTree.setCurrentDepth(MAX_DEPTH);


        alphaBetaTree.setCurrentState(initialState);
        alphaBetaTree.setAlpha(Integer.MIN_VALUE);
        alphaBetaTree.setBeta(Integer.MAX_VALUE);
        alphaBeta(alphaBetaTree);

        int treeValue = alphaBetaTree.getTreeValue();
        printCalculatedMovePath(alphaBetaTree, treeValue);
        Optional<AlphaBetaTree> bestChildTreeOpt = alphaBetaTree.getChildTrees().stream().filter(child -> child.getTreeValue() == treeValue).findAny();

        String moveString = null;
        AlphaBetaTree bestChildTree = null;
        if (bestChildTreeOpt.isPresent()) {
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
        System.out.println("MiniMax value: " + treeValue);
        if (bestChildTreeOpt.isEmpty()) {
            return null;
        }

        System.out.println("Immediate value: " + Evaluation.getBoardValue(bestChildTree.getCurrentState()));

        return Tuples.of(moveString, bestChildTree.getCurrentState());
    }

    private void printCalculatedMovePath(AlphaBetaTree alphaBetaTree, int minimaxValue) {
        String printString = "Calculated move sequence: ";
        while (true) {
            Optional<AlphaBetaTree> bestChildTreeOpt = alphaBetaTree.getChildTrees().stream().filter(child -> child.getTreeValue() == minimaxValue).findAny();
            if (bestChildTreeOpt.isEmpty()) {
                break;
            }
            AlphaBetaTree bestChildTree = bestChildTreeOpt.get();
            Tuple4<Integer, Integer, Integer, Integer> moves = bestChildTree.getMovesToGetToPosition();
            String moveString = MoveTransformer.getMove(moves.getT1(), moves.getT2(), moves.getT3(), moves.getT4());
            printString += moveString + ", ";
            alphaBetaTree = bestChildTree;
        }
        System.out.println(printString);
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
                Piece capturingPiece = followupState.getT6().getPieceAt(followupState.getT3(),followupState.getT4());
                int capturingPieceValue = Evaluation.getAbsolutePieceValue(capturingPiece);
                value -= capturingPieceValue;
            }
        }

        // Prio 2: captures
        Piece previousPieceOnPosition = initialState.getPieceAt(followupState.getT3(),followupState.getT4());
        Piece movingPiece = followupState.getT6().getPieceAt(followupState.getT3(),followupState.getT4());
        int movingPieceValue = Evaluation.getAbsolutePieceValue(movingPiece);
        if (previousPieceOnPosition != null) {
            int capturedPieceValue = Evaluation.getAbsolutePieceValue(previousPieceOnPosition);
            value += 10 * capturedPieceValue;
            value -= movingPieceValue;
        }
        // Prio 3: non captures
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

    private boolean isKingTaken(BoardState boardState) {
        int kingCount = 0;
        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                if (boardState.getPieceAt(row,column) != null) {
                    if (boardState.getPieceAt(row,column) == Piece.WHITE_KING ||boardState.getPieceAt(row,column) == Piece.BLACK_KING) {
                        kingCount++;
                    }
                }
            }
        }

        return kingCount != 2;
    }

    private int countPieces(BoardState boardState) {
        int pieceCount = 0;
        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                if (boardState.getPieceAt(row,column) != null) {
                    pieceCount++;
                }
            }
        }
        return pieceCount;
    }


    public List<Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState>> getNonQuietFollowups(List<Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState>> followups,AlphaBetaTree alphaBetaTree) {
        int pieceCount = countPieces(alphaBetaTree.getCurrentState());

        var nonQuietFollowups = followups.stream().filter(followup -> countPieces(followup.getT6()) != pieceCount).collect(Collectors.toList());
        // remove followups where black or white gets a worse result than the guaranteed value
 //       nonQuietFollowups =nonQuietFollowups.stream().filter( followup -> {
//            int value = Evaluation.getBoardValue(followup.getT6());
//            Integer guaranteedWhiteMinValue = alphaBetaTree.getGuaranteedWhiteMinValue();
//            Integer guaranteedBlackMaxValue = alphaBetaTree.getGuaranteedBlackMaxValue();
//            if (guaranteedWhiteMinValue != null && value > guaranteedWhiteMinValue) {
//                return false;
//            }
//            else if (guaranteedBlackMaxValue != null && value < guaranteedBlackMaxValue) {
//                return false;
//            }
//            return true;
//        }).collect(Collectors.toList());


        return nonQuietFollowups;
    }

    private int evaluatePosition(AlphaBetaTree alphaBetaTree) {
        int currentStateValue = Evaluation.getBoardValue(alphaBetaTree.getCurrentState());
        Integer guaranteedWhiteMinValue = alphaBetaTree.getGuaranteedWhiteMinValue();
        Integer guaranteedBlackMaxValue = alphaBetaTree.getGuaranteedBlackMaxValue();
        if (guaranteedWhiteMinValue != null && currentStateValue < guaranteedWhiteMinValue) {
            return guaranteedWhiteMinValue;
        } else if (guaranteedBlackMaxValue != null && currentStateValue > guaranteedBlackMaxValue) {
            return guaranteedBlackMaxValue;
        }
        return currentStateValue;
    }


    private void alphaBeta(AlphaBetaTree alphaBetaTree) {
        amountTraversedNodes++;
 //       var opponentFollowup = FollowupBoardStates.getFollowups(alphaBetaTree.getCurrentState(), null, true);
  //      AttackBoardState opponentAttack = new AttackBoardStateCalculator().calculateAttackBoardState(alphaBetaTree.getCurrentState(), opponentFollowup);

        AttackBoardState opponentAttack = AttackBoardStateCalculator2.getAttackBoardState(alphaBetaTree.getCurrentState(),!alphaBetaTree.getCurrentState().isWhitePlayerMove());

        var followupBoardStates = FollowupBoardStates.getFollowups(alphaBetaTree.getCurrentState(), opponentAttack);
        List<Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState>> followupStates = followupBoardStates;

        if (alphaBetaTree.getCurrentDepth() == 0) {
            int depth0Value = Evaluation.getBoardValue(alphaBetaTree.getCurrentState());
            // wasWhiteTurn --> followup is black turn (or not...)
            boolean wasWhiteTurn = alphaBetaTree.getCurrentState().isWhitePlayerMove();
            if (wasWhiteTurn) {
                alphaBetaTree.setGuaranteedWhiteMinValue(depth0Value);
            } else {
                alphaBetaTree.setGuaranteedBlackMaxValue(depth0Value);
            }
        }


        if (isKingTaken(alphaBetaTree.getCurrentState())) {
            alphaBetaTree.setTreeValue(evaluatePosition(alphaBetaTree));
            return;
        }

        if (alphaBetaTree.getCurrentDepth() <= 0) {

            List<Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState>> nonQuietFollowups = getNonQuietFollowups(followupStates, alphaBetaTree);

            if (nonQuietFollowups.isEmpty()) {
                alphaBetaTree.setTreeValue(evaluatePosition(alphaBetaTree));
                return;
            } else {
                followupStates = nonQuietFollowups;
            }

        }


        sortMoves(followupStates, alphaBetaTree.getLastMovedPiece(), alphaBetaTree.getCurrentState(), opponentAttack);

        if (alphaBetaTree.getCurrentState().isWhitePlayerMove()) {
            int bestValue = Integer.MIN_VALUE;
            for (var followupState : followupStates) {
                AlphaBetaTree childTree = new AlphaBetaTree();
                alphaBetaTree.getChildTrees().add(childTree);
                childTree.setGuaranteedBlackMaxValue(alphaBetaTree.getGuaranteedBlackMaxValue());
                childTree.setGuaranteedWhiteMinValue(alphaBetaTree.getGuaranteedWhiteMinValue());
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
                childTree.setGuaranteedBlackMaxValue(alphaBetaTree.getGuaranteedBlackMaxValue());
                childTree.setGuaranteedWhiteMinValue(alphaBetaTree.getGuaranteedWhiteMinValue());
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
