package com.nghood.simplechess;

import com.nghood.simplechess.io.BoardPrinter;
import com.nghood.simplechess.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.util.function.Tuples;

import static com.nghood.simplechess.model.Piece.*;

public class AttackBoardStateCalculatorTest {


    private final Piece[][] attackBoardStateTest = {
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, WHITE_PAWN, null},
            {null, null, null, null, null, BLACK_PAWN, null, null},
            {null, null, null, null, null, null, null, null},
            {WHITE_PAWN, BLACK_PAWN, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, WHITE_ROOK}};


    @Test
    @DisplayName("AttackBoardStateCalculator must work correctly")
    public void testAttackBoardStateCalculator() {
        BoardState initialState = new BoardState();
        initialState.setChessBoard(attackBoardStateTest);
        initialState.setEnPassantVulnerablePawn(Tuples.of(3,1));
        BoardPrinter boardPrinter = new BoardPrinter();
        boardPrinter.printBoard(initialState.getChessBoard());
        FollowupBoardStates followupBoardStates = new FollowupBoardStates(initialState);
        var followupStates = followupBoardStates.getFollowupStates();
        AttackBoardState attackBoardState = new AttackBoardStateCalculator().calculateAttackBoardState(initialState,followupStates);
        for(int row = 0; row < 8; row++){
            for(int column = 0; column < 8; column++){
                System.out.print(attackBoardState.getAttackStates()[row][column]+" ");
            }
            System.out.println();
        }
    }



}
