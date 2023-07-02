package com.nghood.simplechess.play;

import com.nghood.simplechess.evaluation.BestMoveCalculator;
import com.nghood.simplechess.io.BoardPrinter;
import com.nghood.simplechess.model.BoardState;

/**
 * Makes the bot competes against itself and prints the board each turn.
 */
public class CompeteAgainstSelf {

    private static final int MAX_TURNS = 10;

    private static void compete(){
        BoardState boardState = new BoardState();
        boardState.setupInitialBoard();
        BoardPrinter boardPrinter = new BoardPrinter();
        boardPrinter.printBoard(boardState.getChessBoard());
        for(int i = 0; i < MAX_TURNS; i++){
            System.out.println("Turn: "+boardState.getTurn());
            BestMoveCalculator bestMoveCalculator = new BestMoveCalculator();
            BoardState bestMove = bestMoveCalculator.calculateBestMove(boardState);
            boardPrinter.printBoard(bestMove.getChessBoard());
            boardState = bestMove;
        }

    }


    public static void main(String[] args){
        compete();
    }


}
