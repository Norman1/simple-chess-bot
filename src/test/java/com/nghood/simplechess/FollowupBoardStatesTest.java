package com.nghood.simplechess;


import com.nghood.simplechess.io.BoardPrinter;
import com.nghood.simplechess.model.BoardState;
import com.nghood.simplechess.model.FollowupBoardStates;
import com.nghood.simplechess.model.Piece;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.util.function.Tuple6;

import java.util.List;

import static com.nghood.simplechess.model.Piece.*;
import static com.nghood.simplechess.model.Piece.BLACK_ROOK;


public class FollowupBoardStatesTest {


    private Piece[][] knightsTest = {
            {WHITE_KNIGHT, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null}};


    private Piece[][] rooksTest = {
            {WHITE_ROOK, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {BLACK_PAWN, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null}};

    private Piece[][] whitePawnTest = {
            {null, null, null, null, null, null, null, null},
            {null, null, WHITE_PAWN, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null}};


    @Test
    @DisplayName("Knight needs to work correctly")
    public void testKnights() {
        BoardState initialState = new BoardState();
        initialState.setChessBoard(knightsTest);
        BoardPrinter boardPrinter = new BoardPrinter();
        FollowupBoardStates followupBoardStates = new FollowupBoardStates(initialState);
        var followupStates = followupBoardStates.getFollowupStates();
        System.out.println(followupStates.size());
        for (var followupState : followupStates) {
            boardPrinter.printBoard(followupState.getT6().getChessBoard());
            System.out.println();
        }
    }


    @Test
    @DisplayName("Rooks need to work correctly")
    public void testRooks() {
        BoardState initialState = new BoardState();
        initialState.setChessBoard(rooksTest);
        BoardPrinter boardPrinter = new BoardPrinter();
        FollowupBoardStates followupBoardStates = new FollowupBoardStates(initialState);
        var followupStates = followupBoardStates.getFollowupStates();
        System.out.println(followupStates.size());
        for (var followupState : followupStates) {
            boardPrinter.printBoard(followupState.getT6().getChessBoard());
            System.out.println("Left white rook moved:"+followupState.getT6().isLeftWhiteRookMoved());
            System.out.println("Right white rook moved:"+followupState.getT6().isRightBlackRookMoved());
        }
    }

    @Test
    @DisplayName("WhitePawns need to work correctly")
    public void testWhitePawn() {
        BoardState initialState = new BoardState();
        initialState.setChessBoard(whitePawnTest);
        BoardPrinter boardPrinter = new BoardPrinter();
        FollowupBoardStates followupBoardStates = new FollowupBoardStates(initialState);
        var followupStates = followupBoardStates.getFollowupStates();
        System.out.println(followupStates.size());
        for (var followupState : followupStates) {
            boardPrinter.printBoard(followupState.getT6().getChessBoard());
            System.out.println("EnPassantVulnerable: "+followupState.getT6().getEnPassantVulnerablePawn());
            System.out.println();
        }
    }


}
