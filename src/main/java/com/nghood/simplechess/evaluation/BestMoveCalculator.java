package com.nghood.simplechess.evaluation;

import com.nghood.simplechess.io.BoardPrinter;
import com.nghood.simplechess.model.BoardState;
import com.nghood.simplechess.model.FollowupBoardStates;

public class BestMoveCalculator {


    // TODO debug state currently, also with return value
    public BoardState calculateBestMove(BoardState initialState){
        FollowupBoardStates followupBoardStates = new FollowupBoardStates(initialState);
        var followupStates = followupBoardStates.getFollowupStates();
        var bestState = followupStates.get(0);
        int bestValue = Evaluation.getBoardValue(bestState.getT6());
        for(var followupState : followupStates){
            int testValue = Evaluation.getBoardValue(followupState.getT6());
            if(initialState.isWhitePlayerMove() && testValue > bestValue){
                bestValue = testValue;
                bestState = followupState;
            }else if(!initialState.isWhitePlayerMove() && testValue < bestValue){
                bestValue = testValue;
                bestState = followupState;
            }
        }
        System.out.println("Calculated best move with the value: "+bestValue);
        return bestState.getT6();
    }

//    @Test
//    @DisplayName("Queen needs to work correctly")
//    public void testQueen() {
//        BoardState initialState = new BoardState();
//        initialState.setChessBoard(queenTest);
//        initialState.setRightWhiteRookMoved(true);
//        BoardPrinter boardPrinter = new BoardPrinter();
//        FollowupBoardStates followupBoardStates = new FollowupBoardStates(initialState);
//        var followupStates = followupBoardStates.getFollowupStates();
//        System.out.println(followupStates.size());
//        for (var followupState : followupStates) {
//            boardPrinter.printBoard(followupState.getT6().getChessBoard());
//            System.out.println();
//        }
//    }



}
