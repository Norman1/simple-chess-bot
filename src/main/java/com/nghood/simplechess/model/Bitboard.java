package com.nghood.simplechess.model;

public class Bitboard {
    private long[] bitboards;

    public Bitboard() {
        bitboards = new long[12];
    }

    public Bitboard deepCopy() {
        Bitboard copy = new Bitboard();
        for (int i = 0; i < bitboards.length; i++) {
            copy.bitboards[i] = bitboards[i];
        }
        return copy;
    }

    public void setPiece(Piece piece, int square) {
        if (piece == null) {
            clearField(square);
        } else {
            bitboards[piece.ordinal()] |= (1L << square);
        }
    }

    private void clearField(int square) {
        for (int piece = 0; piece < 12; piece++) {
            bitboards[piece] &= ~(1L << square);
        }
    }

    // Method to get the bitboard for a specific piece
    public long getBitboard(int piece) {
        return bitboards[piece];
    }


    public Piece getPiece(int square) {
        for (int piece = 0; piece < 12; piece++) {
            if ((bitboards[piece] & (1L << square)) != 0) {
                return Piece.values()[piece];
            }
        }
        return null;
    }


    // Example usage
    public static void main(String[] args) {
        Bitboard board = new Bitboard();

        // Set a white pawn at square e2 (represented as index 52)
        board.setPiece(Piece.BLACK_KNIGHT, 0);
        board.setPiece(Piece.BLACK_KNIGHT, 1);
        board.setPiece(null, 0);
        System.out.println(board.getPiece(0));
        System.out.println(board.getPiece(1));
        System.out.println(board.getPiece(63));
        System.out.println(board.getPiece(62));


    }
}

