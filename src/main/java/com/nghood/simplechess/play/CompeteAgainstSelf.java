package com.nghood.simplechess.play;

import com.nghood.simplechess.evaluation.*;
import com.nghood.simplechess.io.BoardPrinter;
import com.nghood.simplechess.model.BoardState;
import reactor.util.function.Tuple2;

/**
 * Makes the bot competes against itself and prints the board each turn.
 */
public class CompeteAgainstSelf {

    private static final int MAX_TURNS = 500;

    private static void compete(){
        long startTime = System.currentTimeMillis();
        BoardState boardState = new BoardState();
        boardState.setupInitialBoard();
        BoardPrinter boardPrinter = new BoardPrinter();
        boardPrinter.printBoard(boardState.getChessBoard());
        for(int i = 0; i < MAX_TURNS; i++){
            System.out.println();
            System.out.println("=================");
            System.out.println("Turn: "+boardState.getTurn());
            String movingPlayer = boardState.isWhitePlayerMove()? "White":"Black";
            System.out.println("Moving player: "+movingPlayer);
         //   SimpleBestMoveCalculator bestMoveCalculator = new SimpleBestMoveCalculator();
          //  BestMoveCalculation bestMoveCalculator = new BestMoveCalculatorSimpleMinimax();
           // BestMoveCalculation bestMoveCalculator = new BestMoveCalculatorSimpleAlphaBeta();
            BestMoveCalculation bestMoveCalculator = new BestMoveCalculatorAlphaBeta();
            Tuple2<String,BoardState> bestMove = bestMoveCalculator.calculateBestMove(boardState);
            if(bestMove == null){
                break;
            }
            System.out.println("Performing move "+bestMove.getT1());
            boardPrinter.printBoard(bestMove.getT2().getChessBoard());
            boardState = bestMove.getT2();
        }
        long endTime = System.currentTimeMillis();
        long timeSpent = (endTime - startTime) / 1000;
        System.out.println("Time spent on whole game: " + timeSpent + " seconds");
    }


    public static void main(String[] args){
        compete();
    }


}
