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

        System.out.println("   a\u0020  b\u0020 c\u0020  d\u0020  e  f   g \u0020h");
    }

    private String getStringRepresentation(Piece piece){
        if(piece == null){
            return "\u2009-\u2009";
        }
            String out = "-";
            switch (piece){
                case WHITE_BISHOP:
                    out = "\u2657";
                    break;
                case BLACK_BISHOP:
                    out = "\u265D";
                    break;
                case WHITE_PAWN:
                    out = "\u2659";
                    break;
                case BLACK_PAWN:
                    out = "\u265F";
                    break;
                case WHITE_ROOK:
                    out = "\u2656";
                    break;
                case BLACK_ROOK:
                    out = "\u265C";
                    break;
                case WHITE_KNIGHT:
                    out = "\u2658";
                    break;
                case BLACK_KNIGHT:
                    out = "\u265E";
                    break;
                case WHITE_KING:
                  out =   "\u2654";
                    break;
                case BLACK_KING:
                    out = "\u265A";
                    break;
                case WHITE_QUEEN:
                    out = "\u2655";
                    break;
                case BLACK_QUEEN:
                    out = "\u265B";
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
