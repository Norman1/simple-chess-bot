package com.nghood.simplechess;

import com.nghood.simplechess.io.BoardPrinter;
import com.nghood.simplechess.model.BoardState;
import com.nghood.simplechess.model.Piece;
import com.nghood.simplechess.utils.TimeMeasurement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class BoardStateTest {
    @Test
    @DisplayName("Changing the state does not affect the copy")
   public void  testGetCopy(){
        BoardState initialState = new BoardState();
        initialState.setupInitialBoard();
        BoardPrinter boardPrinter = new BoardPrinter();

        BoardState copy = initialState.getCopy();
        // must not affect the copy
        initialState.getChessBoard()[4][4] = Piece.WHITE_BISHOP;
        System.out.println("Initial state");
        boardPrinter.printBoard(initialState.getChessBoard());
        System.out.println("Copy");
        boardPrinter.printBoard(copy.getChessBoard());

    }

}
