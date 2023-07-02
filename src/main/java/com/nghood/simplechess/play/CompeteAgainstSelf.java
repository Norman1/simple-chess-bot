package com.nghood.simplechess.play;

import com.nghood.simplechess.evaluation.BestMoveCalculation;
import com.nghood.simplechess.evaluation.BestMoveCalculatorSimpleAlphaBeta;
import com.nghood.simplechess.evaluation.BestMoveCalculatorSimpleMinimax;
import com.nghood.simplechess.evaluation.SimpleBestMoveCalculator;
import com.nghood.simplechess.io.BoardPrinter;
import com.nghood.simplechess.model.BoardState;
import reactor.util.function.Tuple2;

/**
 * Makes the bot competes against itself and prints the board each turn.
 */
public class CompeteAgainstSelf {

    private static final int MAX_TURNS = 30;

    private static void compete(){
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
            BestMoveCalculation bestMoveCalculator = new BestMoveCalculatorSimpleAlphaBeta();
            Tuple2<String,BoardState> bestMove = bestMoveCalculator.calculateBestMove(boardState);
            System.out.println("Performing move "+bestMove.getT1());
            boardPrinter.printBoard(bestMove.getT2().getChessBoard());
            boardState = bestMove.getT2();
        }

    }


    public static void main(String[] args){
        compete();
    }


}
