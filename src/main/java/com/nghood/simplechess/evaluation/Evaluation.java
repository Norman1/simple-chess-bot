package com.nghood.simplechess.evaluation;

import com.nghood.simplechess.model.BoardState;
import com.nghood.simplechess.model.Piece;

public class Evaluation {
    // https://www.chessprogramming.org/Simplified_Evaluation_Function
    private static final int PAWN_VALUE = 100;
    private static final int KNIGHT_VALUE = 320;
    private static final int BISHOP_VALUE = 330;
    private static final int ROOK_VALUE = 500;
    private static final int QUEEN_VALUE = 900;
    private static final int KING_VALUE = 100000;



    // 0,0 is a1. For black the matrix needs to get horizontally reverted
    private static final int[][] pawnPositionValues = {
            {0,  0,  0,  0,  0,  0,  0,  0},
            {5, 10, 10,-20,-20, 10, 10,  5},
            {5, -5,-10,  0,  0,-10, -5,  5},
            {0,  0,  0, 20, 20,  0,  0,  0},
            {5,  5, 10, 25, 25, 10,  5,  5},
            {10, 10, 20, 30, 30, 20, 10, 10},
            {50, 50, 50, 50, 50, 50, 50, 50},
            {0,  0,  0,  0,  0,  0,  0,  0},
    };

    private static final int[][] knightPositionValues = {
            {-50,-40,-30,-30,-30,-30,-40,-50},
            {-40,-20,  0,  5,  5,  0,-20,-40},
            {-30,  5, 10, 15, 15, 10,  5,-30},
            {-30,  0, 15, 20, 20, 15,  0,-30},
            {-30,  5, 15, 20, 20, 15,  5,-30},
            {-30,  0, 10, 15, 15, 10,  0,-30},
            {-40,-20,  0,  0,  0,  0,-20,-40},
            {-50,-40,-30,-30,-30,-30,-40,-50}
    };

    private static final int[][] bishopPositionValues = {
            {-20,-10,-10,-10,-10,-10,-10,-20},
            {-10,  5,  0,  0,  0,  0,  5,-10},
            {-10, 10, 10, 10, 10, 10, 10,-10},
            {-10,  0, 10, 10, 10, 10,  0,-10},
            {-10,  5,  5, 10, 10,  5,  5,-10},
            {-10,  0,  5, 10, 10,  5,  0,-10},
            {-10,  0,  0,  0,  0,  0,  0,-10},
            {-20,-10,-10,-10,-10,-10,-10,-20}
    };

    private static final int[][] rookPositionValues = {
            {0,  0,  0,  5,  5,  0,  0,  0},
            {-5,  0,  0,  0,  0,  0,  0, -5},
            {-5,  0,  0,  0,  0,  0,  0, -5},
            {-5,  0,  0,  0,  0,  0,  0, -5},
            {-5,  0,  0,  0,  0,  0,  0, -5},
            {-5,  0,  0,  0,  0,  0,  0, -5},
            {5, 10, 10, 10, 10, 10, 10,  5},
            {0,  0,  0,  0,  0,  0,  0,  0}
    };

    private static final int[][] queenPositionValues = {
            {-20,-10,-10, -5, -5,-10,-10,-20},
            {-10,  0,  5,  0,  0,  0,  0,-10},
            {-10,  5,  5,  5,  5,  5,  0,-10},
            {0,  0,  5,  5,  5,  5,  0, -5},
            {-5,  0,  5,  5,  5,  5,  0, -5},
            {-10,  0,  5,  5,  5,  5,  0,-10},
            {-10,  0,  0,  0,  0,  0,  0,-10},
            {-20,-10,-10, -5, -5,-10,-10,-20}
    };


    // TODO: no end game currently, only middle game
    private static final int[][] kingPositionValues = {
            {20, 30, 10,  0,  0, 10, 30, 20},
            { 20, 20,  0,  0,  0,  0, 20, 20},
            {-10,-20,-20,-20,-20,-20,-20,-10},
            {-20,-30,-30,-40,-40,-30,-30,-20},
            {-30,-40,-40,-50,-50,-40,-40,-30},
            {-30,-40,-40,-50,-50,-40,-40,-30},
            {-30,-40,-40,-50,-50,-40,-40,-30},
            {-30,-40,-40,-50,-50,-40,-40,-30}
    };


    public static int getBoardValue(BoardState boardState){
        int boardValue = 0;
        Piece[][] board = boardState.getChessBoard();
        for(int row = 0; row < 8; row++){
            for(int column =0; column < 8; column ++){
                boardValue += getPieceValue(board[row][column]);
                boardValue += getPositionValue(row,column,board[row][column]);

            }
        }
        return boardValue;
    }

    private static int getPositionValue(int row, int column,Piece piece){
        if(piece == null){
            return 0;
        }
        boolean isWhite = piece.ordinal() <= 5;
        int matrixColumn = column;
        int matrixRow = isWhite? row : 7-row;
        switch(piece){
            case WHITE_PAWN:
                return pawnPositionValues[matrixRow][matrixColumn];
            case WHITE_ROOK:
                return rookPositionValues[matrixRow][matrixColumn];
            case WHITE_KNIGHT:
                return knightPositionValues[matrixRow][matrixColumn];
            case WHITE_BISHOP:
                return bishopPositionValues[matrixRow][matrixColumn];
            case WHITE_QUEEN:
                return queenPositionValues[matrixRow][matrixColumn];
            case WHITE_KING:
                return kingPositionValues[matrixRow][matrixColumn];
            case BLACK_KING:
                return -kingPositionValues[matrixRow][matrixColumn];
            case BLACK_PAWN:
                return -pawnPositionValues[matrixRow][matrixColumn];
            case BLACK_ROOK:
                return -rookPositionValues[matrixRow][matrixColumn];
            case BLACK_KNIGHT:
                return -knightPositionValues[matrixRow][matrixColumn];
            case BLACK_BISHOP:
                return -bishopPositionValues[matrixRow][matrixColumn];
            case BLACK_QUEEN:
                return -queenPositionValues[matrixRow][matrixColumn];
        }
        return -1;
    }

    public static int getAbsolutePieceValue(Piece piece){
        int pieceValue = getPieceValue(piece);
        return Math.abs(pieceValue);
    }

    private static int getPieceValue(Piece piece){
        if(piece == null){
            return 0;
        }
        switch (piece){
            case WHITE_PAWN:
                return PAWN_VALUE;
            case WHITE_ROOK:
                return ROOK_VALUE;
            case WHITE_KNIGHT:
                return KNIGHT_VALUE;
            case WHITE_BISHOP:
                return BISHOP_VALUE;
            case WHITE_QUEEN:
                return QUEEN_VALUE;
            case WHITE_KING:
                return KING_VALUE;
            case BLACK_KING:
                return -KING_VALUE;
            case BLACK_PAWN:
                return -PAWN_VALUE;
            case BLACK_ROOK:
                return -ROOK_VALUE;
            case BLACK_KNIGHT:
                return -KNIGHT_VALUE;
            case BLACK_BISHOP:
                return -BISHOP_VALUE;
            case BLACK_QUEEN:
                return -QUEEN_VALUE;
        }
        return 0;
    }



}
