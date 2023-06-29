package com.nghood.simplechess.io;

import com.nghood.simplechess.model.BoardState;
import com.nghood.simplechess.model.Piece;

public class BoardPrinter {

    public void printBoard(Piece[][] pieces) {


        for (int row = 7; row >= 0; row--) {
            System.out.print(row +1+"  ");
            for (int column = 0; column < 8; column++) {
                System.out.print(getStringRepresentation(pieces[row][column])+"  ");
            }
            System.out.println();

        }
       // System.out.println("---------------------");
        System.out.println("   a  b  c  d  e  f  g  h");
    }

    private String getStringRepresentation(Piece piece){
        if(piece == null){
            return "-";
        }
            String out = "-";
            switch (piece){
                case WHITE_BISHOP:
                    out = "B";
                    break;
                case BLACK_BISHOP:
                    out = "b";
                    break;
                case WHITE_PAWN:
                    out = "P";
                    break;
                case BLACK_PAWN:
                    out = "p";
                    break;
                case WHITE_ROOK:
                    out = "R";
                    break;
                case BLACK_ROOK:
                    out = "r";
                    break;
                case WHITE_KNIGHT:
                    out = "N";
                    break;
                case BLACK_KNIGHT:
                    out = "n";
                    break;
                case WHITE_KING:
                    out = "K";
                    break;
                case BLACK_KING:
                    out = "k";
                    break;
                case WHITE_QUEEN:
                    out = "Q";
                    break;
                case BLACK_QUEEN:
                    out = "q";
                    break;
            }
            return out;
    }


    public static void main(String[] args){
        BoardState boardState = new BoardState();
        boardState.setupInitialBoard();
        new BoardPrinter().printBoard(boardState.getChessBoard());
    }
}
