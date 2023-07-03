package com.nghood.simplechess.model;


import com.nghood.simplechess.evaluation.Evaluation;
import lombok.Data;

/**
 * A board not containing the chess pieces but information about which fields are under opponent attack.
 * -1 means that the field is not under attack. A positive number indicates the lowest piece value of the piece
 * attacking that field
 */
@Data
public class AttackBoardState {

    private int[][] attackStates = {
            {-1,-1,-1,-1,-1,-1,-1,-1},
            {-1,-1,-1,-1,-1,-1,-1,-1},
            {-1,-1,-1,-1,-1,-1,-1,-1},
            {-1,-1,-1,-1,-1,-1,-1,-1},
            {-1,-1,-1,-1,-1,-1,-1,-1},
            {-1,-1,-1,-1,-1,-1,-1,-1},
            {-1,-1,-1,-1,-1,-1,-1,-1},
            {-1,-1,-1,-1,-1,-1,-1,-1},
    };

    public void setFieldUnderAttack(int row, int column,Piece piece){
        int pieceValue = Evaluation.getAbsolutePieceValue(piece);
        attackStates[row][column] = pieceValue;
    }

    public boolean isFieldUnderAttack(int row, int column){
        return attackStates[row][column] != -1;
    }

    public int getAttackValue(int row, int column ){
        return attackStates[row][column];
    }



}
