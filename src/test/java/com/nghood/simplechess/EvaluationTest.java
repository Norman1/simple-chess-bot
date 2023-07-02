package com.nghood.simplechess;

import com.nghood.simplechess.io.BoardPrinter;
import com.nghood.simplechess.model.BoardState;
import com.nghood.simplechess.model.Evaluation;
import com.nghood.simplechess.model.Piece;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.nghood.simplechess.model.Piece.WHITE_QUEEN;

public class EvaluationTest {


    private final Piece[][] valueTest = {
            {null, null, null, null, null, null, null, null},
            {null, WHITE_QUEEN, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null}};


    @Test
    @DisplayName("Evaluation needs to work correctly")
    public void testEvaluation() {
        BoardState boardState = new BoardState();
        boardState.setChessBoard(valueTest);
        BoardPrinter boardPrinter = new BoardPrinter();
        boardPrinter.printBoard(boardState.getChessBoard());
        System.out.println("Evaluation: "+Evaluation.getBoardValue(boardState));
    }

    @Test
    @DisplayName("Evaluation needs to work correctly (more pieces)")
    public void testEvaluation2() {
        BoardState boardState = new BoardState();
        boardState.setupInitialBoard();
        BoardPrinter boardPrinter = new BoardPrinter();
        boardPrinter.printBoard(boardState.getChessBoard());
        System.out.println("Evaluation: "+Evaluation.getBoardValue(boardState));
    }


}
