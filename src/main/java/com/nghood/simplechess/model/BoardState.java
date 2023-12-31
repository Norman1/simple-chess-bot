package com.nghood.simplechess.model;


import com.nghood.simplechess.utils.TimeMeasurement;
import lombok.Data;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.sql.Time;

import static com.nghood.simplechess.model.Piece.*;

/**
 * This class contains all the information about the board for a specific point in time.
 */
@Data
public class BoardState {
    private int turn = 1;
    private boolean isWhitePlayerMove;
    private boolean isWhiteKingMoved;
    private boolean isBlackKingMoved;
    private boolean isLeftWhiteRookMoved;
    private boolean isRightWhiteRookMoved;
    private boolean isLeftBlackRookMoved;
    private boolean isRightBlackRookMoved;
    private Tuple2<Integer, Integer> enPassantVulnerablePawn = null;


    /*
     * a11 is 0-0
     * first rows then columns
     */
    private Piece[] chessBoard;

    public Piece getPieceAt(int row, int column) {
        Piece piece = chessBoard[(row * 8) + column];
        return piece;
    }

    public void setPieceAt(int row, int column, Piece piece) {
        chessBoard[(row * 8) + column] = piece;
    }


    public void setupInitialBoard() {

        Piece[] initialBoard = {WHITE_ROOK, WHITE_KNIGHT, WHITE_BISHOP, WHITE_QUEEN, WHITE_KING, WHITE_BISHOP, WHITE_KNIGHT, WHITE_ROOK,
                WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN,
                null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null,
                BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN,
                BLACK_ROOK, BLACK_KNIGHT, BLACK_BISHOP, BLACK_QUEEN, BLACK_KING, BLACK_BISHOP, BLACK_KNIGHT, BLACK_ROOK};


        isWhitePlayerMove = true;
        chessBoard = initialBoard;
    }

    public void nextMove() {
        turn++;
        isWhitePlayerMove = !isWhitePlayerMove;
    }

    public BoardState getCopy() {
        TimeMeasurement.start();
        BoardState copy = new BoardState();
        copy.turn = turn;
        copy.isWhitePlayerMove = isWhitePlayerMove;
        copy.isWhiteKingMoved = isWhiteKingMoved;
        copy.isBlackKingMoved = isBlackKingMoved;
        copy.isLeftWhiteRookMoved = isLeftWhiteRookMoved;
        copy.isRightWhiteRookMoved = isRightWhiteRookMoved;
        copy.isLeftBlackRookMoved = isLeftBlackRookMoved;
        copy.isRightBlackRookMoved = isRightBlackRookMoved;
        if (enPassantVulnerablePawn != null) {
            copy.enPassantVulnerablePawn = Tuples.of(enPassantVulnerablePawn.getT1(), enPassantVulnerablePawn.getT2());
        }
        TimeMeasurement.start();
        copy.chessBoard = chessBoard.clone();

        TimeMeasurement.stop(TimeMeasurement.Category.CLONE_PIECE_ARRAY);
        TimeMeasurement.stop(TimeMeasurement.Category.COPY_BOARD);
        return copy;
    }


}
