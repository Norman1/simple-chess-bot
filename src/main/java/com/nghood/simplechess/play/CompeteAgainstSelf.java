package com.nghood.simplechess.play;

import com.nghood.simplechess.evaluation.*;
import com.nghood.simplechess.io.BoardPrinter;
import com.nghood.simplechess.model.BoardState;
import com.nghood.simplechess.utils.TimeMeasurement;
import reactor.util.function.Tuple2;

import java.sql.Time;

/**
 * Makes the bot competes against itself and prints the board each turn.
 */
public class CompeteAgainstSelf {

    private static final int MAX_TURNS = 500;

    private static void compete(){
        TimeMeasurement.start();
        BoardState boardState = new BoardState();
        boardState.setupInitialBoard();
        BoardPrinter boardPrinter = new BoardPrinter();
        boardPrinter.printBoard(boardState);
        for(int i = 0; i < MAX_TURNS; i++){
            System.out.println();
            System.out.println("=================");
            System.out.println("Turn: "+boardState.getTurn());
            String movingPlayer = boardState.isWhitePlayerMove()? "White":"Black";
            System.out.println("Moving player: "+movingPlayer);
            BestMoveCalculation bestMoveCalculator = new BestMoveCalculatorAlphaBeta();
            Tuple2<String,BoardState> bestMove = bestMoveCalculator.calculateBestMove(boardState);
            if(bestMove == null){
                break;
            }
            System.out.println("Performing move "+bestMove.getT1());
            boardPrinter.printBoard(bestMove.getT2());
            boardState = bestMove.getT2();
        }
        TimeMeasurement.stop(TimeMeasurement.Category.ALL);
        TimeMeasurement.printTimes();
    }


    public static void main(String[] args){
        compete();
    }


}
