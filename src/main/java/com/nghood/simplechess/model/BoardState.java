package com.nghood.simplechess.model;


import lombok.Data;

import static com.nghood.simplechess.model.Piece.*;

/**
 * This class contains all the information about the board for a specific point in time.
 */
@Data
public class BoardState {
    /*
    *
     */
    private int turn;
    private boolean isWhitePlayerMove;



    /*
    * a11 is 0-0
    *
     */
    private Piece[][] chessBoard;



    public  void setupInitialBoard(){
        Piece[][] initialBoard ={ {WHITE_ROOK,WHITE_KNIGHT,WHITE_BISHOP,WHITE_QUEEN,WHITE_KING,WHITE_BISHOP,WHITE_KNIGHT,WHITE_ROOK},
        {WHITE_PAWN,WHITE_PAWN,WHITE_PAWN,WHITE_PAWN,WHITE_PAWN,WHITE_PAWN,WHITE_PAWN,WHITE_PAWN},
        {null,null,null,null,null,null,null,null},
        {null,null,null,null,null,null,null,null},
        {null,null,null,null,null,null,null,null},
        {null,null,null,null,null,null,null,null},
        {BLACK_PAWN,BLACK_PAWN,BLACK_PAWN,BLACK_PAWN,BLACK_PAWN,BLACK_PAWN,BLACK_PAWN,BLACK_PAWN},
        {BLACK_ROOK,BLACK_KNIGHT,BLACK_BISHOP,BLACK_QUEEN,BLACK_KING,BLACK_BISHOP,BLACK_KNIGHT,BLACK_ROOK} };

       chessBoard = initialBoard;
    }



}
