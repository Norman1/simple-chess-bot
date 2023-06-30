package com.nghood.simplechess.model;

import lombok.Data;

import reactor.util.function.Tuple5;
import reactor.util.function.Tuple6;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains and calculates all the followup possible board states from the initial state.
 */
@Data
public class FollowupBoardStates {

    private BoardState initialState;
    // first row, first column, second row, second column
    private List<Tuple6<Integer,Integer,Integer,Integer,Piece,BoardState>> followupStates = new ArrayList<>();


    public FollowupBoardStates(BoardState initialState){
        this.initialState  = initialState;
        for(int row = 0; row < 8; row ++){
            for(int column = 0; column < 8; column ++){
                Piece piece = initialState.getChessBoard()[row][column];
            }
        }
    }


}
