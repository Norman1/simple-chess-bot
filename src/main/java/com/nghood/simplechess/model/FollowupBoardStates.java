package com.nghood.simplechess.model;

import com.nghood.simplechess.utils.TimeMeasurement;
import lombok.Data;

import reactor.util.function.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class contains and calculates all the followup possible board states from the initial state.
 */
@Data
public class FollowupBoardStates {

    private FollowupBoardStates() {

    }


    public static List<Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState>> getFollowups
            (BoardState initialState, AttackBoardState attackBoardState, boolean invertCurrentPlayer) {
        List<Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState>> followupStates = new ArrayList<>();
        TimeMeasurement.start();
        boolean isWhitePlayerMove = initialState.isWhitePlayerMove();
        if (invertCurrentPlayer) {
            isWhitePlayerMove = !isWhitePlayerMove;
        }
        TimeMeasurement.start();
        boolean isKingLost = isKingLost(isWhitePlayerMove, initialState);
        TimeMeasurement.stop(TimeMeasurement.Category.KING_LOSS_CHECK);
        if (isKingLost) {
            TimeMeasurement.stop(TimeMeasurement.Category.FOLLOWUP_BOARD_STATES);
            return followupStates;
        }

        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                Piece piece = initialState.getPieceAt(row,column);
                if (piece == null) {
                    continue;
                }
                switch (piece) {
                    case WHITE_KNIGHT:
                        if (isWhitePlayerMove) {
                            followupStates.addAll(handleKnight(row, column, Piece.WHITE_KNIGHT,initialState));
                        }
                        break;
                    case BLACK_KNIGHT:
                        if (!isWhitePlayerMove) {
                            followupStates.addAll(handleKnight(row, column, Piece.BLACK_KNIGHT,initialState));
                        }
                        break;
                    case WHITE_ROOK:
                        if (isWhitePlayerMove) {
                            followupStates.addAll(handleRook(row, column, Piece.WHITE_ROOK,initialState));
                        }
                        break;
                    case BLACK_ROOK:
                        if (!isWhitePlayerMove) {
                            followupStates.addAll(handleRook(row, column, Piece.BLACK_ROOK,initialState));
                        }
                        break;
                    case WHITE_PAWN:
                        if (isWhitePlayerMove) {
                            followupStates.addAll(handleWhitePawn(row, column,initialState));
                        }
                        break;
                    case BLACK_PAWN:
                        if (!isWhitePlayerMove) {
                            followupStates.addAll(handleBlackPawn(row, column,initialState));
                        }
                        break;
                    case WHITE_BISHOP:
                        if (isWhitePlayerMove) {
                            followupStates.addAll(handleBishop(row, column, piece,initialState));
                        }
                        break;
                    case BLACK_BISHOP:
                        if (!isWhitePlayerMove) {
                            followupStates.addAll(handleBishop(row, column, piece,initialState));
                        }
                        break;
                    case WHITE_QUEEN:
                        if (isWhitePlayerMove) {
                            followupStates.addAll(handleQueen(row, column, piece,initialState));
                        }
                        break;
                    case BLACK_QUEEN:
                        if (!isWhitePlayerMove) {
                            followupStates.addAll(handleQueen(row, column, piece,initialState));
                        }
                        break;
                    case WHITE_KING:
                        if (isWhitePlayerMove) {
                            followupStates.addAll(handleKing(row, column, piece, attackBoardState,initialState));
                        }
                        break;
                    case BLACK_KING:
                        if (!isWhitePlayerMove) {
                            followupStates.addAll(handleKing(row, column, piece, attackBoardState,initialState));
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        setKingAndRookMovements(followupStates);
        followupStates.forEach(followup -> followup.getT6().nextMove());

        // remove enpassantvulnerable pawns from the previous player
        // after the turn it is the opposites players turn.
        followupStates.forEach(followup -> {
            Tuple2<Integer, Integer> enPassantVulnerablePawn = followup.getT6().getEnPassantVulnerablePawn();
            if (enPassantVulnerablePawn != null) {
                boolean isWhitePlayerTurn = followup.getT6().isWhitePlayerMove();
                Piece enPassantPiece = followup.getT6().getPieceAt(enPassantVulnerablePawn.getT1(),enPassantVulnerablePawn.getT2());
                boolean isWhiteEnPassantPawn = enPassantPiece == Piece.WHITE_PAWN;
                boolean isBlackEnPassantPawn = enPassantPiece == Piece.BLACK_PAWN;
                if ((isWhitePlayerTurn && isBlackEnPassantPawn) || (!isWhitePlayerTurn && isWhiteEnPassantPawn)) {
                } else {
                    followup.getT6().setEnPassantVulnerablePawn(null);
                }
            }
        });

        // if there is a move where we can capture the opponent king, remove all other moves
        var kingTakeStateOpt = followupStates.stream().filter(follow -> isKingTaken(follow.getT6())).findAny();
        if (kingTakeStateOpt.isPresent()) {
            var kingTakeState = kingTakeStateOpt.get();
            followupStates.clear();
            followupStates.add(kingTakeState);
        }
        TimeMeasurement.stop(TimeMeasurement.Category.FOLLOWUP_BOARD_STATES);
        return followupStates;
    }

    private static boolean  isKingTaken(BoardState board) {
        int kingCount = 0;
        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                Piece piece = board.getPieceAt(row,column);
                if (piece != null) {
                    if (piece== Piece.WHITE_KING || piece == Piece.BLACK_KING) {
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
                Piece piece = initialState.getPieceAt(row,column);
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

    public static void setKingAndRookMovements(List<Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState>> followupStates) {
        //0,0; 0,4; 0,7; 7,0; 7,4; 7,7
        followupStates.forEach(followupState -> {
            BoardState boardState = followupState.getT6();
            if ((followupState.getT1() == 0 && followupState.getT2() == 0) || (followupState.getT3() == 0 && followupState.getT4() == 0)) {
                boardState.setLeftWhiteRookMoved(true);
            } else if (followupState.getT1() == 0 && followupState.getT2() == 4) {
                boardState.setWhiteKingMoved(true);
            } else if ((followupState.getT1() == 0 && followupState.getT2() == 7) || (followupState.getT3() == 0 && followupState.getT4() == 7)) {
                boardState.setRightWhiteRookMoved(true);
            } else if ((followupState.getT1() == 7 && followupState.getT2() == 0) || (followupState.getT3() == 7 && followupState.getT4() == 0)) {
                boardState.setLeftBlackRookMoved(true);
            } else if (followupState.getT1() == 7 && followupState.getT2() == 4) {
                boardState.setBlackKingMoved(true);
            } else if ((followupState.getT1() == 7 && followupState.getT2() == 7) || (followupState.getT3() == 7 && followupState.getT4() == 7)) {
                boardState.setRightBlackRookMoved(true);
            }
        });
    }


    private static List<Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState>> handleKing(int row, int column, Piece piece, AttackBoardState attackBoardState, BoardState initialState) {
        Tuple2<Integer, Integer> upLeft = Tuples.of(row + 1, column - 1);
        Tuple2<Integer, Integer> up = Tuples.of(row + 1, column);
        Tuple2<Integer, Integer> upRight = Tuples.of(row + 1, column + 1);
        Tuple2<Integer, Integer> left = Tuples.of(row, column - 1);
        Tuple2<Integer, Integer> right = Tuples.of(row, column + 1);
        Tuple2<Integer, Integer> downLeft = Tuples.of(row - 1, column - 1);
        Tuple2<Integer, Integer> down = Tuples.of(row - 1, column);
        Tuple2<Integer, Integer> downRight = Tuples.of(row - 1, column + 1);
        List<Tuple2<Integer, Integer>> moveLocations = List.of(upLeft, up, upRight, left, right, downLeft, down, downRight);
        moveLocations = removeErroneousLocations(moveLocations, piece,initialState);

        var followupStates = getFollowupStates(row, column, moveLocations, piece,initialState);

        boolean kingIsWhite = piece.ordinal() <= 5;
        // Do not accept castling when the king or the fields 1 left and 1 right are under attack

        // white left castling

        boolean leftWhiteCastleAttackBlocked = false;
        boolean rightWhiteCastleAttackBlocked = false;
        boolean leftBlackCastleAttackBlocked = false;
        boolean rightBlackCastleAttackBlocked = false;
        if (attackBoardState != null) {
            leftWhiteCastleAttackBlocked = attackBoardState.isFieldUnderAttack(0, 3) ||
                    attackBoardState.isFieldUnderAttack(0, 4);
            rightWhiteCastleAttackBlocked = attackBoardState.isFieldUnderAttack(0, 4) ||
                    attackBoardState.isFieldUnderAttack(0, 5);

            leftBlackCastleAttackBlocked = attackBoardState.isFieldUnderAttack(7, 3) ||
                    attackBoardState.isFieldUnderAttack(7, 4);
            rightBlackCastleAttackBlocked = attackBoardState.isFieldUnderAttack(7, 4) ||
                    attackBoardState.isFieldUnderAttack(7, 5);
        }


        List<Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState>> castlingFollowupStates = new ArrayList<>();
        if (kingIsWhite && !initialState.isWhiteKingMoved() && !initialState.isLeftWhiteRookMoved() &&initialState.getPieceAt(0,1) == null &&initialState.getPieceAt(0,2)  == null && initialState.getPieceAt(0,3)  == null && !leftWhiteCastleAttackBlocked) {
            Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState> whiteLeftFollowup = Tuples.of(row, column, row, column - 2, Piece.WHITE_KING, initialState.getCopy());
            whiteLeftFollowup.getT6().setPieceAt(0,0,null);
            whiteLeftFollowup.getT6().setPieceAt(0,4,null);
            whiteLeftFollowup.getT6().setPieceAt(0,2,Piece.WHITE_KING);
            whiteLeftFollowup.getT6().setPieceAt(0,3,Piece.WHITE_ROOK);
            whiteLeftFollowup.getT6().setLeftWhiteRookMoved(true);
            castlingFollowupStates.add(whiteLeftFollowup);
        }
        // white right castling
        if (kingIsWhite && !initialState.isWhiteKingMoved() && !initialState.isRightWhiteRookMoved() &&
               initialState.getPieceAt(0,5)  == null && initialState.getPieceAt(0,6) == null && !rightWhiteCastleAttackBlocked) {
            Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState> whiteRightFollowup = Tuples.of(row, column, row, column + 2, Piece.WHITE_KING, initialState.getCopy());
            whiteRightFollowup.getT6().setPieceAt(0,7,null);
            whiteRightFollowup.getT6().setPieceAt(0,4,null);
            whiteRightFollowup.getT6().setPieceAt(0,6,Piece.WHITE_KING);
            whiteRightFollowup.getT6().setPieceAt(0,5,Piece.WHITE_ROOK);
            whiteRightFollowup.getT6().setRightWhiteRookMoved(true);
            castlingFollowupStates.add(whiteRightFollowup);
        }

        // black left castling
        if (!kingIsWhite && !initialState.isBlackKingMoved() && !initialState.isLeftBlackRookMoved() &&initialState.getPieceAt(7,1) == null && initialState.getPieceAt(7,2)== null && initialState.getPieceAt(7,3) == null && !leftBlackCastleAttackBlocked) {
            Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState> blackLeftFollowup = Tuples.of(row, column, row, column - 2, Piece.BLACK_KING, initialState.getCopy());
            blackLeftFollowup.getT6().setPieceAt(7,0,null);
            blackLeftFollowup.getT6().setPieceAt(7,4,null);
            blackLeftFollowup.getT6().setPieceAt(7,2,Piece.BLACK_KING);
            blackLeftFollowup.getT6().setPieceAt(7,3,Piece.BLACK_ROOK);
            blackLeftFollowup.getT6().setLeftBlackRookMoved(true);
            castlingFollowupStates.add(blackLeftFollowup);
        }

        // black right castling
        if (!kingIsWhite && !initialState.isBlackKingMoved() && !initialState.isRightBlackRookMoved() &&initialState.getPieceAt(7,5)  == null && initialState.getPieceAt(7,6)  == null && !rightBlackCastleAttackBlocked) {
            Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState> blackRightFollowup = Tuples.of(row, column, row, column + 2, Piece.BLACK_KING, initialState.getCopy());
            blackRightFollowup.getT6().setPieceAt(7,7,null);
            blackRightFollowup.getT6().setPieceAt(7,4,null);
            blackRightFollowup.getT6().setPieceAt(7,6,Piece.BLACK_KING);
            blackRightFollowup.getT6().setPieceAt(7,5,Piece.BLACK_ROOK);
            blackRightFollowup.getT6().setRightBlackRookMoved(true);
            castlingFollowupStates.add(blackRightFollowup);
        }

        followupStates.addAll(castlingFollowupStates);
        return followupStates;
    }


    private static List<Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState>> handleBlackPawn(int row, int column, BoardState initialState) {
        List<Tuple2<Integer, Integer>> moveLocations = new ArrayList<>();

        // move 1 down
        boolean down1IsTaken = isLocationOwnedBySomeone(row - 1, column,initialState);
        if (!down1IsTaken) {
            moveLocations.add(Tuples.of(row - 1, column));
        }

        // move 2 down
        boolean down2IsTaken = !isLocationInBounds(Tuples.of(row - 2, column)) || isLocationOwnedBySomeone(row - 2, column,initialState);
        if (row == 6 && !down1IsTaken) {
            if (!down2IsTaken) {
                moveLocations.add(Tuples.of(row - 2, column));
            }
        }

        // attack
        Tuple2<Integer, Integer> attackLeft = Tuples.of(row - 1, column - 1);
        Tuple2<Integer, Integer> attackRight = Tuples.of(row - 1, column + 1);
        if (isLocationInBounds(attackLeft) && isLocationOwnedByOpponent(row - 1, column - 1, Piece.BLACK_PAWN,initialState)) {
            moveLocations.add(attackLeft);
        }
        if (isLocationInBounds(attackRight) && isLocationOwnedByOpponent(row - 1, column + 1, Piece.BLACK_PAWN,initialState)) {
            moveLocations.add(attackRight);
        }

        var followupStates = getFollowupStates(row, column, moveLocations, Piece.BLACK_PAWN,initialState);

        // take en passant
        Tuple2<Integer, Integer> whiteEnPassantVulnerablePawn = initialState.getEnPassantVulnerablePawn();
        if (whiteEnPassantVulnerablePawn != null) {
            // have to be on 4th row and 1 column to the left or right
            if (row == 3 && (whiteEnPassantVulnerablePawn.getT2() == column - 1 || whiteEnPassantVulnerablePawn.getT2() == column + 1)) {
                Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState> takeEnPassantFollowupState = Tuples.of(row, column, row - 1, whiteEnPassantVulnerablePawn.getT2(), Piece.BLACK_PAWN, initialState.getCopy());
                takeEnPassantFollowupState.getT6().setPieceAt(whiteEnPassantVulnerablePawn.getT1(),whiteEnPassantVulnerablePawn.getT2(),null);
                takeEnPassantFollowupState.getT6().setPieceAt(row,column,null);
                takeEnPassantFollowupState.getT6().setPieceAt(whiteEnPassantVulnerablePawn.getT1() - 1,whiteEnPassantVulnerablePawn.getT2(),Piece.BLACK_PAWN);
                followupStates.add(takeEnPassantFollowupState);
            }
        }

        // promote
        var promotionStates = followupStates.stream().filter(state -> state.getT3() == 0).toList();
        followupStates.removeAll(promotionStates);

        for (var promotionState : promotionStates) {
            // black bishop
            Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState> blackBishopState =
                    Tuples.of(promotionState.getT1(), promotionState.getT2(), promotionState.getT3(), promotionState.getT4(),
                            promotionState.getT5(), promotionState.getT6().getCopy());
            blackBishopState.getT6().setPieceAt(blackBishopState.getT3(),blackBishopState.getT4(),Piece.BLACK_BISHOP);
            followupStates.add(blackBishopState);

            // black knight
            Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState> blackKnightState =
                    Tuples.of(promotionState.getT1(), promotionState.getT2(), promotionState.getT3(), promotionState.getT4(),
                            promotionState.getT5(), promotionState.getT6().getCopy());
            blackKnightState.getT6().setPieceAt(blackKnightState.getT3(),blackKnightState.getT4(),Piece.BLACK_KNIGHT);
            followupStates.add(blackKnightState);

            // black rook
            Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState> blackRookState =
                    Tuples.of(promotionState.getT1(), promotionState.getT2(), promotionState.getT3(), promotionState.getT4(),
                            promotionState.getT5(), promotionState.getT6().getCopy());
            blackRookState.getT6().setPieceAt(blackRookState.getT3(),blackRookState.getT4(),Piece.BLACK_ROOK);
            followupStates.add(blackRookState);

            // white queen
            Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState> blackQueenState =
                    Tuples.of(promotionState.getT1(), promotionState.getT2(), promotionState.getT3(), promotionState.getT4(),
                            promotionState.getT5(), promotionState.getT6().getCopy());
            blackQueenState.getT6().setPieceAt(blackQueenState.getT3(),blackQueenState.getT4(),Piece.BLACK_QUEEN);
            followupStates.add(blackQueenState);
        }

        // moving 2 up is the second entry if up1IsTaken == up2IsTaken == false
        if (!down1IsTaken && !down2IsTaken && row == 1) {
            followupStates.get(1).getT6().setEnPassantVulnerablePawn(Tuples.of(row - 2, column));
        }

        return followupStates;
    }

    private static List<Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState>> handleWhitePawn(int row, int column, BoardState initialState) {
        List<Tuple2<Integer, Integer>> moveLocations = new ArrayList<>();


        // move 1 up
        boolean up1IsTaken = isLocationOwnedBySomeone(row + 1, column,initialState);
        if (!up1IsTaken) {
            moveLocations.add(Tuples.of(row + 1, column));
        }

        // move 2 up
        boolean up2IsTaken = !isLocationInBounds(Tuples.of(row + 2, column)) || isLocationOwnedBySomeone(row + 2, column,initialState);
        if (row == 1 && !up1IsTaken) {
            if (!up2IsTaken) {
                moveLocations.add(Tuples.of(row + 2, column));
            }
        }

        // attack
        Tuple2<Integer, Integer> attackLeft = Tuples.of(row + 1, column - 1);
        Tuple2<Integer, Integer> attackRight = Tuples.of(row + 1, column + 1);
        if (isLocationInBounds(attackLeft) && isLocationOwnedByOpponent(row + 1, column - 1, Piece.WHITE_PAWN,initialState)) {
            moveLocations.add(attackLeft);
        }
        if (isLocationInBounds(attackRight) && isLocationOwnedByOpponent(row + 1, column + 1, Piece.WHITE_PAWN,initialState)) {
            moveLocations.add(attackRight);
        }

        var followupStates = getFollowupStates(row, column, moveLocations, Piece.WHITE_PAWN,initialState);

        // take en passant
        Tuple2<Integer, Integer> blackEnPassantVulnerablePawn = initialState.getEnPassantVulnerablePawn();
        if (blackEnPassantVulnerablePawn != null) {
            // have to be on 4th row and 1 column to the left or right
            if (row == 4 && (blackEnPassantVulnerablePawn.getT2() == column - 1 || blackEnPassantVulnerablePawn.getT2() == column + 1)) {
                Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState> takeEnPassantFollowupState = Tuples.of(row, column, row + 1, blackEnPassantVulnerablePawn.getT2(), Piece.WHITE_PAWN, initialState.getCopy());
                takeEnPassantFollowupState.getT6().setPieceAt(blackEnPassantVulnerablePawn.getT1(),blackEnPassantVulnerablePawn.getT2(),null);
                takeEnPassantFollowupState.getT6().setPieceAt(row,column,null);
                takeEnPassantFollowupState.getT6().setPieceAt(blackEnPassantVulnerablePawn.getT1() + 1,blackEnPassantVulnerablePawn.getT2(),Piece.WHITE_PAWN);
                followupStates.add(takeEnPassantFollowupState);
            }
        }

        // promote
        var promotionStates = followupStates.stream().filter(state -> state.getT3() == 7).toList();
        followupStates.removeAll(promotionStates);

        for (var promotionState : promotionStates) {
            // white bishop
            Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState> whiteBishopState =
                    Tuples.of(promotionState.getT1(), promotionState.getT2(), promotionState.getT3(), promotionState.getT4(),
                            promotionState.getT5(), promotionState.getT6().getCopy());
            whiteBishopState.getT6().setPieceAt(whiteBishopState.getT3(),whiteBishopState.getT4(),Piece.WHITE_BISHOP);
            followupStates.add(whiteBishopState);

            // white knight
            Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState> whiteKnightState =
                    Tuples.of(promotionState.getT1(), promotionState.getT2(), promotionState.getT3(), promotionState.getT4(),
                            promotionState.getT5(), promotionState.getT6().getCopy());
            whiteKnightState.getT6().setPieceAt(whiteKnightState.getT3(),whiteKnightState.getT4(),Piece.WHITE_KNIGHT);
            followupStates.add(whiteKnightState);

            // white rook
            Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState> whiteRookState =
                    Tuples.of(promotionState.getT1(), promotionState.getT2(), promotionState.getT3(), promotionState.getT4(),
                            promotionState.getT5(), promotionState.getT6().getCopy());
            whiteRookState.getT6().setPieceAt(whiteRookState.getT3(),whiteRookState.getT4(),Piece.WHITE_ROOK);
            followupStates.add(whiteRookState);

            // white queen
            Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState> whiteQueenState =
                    Tuples.of(promotionState.getT1(), promotionState.getT2(), promotionState.getT3(), promotionState.getT4(),
                            promotionState.getT5(), promotionState.getT6().getCopy());
            whiteQueenState.getT6().setPieceAt(whiteQueenState.getT3(),whiteQueenState.getT4(),Piece.WHITE_QUEEN);
            followupStates.add(whiteQueenState);
        }

        // moving 2 up is the second entry if up1IsTaken == up2IsTaken == false
        if (!up1IsTaken && !up2IsTaken && row == 1) {
            followupStates.get(1).getT6().setEnPassantVulnerablePawn(Tuples.of(row + 2, column));
        }

        return followupStates;
    }

    private static List<Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState>> handleQueen(int row, int column, Piece piece,BoardState initialState) {
        var result = handleBishop(row, column, piece,initialState);
        // the isRookMoved stuff is no problem since if the queen is there the rook is gone from there
        result.addAll(handleRook(row, column, piece,initialState));
        return result;
    }

    private static List<Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState>> handleBishop(int row, int column, Piece piece,BoardState initialState) {
        List<Tuple2<Integer, Integer>> moveLocations = new ArrayList<>();
        // left up
        for (int i = 1; i < 8; i++) {
            Tuple2<Integer, Integer> location = Tuples.of(row + i, column - i);
            if (!isLocationInBounds(location)) {
                break;
            }
            if (isLocationOwnedBySelf(location.getT1(), location.getT2(), piece,initialState)) {
                break;
            } else if (isLocationOwnedByOpponent(location.getT1(), location.getT2(), piece,initialState)) {
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
            }
            if (isLocationOwnedBySelf(location.getT1(), location.getT2(), piece,initialState)) {
                break;
            } else if (isLocationOwnedByOpponent(location.getT1(), location.getT2(), piece,initialState)) {
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
            }
            if (isLocationOwnedBySelf(location.getT1(), location.getT2(), piece,initialState)) {
                break;
            } else if (isLocationOwnedByOpponent(location.getT1(), location.getT2(), piece,initialState)) {
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
            }
            if (isLocationOwnedBySelf(location.getT1(), location.getT2(), piece,initialState)) {
                break;
            } else if (isLocationOwnedByOpponent(location.getT1(), location.getT2(), piece,initialState)) {
                moveLocations.add(Tuples.of(row - i, column + i));
                break;
            }
            moveLocations.add(Tuples.of(row - i, column + i));
        }

        return getFollowupStates(row, column, moveLocations, piece,initialState);
    }

    private static List<Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState>> handleRook(int row, int column, Piece piece,BoardState initialState) {
        List<Tuple2<Integer, Integer>> moveLocations = new ArrayList<>();
        // row up
        for (int i = row + 1; i < 8; i++) {
            if (isLocationOwnedBySelf(i, column, piece,initialState)) {
                break;
            } else if (isLocationOwnedByOpponent(i, column, piece,initialState)) {
                moveLocations.add(Tuples.of(i, column));
                break;
            }
            moveLocations.add(Tuples.of(i, column));
        }
        // row down
        for (int i = row - 1; i >= 0; i--) {
            if (isLocationOwnedBySelf(i, column, piece,initialState)) {
                break;
            } else if (isLocationOwnedByOpponent(i, column, piece,initialState)) {
                moveLocations.add(Tuples.of(i, column));
                break;
            }
            moveLocations.add(Tuples.of(i, column));
        }

        // column right
        for (int i = column + 1; i < 8; i++) {
            if (isLocationOwnedBySelf(row, i, piece,initialState)) {
                break;
            } else if (isLocationOwnedByOpponent(row, i, piece,initialState)) {
                moveLocations.add(Tuples.of(row, i));
                break;
            }
            moveLocations.add(Tuples.of(row, i));
        }

        // column left
        for (int i = column - 1; i >= 0; i--) {
            if (isLocationOwnedBySelf(row, i, piece,initialState)) {
                break;
            } else if (isLocationOwnedByOpponent(row, i, piece,initialState)) {
                moveLocations.add(Tuples.of(row, i));
                break;
            }
            moveLocations.add(Tuples.of(row, i));
        }

        var followupStates = getFollowupStates(row, column, moveLocations, piece,initialState);

        for (var followupState : followupStates) {
            // white queen rook initial position
            if (row == 0 && column == 0) {
                followupState.getT6().setLeftWhiteRookMoved(true);
            }
            // white king rook initial position
            if (row == 0 && column == 7) {
                followupState.getT6().setRightWhiteRookMoved(true);
            }

            // black queen rook initial position
            if (row == 7 && column == 0) {
                followupState.getT6().setLeftBlackRookMoved(true);
            }

            // black king rook initial position
            if (row == 7 && column == 7) {
                followupState.getT6().setRightBlackRookMoved(true);
            }
        }
        return followupStates;
    }

    private static List<Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState>> handleKnight(int row, int column, Piece piece,BoardState initialState) {
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
        jumpLocations = removeErroneousLocations(jumpLocations, piece,initialState);
        return getFollowupStates(row, column, jumpLocations, piece,initialState);
    }

    private static List<Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState>> getFollowupStates(int initialRow,
         int initialColumn, List<Tuple2<Integer, Integer>> moveLocations, Piece piece, BoardState initialState) {
        List<Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState>> out = new ArrayList<>();
        for (Tuple2<Integer, Integer> moveLocation : moveLocations) {
            BoardState copy = initialState.getCopy();
            copy.setPieceAt(initialRow,initialColumn,null); ;
            copy.setPieceAt(moveLocation.getT1(),moveLocation.getT2(),piece);
            Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState> newSituation = Tuples.of(
                    initialRow, initialColumn, moveLocation.getT1(), moveLocation.getT2(), piece, copy);
            out.add(newSituation);
        }
        return out;

    }


    private static List<Tuple2<Integer, Integer>> removeErroneousLocations(List<Tuple2<Integer, Integer>> locations, Piece piece,BoardState initialState) {
        List<Tuple2<Integer, Integer>> out = removeLocationsOutsideOfBounds(locations);
        out = removeLocationsOwnedBySelf(out, piece,initialState);
        return out;
    }

    private static List<Tuple2<Integer, Integer>> removeLocationsOutsideOfBounds(List<Tuple2<Integer, Integer>> locations) {
        return locations.stream().filter(FollowupBoardStates::isLocationInBounds).collect(Collectors.toList());
    }

    private static boolean isLocationInBounds(Tuple2<Integer, Integer> location) {
        return location.getT1() >= 0 && location.getT2() >= 0 && location.getT1() < 8 && location.getT2() < 8;
    }


    private static List<Tuple2<Integer, Integer>> removeLocationsOwnedBySelf(List<Tuple2<Integer, Integer>> locations, Piece piece,BoardState initialState) {
        return locations.stream().filter(location -> !isLocationOwnedBySelf(location.getT1(), location.getT2(), piece,initialState)).collect(Collectors.toList());
    }

    private static boolean isLocationOwnedBySelf(int row, int column, Piece testPiece,BoardState initialState) {
        boolean pieceIsWhite = testPiece.ordinal() <= 5;
        Piece pieceOnLocation = initialState.getPieceAt(row,column);
        if (pieceOnLocation == null) {
            return false;
        }
        boolean locationIsWhite = pieceOnLocation.ordinal() <= 5;
        return pieceIsWhite == locationIsWhite;
    }

    private static boolean isLocationOwnedByOpponent(int row, int column, Piece testPiece,BoardState initialState) {
        boolean pieceIsWhite = testPiece.ordinal() <= 5;
        Piece pieceOnLocation = initialState.getPieceAt(row,column);
        if (pieceOnLocation == null) {
            return false;
        }
        boolean locationIsWhite = pieceOnLocation.ordinal() <= 5;
        return pieceIsWhite != locationIsWhite;
    }


    private static boolean isLocationOwnedBySomeone(int row, int column, BoardState initialState) {
        return initialState.getPieceAt(row,column) != null;
    }


}
