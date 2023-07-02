package com.nghood.simplechess;


import com.nghood.simplechess.io.BoardPrinter;
import com.nghood.simplechess.model.BoardState;
import com.nghood.simplechess.model.FollowupBoardStates;
import com.nghood.simplechess.model.Piece;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.util.function.Tuple6;
import reactor.util.function.Tuples;

import java.util.List;

import static com.nghood.simplechess.model.Piece.*;
import static com.nghood.simplechess.model.Piece.BLACK_ROOK;


public class FollowupBoardStatesTest {


    private Piece[][] queenTest = {
            {null, null, null, null, null, null, null, null},
            {null, WHITE_QUEEN, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null}};

    private Piece[][] kingTest = {
            {WHITE_ROOK, null, null, null, WHITE_KING, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null}};

    private Piece[][] bishopTest = {
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, WHITE_BISHOP, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null}};

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
            {null, BLACK_PAWN, null, BLACK_PAWN, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null}};

    private Piece[][] blackPawnTest = {
            {null, null, null, null, null, null, null, null},
            {WHITE_PAWN, null, WHITE_PAWN, null, null, null, null, null},
            {null, BLACK_PAWN, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null}};

    private Piece[][] whitePromotionTest = {
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {WHITE_PAWN, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null}};

    private Piece[][] blackPromotionTest = {
            {null, null, null, null, null, null, null, null},
            {BLACK_PAWN, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null}};


    private Piece[][] whiteEnPassantTest = {
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {WHITE_PAWN, BLACK_PAWN, WHITE_PAWN, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null}};

    private Piece[][] blackEnPassantTest = {
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {BLACK_PAWN, WHITE_PAWN, BLACK_PAWN, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null}};



    @Test
    @DisplayName("Queen needs to work correctly")
    public void testQueen() {
        BoardState initialState = new BoardState();
        initialState.setChessBoard(queenTest);
        initialState.setRightWhiteRookMoved(true);
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
    @DisplayName("King needs to work correctly")
    public void testKing() {
        BoardState initialState = new BoardState();
        initialState.setChessBoard(kingTest);
        initialState.setRightWhiteRookMoved(true);
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
    @DisplayName("Bishops need to work correctly")
    public void testBishops() {
        BoardState initialState = new BoardState();
        initialState.setChessBoard(bishopTest);
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

    @Test
    @DisplayName("WhitePawns need to work correctly")
    public void testBlackPawn() {
        BoardState initialState = new BoardState();
        initialState.setChessBoard(blackPawnTest);
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

    @Test
    @DisplayName("White pawn promotion needs to work correctly")
    public void testWhitePawnPromotion() {
        BoardState initialState = new BoardState();
        initialState.setChessBoard(whitePromotionTest);
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
    @DisplayName("Black pawn promotion needs to work correctly")
    public void testBlackPawnPromotion() {
        BoardState initialState = new BoardState();
        initialState.setChessBoard(blackPromotionTest);
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
    @DisplayName("White en passant needs to work correctly")
    public void testWhiteEnPassant() {
        BoardState initialState = new BoardState();
        initialState.setChessBoard(whiteEnPassantTest);
        initialState.setEnPassantVulnerablePawn(Tuples.of(4,1));
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
    @DisplayName("Black en passant needs to work correctly")
    public void testBlackEnPassant() {
        BoardState initialState = new BoardState();
        initialState.setChessBoard(blackEnPassantTest);
        initialState.setEnPassantVulnerablePawn(Tuples.of(3,1));
        BoardPrinter boardPrinter = new BoardPrinter();
        FollowupBoardStates followupBoardStates = new FollowupBoardStates(initialState);
        var followupStates = followupBoardStates.getFollowupStates();
        System.out.println(followupStates.size());
        for (var followupState : followupStates) {
            boardPrinter.printBoard(followupState.getT6().getChessBoard());
            System.out.println();
        }
    }


}
