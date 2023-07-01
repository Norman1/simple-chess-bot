package com.nghood.simplechess;


import com.nghood.simplechess.io.BoardPrinter;
import com.nghood.simplechess.model.BoardState;
import com.nghood.simplechess.model.FollowupBoardStates;
import com.nghood.simplechess.model.Piece;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.util.function.Tuple6;

import java.util.List;


public class FollowupBoardStatesTest {

    @Test
    @DisplayName("Knights needs to work correctly")
    public void testKnights(){
        BoardState initialState = new BoardState();
        initialState.setupInitialBoard();
        initialState.getChessBoard()[5][5] = Piece.WHITE_KNIGHT;
        BoardPrinter boardPrinter = new BoardPrinter();
        FollowupBoardStates followupBoardStates = new FollowupBoardStates(initialState);
        var followupStates = followupBoardStates.getFollowupStates();
        System.out.println(followupStates.size());
        for(var followupState : followupStates){
            boardPrinter.printBoard(followupState.getT6().getChessBoard());
            System.out.println();
        }


    }
}
