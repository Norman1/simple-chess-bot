package com.nghood.simplechess.model;

import com.nghood.simplechess.utils.TimeMeasurement;
import lombok.Data;

import reactor.util.function.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Calculates the AttackBoardState
 */
@Data
public class AttackBoardStateCalculator2 {

    private AttackBoardStateCalculator2() {

    }


    public static AttackBoardState getAttackBoardState(BoardState initialState, boolean isWhitePlayerRequest) {
        TimeMeasurement.start();
        AttackBoardState attackBoardState = new AttackBoardState();


        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                Piece piece = initialState.getPieceAt(row, column);
                if (piece == null) {
                    continue;
                }
                switch (piece) {
                    case WHITE_KNIGHT:
                        if (isWhitePlayerRequest) {
                            handleKnight(row, column, Piece.WHITE_KNIGHT, initialState, attackBoardState);
                        }
                        break;
                    case BLACK_KNIGHT:
                        if (!isWhitePlayerRequest) {
                            handleKnight(row, column, Piece.BLACK_KNIGHT, initialState, attackBoardState);
                        }
                        break;
                    case WHITE_ROOK:
                        if (isWhitePlayerRequest) {
                            handleRook(row, column, Piece.WHITE_ROOK, initialState, attackBoardState);
                        }
                        break;
                    case BLACK_ROOK:
                        if (!isWhitePlayerRequest) {
                            handleRook(row, column, Piece.WHITE_ROOK, initialState, attackBoardState);
                        }
                        break;
                    case WHITE_PAWN:
                        if (isWhitePlayerRequest) {
                            handleWhitePawn(row, column, initialState, attackBoardState);
                        }
                        break;
                    case BLACK_PAWN:
                        if (!isWhitePlayerRequest) {
                            handleBlackPawn(row, column, initialState, attackBoardState);
                        }
                        break;
                    case WHITE_BISHOP:
                        if (isWhitePlayerRequest) {
                            handleBishop(row, column, piece, initialState, attackBoardState);
                        }
                        break;
                    case BLACK_BISHOP:
                        if (!isWhitePlayerRequest) {
                            handleBishop(row, column, piece, initialState, attackBoardState);
                        }
                        break;
                    case WHITE_QUEEN:
                        if (isWhitePlayerRequest) {
                            handleQueen(row, column, piece, initialState, attackBoardState);
                        }
                        break;
                    case BLACK_QUEEN:
                        if (!isWhitePlayerRequest) {
                            handleQueen(row, column, piece, initialState, attackBoardState);
                        }
                        break;
                    case WHITE_KING:
                        if (isWhitePlayerRequest) {
                            handleKing(row, column, piece, attackBoardState, initialState);
                        }
                        break;
                    case BLACK_KING:
                        if (!isWhitePlayerRequest) {
                            handleKing(row, column, piece, attackBoardState, initialState);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        TimeMeasurement.stop(TimeMeasurement.Category.ATTACK_BOARD_STATE);
        return attackBoardState;
    }

    private static boolean isKingTaken(BoardState board) {
        int kingCount = 0;
        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                Piece piece = board.getPieceAt(row, column);
                if (piece != null) {
                    if (piece == Piece.WHITE_KING || piece == Piece.BLACK_KING) {
                        kingCount++;
                    }
                }
            }
        }
        return kingCount != 2;
    }


    // we allow king captures but then there are no followup states for the player having lost his king.
    private static boolean isKingLost(boolean checkWhiteKing, BoardState initialState) {
        boolean kingPresent = false;
        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                Piece piece = initialState.getPieceAt(row, column);
                if (piece != null) {
                    if (checkWhiteKing && piece == Piece.WHITE_KING) {
                        kingPresent = true;
                    } else if (!checkWhiteKing & piece == Piece.BLACK_KING) {
                        kingPresent = true;
                    }
                }
            }
        }

        return !kingPresent;
    }

    private static void handleKing(int row, int column, Piece piece, AttackBoardState attackBoardState, BoardState initialState) {
        Tuple2<Integer, Integer> upLeft = Tuples.of(row + 1, column - 1);
        Tuple2<Integer, Integer> up = Tuples.of(row + 1, column);
        Tuple2<Integer, Integer> upRight = Tuples.of(row + 1, column + 1);
        Tuple2<Integer, Integer> left = Tuples.of(row, column - 1);
        Tuple2<Integer, Integer> right = Tuples.of(row, column + 1);
        Tuple2<Integer, Integer> downLeft = Tuples.of(row - 1, column - 1);
        Tuple2<Integer, Integer> down = Tuples.of(row - 1, column);
        Tuple2<Integer, Integer> downRight = Tuples.of(row - 1, column + 1);
        List<Tuple2<Integer, Integer>> moveLocations = List.of(upLeft, up, upRight, left, right, downLeft, down, downRight);
       moveLocations = removeLocationsOutsideOfBounds(moveLocations);

        setFieldsUnderAttack(attackBoardState, moveLocations, piece);

    }


    private static void handleBlackPawn(int row, int column, BoardState initialState, AttackBoardState attackBoardState) {
        List<Tuple2<Integer, Integer>> moveLocations = new ArrayList<>();


        // attack
        Tuple2<Integer, Integer> attackLeft = Tuples.of(row - 1, column - 1);
        Tuple2<Integer, Integer> attackRight = Tuples.of(row - 1, column + 1);
        if (isLocationInBounds(attackLeft) && isLocationOwnedByOpponent(row - 1, column - 1, Piece.BLACK_PAWN, initialState)) {
            moveLocations.add(attackLeft);
        }
        if (isLocationInBounds(attackRight) && isLocationOwnedByOpponent(row - 1, column + 1, Piece.BLACK_PAWN, initialState)) {
            moveLocations.add(attackRight);
        }
        setFieldsUnderAttack(attackBoardState, moveLocations, Piece.BLACK_PAWN);
    }

    private static void handleWhitePawn(int row, int column, BoardState initialState, AttackBoardState attackBoardState) {
        List<Tuple2<Integer, Integer>> moveLocations = new ArrayList<>();

        // attack
        Tuple2<Integer, Integer> attackLeft = Tuples.of(row + 1, column - 1);
        Tuple2<Integer, Integer> attackRight = Tuples.of(row + 1, column + 1);
        if (isLocationInBounds(attackLeft) && isLocationOwnedByOpponent(row + 1, column - 1, Piece.WHITE_PAWN, initialState)) {
            moveLocations.add(attackLeft);
        }
        if (isLocationInBounds(attackRight) && isLocationOwnedByOpponent(row + 1, column + 1, Piece.WHITE_PAWN, initialState)) {
            moveLocations.add(attackRight);
        }

        setFieldsUnderAttack(attackBoardState, moveLocations, Piece.WHITE_PAWN);


    }

    private static void handleQueen(int row, int column, Piece piece, BoardState initialState, AttackBoardState attackBoardState) {
        handleBishop(row, column, piece, initialState, attackBoardState);
        handleRook(row, column, piece, initialState, attackBoardState);
    }

    private static void handleBishop(int row, int column, Piece piece, BoardState initialState, AttackBoardState attackBoardState) {
        List<Tuple2<Integer, Integer>> moveLocations = new ArrayList<>();
        // left up
        for (int i = 1; i < 8; i++) {
            Tuple2<Integer, Integer> location = Tuples.of(row + i, column - i);
            if (!isLocationInBounds(location)) {
                break;
            } else if (isLocationOwnedBySomeone(location.getT1(), location.getT2(), initialState)) {
                moveLocations.add(Tuples.of(row + i, column - i));
                break;
            }
            moveLocations.add(Tuples.of(row + i, column - i));
        }

        // right up
        for (int i = 1; i < 8; i++) {
            Tuple2<Integer, Integer> location = Tuples.of(row + i, column + i);
            if (!isLocationInBounds(location)) {
                break;
            } else if (isLocationOwnedBySomeone(location.getT1(), location.getT2(), initialState)) {
                moveLocations.add(Tuples.of(row + i, column + i));
                break;
            }
            moveLocations.add(Tuples.of(row + i, column + i));
        }

        // left down
        for (int i = 1; i < 8; i++) {
            Tuple2<Integer, Integer> location = Tuples.of(row - i, column - i);
            if (!isLocationInBounds(location)) {
                break;
            } else if (isLocationOwnedBySomeone(location.getT1(), location.getT2(), initialState)) {
                moveLocations.add(Tuples.of(row - i, column - i));
                break;
            }
            moveLocations.add(Tuples.of(row - i, column - i));
        }

        // right down
        for (int i = 1; i < 8; i++) {
            Tuple2<Integer, Integer> location = Tuples.of(row - i, column + i);
            if (!isLocationInBounds(location)) {
                break;
            } else if (isLocationOwnedBySomeone(location.getT1(), location.getT2(), initialState)) {
                moveLocations.add(Tuples.of(row - i, column + i));
                break;
            }
            moveLocations.add(Tuples.of(row - i, column + i));
        }
        setFieldsUnderAttack(attackBoardState, moveLocations, piece);
    }

    private static void
    handleRook(int row, int column, Piece piece, BoardState initialState, AttackBoardState attackBoardState) {
        List<Tuple2<Integer, Integer>> moveLocations = new ArrayList<>();
        // row up
        for (int i = row + 1; i < 8; i++) {
            if (isLocationOwnedBySomeone(i, column, initialState)) {
                moveLocations.add(Tuples.of(i, column));
                break;
            }
            moveLocations.add(Tuples.of(i, column));
        }
        // row down
        for (int i = row - 1; i >= 0; i--) {
            if (isLocationOwnedBySomeone(i, column, initialState)) {
                moveLocations.add(Tuples.of(i, column));
                break;
            }
            moveLocations.add(Tuples.of(i, column));
        }

        // column right
        for (int i = column + 1; i < 8; i++) {
            if (isLocationOwnedBySomeone(row, i, initialState)) {
                moveLocations.add(Tuples.of(row, i));
                break;
            }
            moveLocations.add(Tuples.of(row, i));
        }

        // column left
        for (int i = column - 1; i >= 0; i--) {
            if (isLocationOwnedBySomeone(row, i, initialState)) {
                moveLocations.add(Tuples.of(row, i));
                break;
            }
            moveLocations.add(Tuples.of(row, i));
        }


        setFieldsUnderAttack(attackBoardState, moveLocations, piece);
    }

    private static void handleKnight
            (int row, int column, Piece piece, BoardState initialState, AttackBoardState attackBoardState) {
        Tuple2<Integer, Integer> jumpLocation1 = Tuples.of(row + 2, column - 1);
        Tuple2<Integer, Integer> jumpLocation2 = Tuples.of(row + 2, column + 1);
        Tuple2<Integer, Integer> jumpLocation3 = Tuples.of(row - 2, column - 1);
        Tuple2<Integer, Integer> jumpLocation4 = Tuples.of(row - 2, column + 1);
        Tuple2<Integer, Integer> jumpLocation5 = Tuples.of(row + 1, column - 2);
        Tuple2<Integer, Integer> jumpLocation6 = Tuples.of(row - 1, column - 2);
        Tuple2<Integer, Integer> jumpLocation7 = Tuples.of(row - 1, column + 2);
        Tuple2<Integer, Integer> jumpLocation8 = Tuples.of(row + 1, column + 2);
        List<Tuple2<Integer, Integer>> jumpLocations = List.of(jumpLocation1, jumpLocation2, jumpLocation3,
                jumpLocation4, jumpLocation5, jumpLocation6, jumpLocation7, jumpLocation8);
        jumpLocations = removeLocationsOutsideOfBounds(jumpLocations);
        setFieldsUnderAttack(attackBoardState, jumpLocations, piece);
    }

    private static void setFieldsUnderAttack(AttackBoardState attackBoardState, List<Tuple2<Integer, Integer>> attacks, Piece piece) {
        for (var attack : attacks) {
            attackBoardState.setFieldUnderAttack(attack.getT1(), attack.getT2(), piece);
        }
    }

    private static List<Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState>> getFollowupStates(int initialRow,
                                                                                                         int initialColumn, List<Tuple2<Integer, Integer>> moveLocations, Piece piece, BoardState initialState) {
        List<Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState>> out = new ArrayList<>();
        for (Tuple2<Integer, Integer> moveLocation : moveLocations) {
            BoardState copy = initialState.getCopy();
            copy.setPieceAt(initialRow, initialColumn, null);
            ;
            copy.setPieceAt(moveLocation.getT1(), moveLocation.getT2(), piece);
            Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState> newSituation = Tuples.of(
                    initialRow, initialColumn, moveLocation.getT1(), moveLocation.getT2(), piece, copy);
            out.add(newSituation);
        }
        return out;

    }


    private static List<Tuple2<Integer, Integer>> removeLocationsOutsideOfBounds(List<Tuple2<Integer, Integer>> locations) {
        return locations.stream().filter(AttackBoardStateCalculator2::isLocationInBounds).collect(Collectors.toList());
    }

    private static boolean isLocationInBounds(Tuple2<Integer, Integer> location) {
        return location.getT1() >= 0 && location.getT2() >= 0 && location.getT1() < 8 && location.getT2() < 8;
    }


    private static boolean isLocationOwnedByOpponent(int row, int column, Piece testPiece, BoardState initialState) {
        boolean pieceIsWhite = testPiece.ordinal() <= 5;
        Piece pieceOnLocation = initialState.getPieceAt(row, column);
        if (pieceOnLocation == null) {
            return false;
        }
        boolean locationIsWhite = pieceOnLocation.ordinal() <= 5;
        return pieceIsWhite != locationIsWhite;
    }


    private static boolean isLocationOwnedBySomeone(int row, int column, BoardState initialState) {
        return initialState.getPieceAt(row, column) != null;
    }


}
