package com.nghood.simplechess.model;

import com.nghood.simplechess.io.BoardPrinter;
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


    public static List<Followup> getFollowups
            (BoardState initialState, AttackBoardState attackBoardState) {
        // row 1, column 1, row 2, column 2, moving piece, result board state
        List<Followup> followupStates = new ArrayList<>();
        TimeMeasurement.start();
        boolean isWhitePlayerMove = initialState.isWhitePlayerMove();

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
                    case WHITE_KNIGHT -> {
                        if (isWhitePlayerMove) {
                            followupStates.addAll(handleKnight(row, column, Piece.WHITE_KNIGHT, initialState));
                        }
                    }
                    case BLACK_KNIGHT -> {
                        if (!isWhitePlayerMove) {
                            followupStates.addAll(handleKnight(row, column, Piece.BLACK_KNIGHT, initialState));
                        }
                    }
                    case WHITE_ROOK -> {
                        if (isWhitePlayerMove) {
                            followupStates.addAll(handleRook(row, column, Piece.WHITE_ROOK, initialState));
                        }
                    }
                    case BLACK_ROOK -> {
                        if (!isWhitePlayerMove) {
                            followupStates.addAll(handleRook(row, column, Piece.BLACK_ROOK, initialState));
                        }
                    }
                    case WHITE_PAWN -> {
                        if (isWhitePlayerMove) {
                            followupStates.addAll(handleWhitePawn(row, column, initialState));
                        }
                    }
                    case BLACK_PAWN -> {
                        if (!isWhitePlayerMove) {
                            followupStates.addAll(handleBlackPawn(row, column, initialState));
                        }
                    }
                    case WHITE_BISHOP -> {
                        if (isWhitePlayerMove) {
                            followupStates.addAll(handleBishop(row, column, piece, initialState));
                        }
                    }
                    case BLACK_BISHOP -> {
                        if (!isWhitePlayerMove) {
                            followupStates.addAll(handleBishop(row, column, piece, initialState));
                        }
                    }
                    case WHITE_QUEEN -> {
                        if (isWhitePlayerMove) {
                            followupStates.addAll(handleQueen(row, column, piece, initialState));
                        }
                    }
                    case BLACK_QUEEN -> {
                        if (!isWhitePlayerMove) {
                            followupStates.addAll(handleQueen(row, column, piece, initialState));
                        }
                    }
                    case WHITE_KING -> {
                        if (isWhitePlayerMove) {
                            followupStates.addAll(handleKing(row, column, piece, attackBoardState, initialState));
                        }
                    }
                    case BLACK_KING -> {
                        if (!isWhitePlayerMove) {
                            followupStates.addAll(handleKing(row, column, piece, attackBoardState, initialState));
                        }
                    }
                    default -> {
                    }
                }
            }
        }
        setKingAndRookMovements(followupStates);
        followupStates.forEach(followup -> followup.resultState.nextMove());

        // remove enpassantvulnerable pawns from the previous player
        // after the turn it is the opposites players turn.
        followupStates.forEach(followup -> {
            Tuple2<Integer, Integer> enPassantVulnerablePawn = followup.resultState.getEnPassantVulnerablePawn();
            if (enPassantVulnerablePawn != null) {
                boolean isWhitePlayerTurn = followup.resultState.isWhitePlayerMove();
                Piece enPassantPiece = followup.resultState.getPieceAt(enPassantVulnerablePawn.getT1(),enPassantVulnerablePawn.getT2());
                boolean isWhiteEnPassantPawn = enPassantPiece == Piece.WHITE_PAWN;
                boolean isBlackEnPassantPawn = enPassantPiece == Piece.BLACK_PAWN;
                if ((isWhitePlayerTurn && isBlackEnPassantPawn) || (!isWhitePlayerTurn && isWhiteEnPassantPawn)) {
                } else {
                    followup.resultState.setEnPassantVulnerablePawn(null);
                }
            }
        });

        // if there is a move where we can capture the opponent king, remove all other moves
        var kingTakeStateOpt = followupStates.stream().filter(follow -> isKingTaken(follow.resultState)).findAny();
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

    public static void setKingAndRookMovements(List<Followup> followupStates) {
        //0,0; 0,4; 0,7; 7,0; 7,4; 7,7
        followupStates.forEach(followupState -> {
            BoardState boardState = followupState.resultState;
            if ((followupState.startRow == 0 && followupState.startColumn == 0) || (followupState.resultRow == 0 && followupState.resultColumn == 0)) {
                boardState.setLeftWhiteRookMoved(true);
            } else if (followupState.startRow == 0 && followupState.startColumn == 4) {
                boardState.setWhiteKingMoved(true);
            } else if ((followupState.startRow == 0 && followupState.startColumn == 7) || (followupState.resultRow == 0 && followupState.resultColumn == 7)) {
                boardState.setRightWhiteRookMoved(true);
            } else if ((followupState.startRow == 7 && followupState.startColumn == 0) || (followupState.resultRow == 7 && followupState.resultColumn == 0)) {
                boardState.setLeftBlackRookMoved(true);
            } else if (followupState.startRow == 7 && followupState.startColumn == 4) {
                boardState.setBlackKingMoved(true);
            } else if ((followupState.startRow == 7 && followupState.startColumn == 7) || (followupState.resultRow == 7 && followupState.resultColumn == 7)) {
                boardState.setRightBlackRookMoved(true);
            }
        });
    }


    private static List<Followup> handleKing(int row, int column, Piece piece, AttackBoardState attackBoardState, BoardState initialState) {
        Location upLeft = new Location(row+1,column -1);
        Location up = new Location(row + 1, column);
        Location upRight = new Location(row + 1, column + 1);
        Location left = new Location(row, column - 1);
        Location right = new Location(row, column + 1);
        Location downLeft = new Location(row - 1, column - 1);
        Location down = new Location(row - 1, column);
        Location downRight = new Location(row - 1, column + 1);
        List<Location> moveLocations = List.of(upLeft, up, upRight, left, right, downLeft, down, downRight);
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


        List<Followup> castlingFollowupStates = new ArrayList<>();
        if (kingIsWhite && !initialState.isWhiteKingMoved() && !initialState.isLeftWhiteRookMoved() &&initialState.getPieceAt(0,1) == null &&initialState.getPieceAt(0,2)  == null && initialState.getPieceAt(0,3)  == null && !leftWhiteCastleAttackBlocked) {
            Followup whiteLeftFollowup = new Followup(row,column, row, column -2, Piece.WHITE_KING,initialState.getCopy()) ;
            whiteLeftFollowup.resultState.setPieceAt(0,0,null);
            whiteLeftFollowup.resultState.setPieceAt(0,4,null);
            whiteLeftFollowup.resultState.setPieceAt(0,2,Piece.WHITE_KING);
            whiteLeftFollowup.resultState.setPieceAt(0,3,Piece.WHITE_ROOK);
            whiteLeftFollowup.resultState.setLeftWhiteRookMoved(true);
            castlingFollowupStates.add(whiteLeftFollowup);
        }
        // white right castling
        if (kingIsWhite && !initialState.isWhiteKingMoved() && !initialState.isRightWhiteRookMoved() &&
               initialState.getPieceAt(0,5)  == null && initialState.getPieceAt(0,6) == null && !rightWhiteCastleAttackBlocked) {
            Followup whiteRightFollowup = new Followup(row,column, row, column +2, Piece.WHITE_KING,initialState.getCopy());
            whiteRightFollowup.resultState.setPieceAt(0,7,null);
            whiteRightFollowup.resultState.setPieceAt(0,4,null);
            whiteRightFollowup.resultState.setPieceAt(0,6,Piece.WHITE_KING);
            whiteRightFollowup.resultState.setPieceAt(0,5,Piece.WHITE_ROOK);
            whiteRightFollowup.resultState.setRightWhiteRookMoved(true);
            castlingFollowupStates.add(whiteRightFollowup);
        }

        // black left castling
        if (!kingIsWhite && !initialState.isBlackKingMoved() && !initialState.isLeftBlackRookMoved() &&initialState.getPieceAt(7,1) == null && initialState.getPieceAt(7,2)== null && initialState.getPieceAt(7,3) == null && !leftBlackCastleAttackBlocked) {
           Followup blackLeftFollowup = new Followup(row,column, row, column -2, Piece.BLACK_KING, initialState.getCopy() );
            blackLeftFollowup.resultState.setPieceAt(7,0,null);
            blackLeftFollowup.resultState.setPieceAt(7,4,null);
            blackLeftFollowup.resultState.setPieceAt(7,2,Piece.BLACK_KING);
            blackLeftFollowup.resultState.setPieceAt(7,3,Piece.BLACK_ROOK);
            blackLeftFollowup.resultState.setLeftBlackRookMoved(true);
            castlingFollowupStates.add(blackLeftFollowup);
        }

        // black right castling
        if (!kingIsWhite && !initialState.isBlackKingMoved() && !initialState.isRightBlackRookMoved() &&initialState.getPieceAt(7,5)  == null && initialState.getPieceAt(7,6)  == null && !rightBlackCastleAttackBlocked) {
            Followup blackRightFollowup = new Followup(row,column,row, column +2, Piece.BLACK_KING, initialState.getCopy());
            blackRightFollowup.resultState.setPieceAt(7,7,null);
            blackRightFollowup.resultState.setPieceAt(7,4,null);
            blackRightFollowup.resultState.setPieceAt(7,6,Piece.BLACK_KING);
            blackRightFollowup.resultState.setPieceAt(7,5,Piece.BLACK_ROOK);
            blackRightFollowup.resultState.setRightBlackRookMoved(true);
            castlingFollowupStates.add(blackRightFollowup);
        }

        followupStates.addAll(castlingFollowupStates);
        return followupStates;
    }


    private static List<Followup> handleBlackPawn(int row, int column, BoardState initialState) {
        List<Location> moveLocations = new ArrayList<>();

        // move 1 down
        boolean down1IsTaken = isLocationOwnedBySomeone(row - 1, column,initialState);
        if (!down1IsTaken) {
            moveLocations.add(new Location(row - 1, column));
        }

        // move 2 down
        boolean down2IsTaken = !isLocationInBounds(new Location(row-2,column)) || isLocationOwnedBySomeone(row - 2, column,initialState);
        if (row == 6 && !down1IsTaken) {
            if (!down2IsTaken) {
                moveLocations.add(new Location(row - 2, column));
            }
        }

        // attack
        Location attackLeft = new Location(row - 1, column - 1);
        Location attackRight = new Location(row - 1, column + 1);
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
               Followup takeEnPassantFollowupState = new Followup(row, column, row-1,whiteEnPassantVulnerablePawn.getT2() ,Piece.BLACK_PAWN, initialState.getCopy());
                takeEnPassantFollowupState.resultState.setPieceAt(whiteEnPassantVulnerablePawn.getT1(),whiteEnPassantVulnerablePawn.getT2(),null);
                takeEnPassantFollowupState.resultState.setPieceAt(row,column,null);
                takeEnPassantFollowupState.resultState.setPieceAt(whiteEnPassantVulnerablePawn.getT1() - 1,whiteEnPassantVulnerablePawn.getT2(),Piece.BLACK_PAWN);
                followupStates.add(takeEnPassantFollowupState);
            }
        }

        // promote
        var promotionStates = followupStates.stream().filter(state -> state.resultRow == 0).toList();
        followupStates.removeAll(promotionStates);

        for (var promotionState : promotionStates) {
            // black bishop
            Followup blackBishopState =
                    new Followup(promotionState.startRow, promotionState.startColumn, promotionState.resultRow, promotionState.resultColumn,
                            promotionState.movingPiece, promotionState.resultState.getCopy());
            blackBishopState.resultState.setPieceAt(blackBishopState.resultRow,blackBishopState.resultColumn,Piece.BLACK_BISHOP);
            followupStates.add(blackBishopState);

            // black knight
            Followup blackKnightState =new Followup(promotionState.startRow, promotionState.startColumn, promotionState.resultRow, promotionState.resultColumn,
                            promotionState.movingPiece, promotionState.resultState.getCopy());
            blackKnightState.resultState.setPieceAt(blackKnightState.resultRow,blackKnightState.resultColumn,Piece.BLACK_KNIGHT);
            followupStates.add(blackKnightState);

            // black rook
            Followup blackRookState =
                   new Followup(promotionState.startRow, promotionState.startColumn, promotionState.resultRow, promotionState.resultColumn,
                            promotionState.movingPiece, promotionState.resultState.getCopy());
            blackRookState.resultState.setPieceAt(blackRookState.resultRow,blackRookState.resultColumn,Piece.BLACK_ROOK);
            followupStates.add(blackRookState);

            // white queen
            Followup blackQueenState =
                   new Followup(promotionState.startRow, promotionState.startColumn, promotionState.resultRow, promotionState.resultColumn,
                            promotionState.movingPiece, promotionState.resultState.getCopy());
            blackQueenState.resultState.setPieceAt(blackQueenState.resultRow,blackQueenState.resultColumn,Piece.BLACK_QUEEN);
            followupStates.add(blackQueenState);
        }

        // moving 2 up is the second entry if up1IsTaken == up2IsTaken == false
        if (!down1IsTaken && !down2IsTaken && row == 1) {
            followupStates.get(1).resultState.setEnPassantVulnerablePawn(Tuples.of(row - 2, column));
        }

        return followupStates;
    }

    private static List<Followup> handleWhitePawn(int row, int column, BoardState initialState) {
        List<Location> moveLocations = new ArrayList<>();


        // move 1 up
        boolean up1IsTaken = isLocationOwnedBySomeone(row + 1, column,initialState);
        if (!up1IsTaken) {
            moveLocations.add(new Location(row + 1, column));
        }

        // move 2 up
        boolean up2IsTaken = !isLocationInBounds(new Location(row + 2, column)) || isLocationOwnedBySomeone(row + 2, column,initialState);
        if (row == 1 && !up1IsTaken) {
            if (!up2IsTaken) {
                moveLocations.add(new Location(row + 2, column));
            }
        }

        // attack
        Location attackLeft = new Location(row + 1, column - 1);
        Location attackRight = new Location(row + 1, column + 1);
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
                Followup takeEnPassantFollowupState = new Followup(row,column, row +1, blackEnPassantVulnerablePawn.getT2(),Piece.WHITE_PAWN,initialState.getCopy());
                takeEnPassantFollowupState.resultState.setPieceAt(blackEnPassantVulnerablePawn.getT1(),blackEnPassantVulnerablePawn.getT2(),null);
                takeEnPassantFollowupState.resultState.setPieceAt(row,column,null);
                takeEnPassantFollowupState.resultState.setPieceAt(blackEnPassantVulnerablePawn.getT1() + 1,blackEnPassantVulnerablePawn.getT2(),Piece.WHITE_PAWN);
                followupStates.add(takeEnPassantFollowupState);
            }
        }

        // promote
        var promotionStates = followupStates.stream().filter(state -> state.resultRow == 7).toList();
        followupStates.removeAll(promotionStates);

        for (var promotionState : promotionStates) {
            // white bishop
            Followup whiteBishopState = new Followup(promotionState.startRow,promotionState.startColumn,
                    promotionState.resultRow, promotionState.resultColumn,Piece.WHITE_BISHOP,promotionState.resultState.getCopy());
            whiteBishopState.resultState.setPieceAt(whiteBishopState.resultRow,whiteBishopState.resultColumn,Piece.WHITE_BISHOP);
            followupStates.add(whiteBishopState);

            // white knight
            Followup whiteKnightState = new Followup(promotionState.startRow,promotionState.startColumn,promotionState.resultRow
            ,promotionState.resultColumn,promotionState.movingPiece,promotionState.resultState.getCopy());
            whiteKnightState.resultState.setPieceAt(whiteKnightState.resultRow,whiteKnightState.resultColumn,Piece.WHITE_KNIGHT);
            followupStates.add(whiteKnightState);

            // white rook
            Followup whiteRookState =
                    new Followup(promotionState.startRow, promotionState.startColumn, promotionState.resultRow, promotionState.resultColumn,
                            promotionState.movingPiece, promotionState.resultState.getCopy());
            whiteRookState.resultState.setPieceAt(whiteRookState.resultRow,whiteRookState.resultColumn,Piece.WHITE_ROOK);
            followupStates.add(whiteRookState);

            // white queen
            Followup whiteQueenState =
                    new Followup(promotionState.startRow, promotionState.startColumn, promotionState.resultRow, promotionState.resultColumn,
                            promotionState.movingPiece, promotionState.resultState.getCopy());
            whiteQueenState.resultState.setPieceAt(whiteQueenState.resultRow,whiteQueenState.resultColumn,Piece.WHITE_QUEEN);
            followupStates.add(whiteQueenState);
        }

        // moving 2 up is the second entry if up1IsTaken == up2IsTaken == false
        if (!up1IsTaken && !up2IsTaken && row == 1) {
            followupStates.get(1).resultState.setEnPassantVulnerablePawn(Tuples.of(row + 2, column));
        }

        return followupStates;
    }

    private static List<Followup> handleQueen(int row, int column, Piece piece,BoardState initialState) {
        var result = handleBishop(row, column, piece,initialState);
        // the isRookMoved stuff is no problem since if the queen is there the rook is gone from there
        result.addAll(handleRook(row, column, piece,initialState));
        return result;
    }

    private static List<Followup> handleBishop(int row, int column, Piece piece,BoardState initialState) {
        List<Location> moveLocations = new ArrayList<>();
        // left up
        for (int i = 1; i < 8; i++) {
            Location location = new Location(row + i, column - i);
            if (!isLocationInBounds(location)) {
                break;
            }
            if (isLocationOwnedBySelf(location.row, location.column, piece,initialState)) {
                break;
            } else if (isLocationOwnedByOpponent(location.row, location.column, piece,initialState)) {
                moveLocations.add(new Location(row + i, column - i));
                break;
            }
            moveLocations.add(new Location(row + i, column - i));
        }

        // right up
        for (int i = 1; i < 8; i++) {
            Location location = new Location(row + i, column + i);
            if (!isLocationInBounds(location)) {
                break;
            }
            if (isLocationOwnedBySelf(location.row, location.column, piece,initialState)) {
                break;
            } else if (isLocationOwnedByOpponent(location.row, location.column, piece,initialState)) {
                moveLocations.add(new Location(row + i, column + i));
                break;
            }
            moveLocations.add(new Location(row + i, column + i));
        }

        // left down
        for (int i = 1; i < 8; i++) {
            Location location = new Location(row - i, column - i);
            if (!isLocationInBounds(location)) {
                break;
            }
            if (isLocationOwnedBySelf(location.row, location.column, piece,initialState)) {
                break;
            } else if (isLocationOwnedByOpponent(location.row, location.column, piece,initialState)) {
                moveLocations.add(new Location(row - i, column - i));
                break;
            }
            moveLocations.add(new Location(row - i, column - i));
        }

        // right down
        for (int i = 1; i < 8; i++) {
            Location location = new Location(row - i, column + i);
            if (!isLocationInBounds(location)) {
                break;
            }
            if (isLocationOwnedBySelf(location.row, location.column, piece,initialState)) {
                break;
            } else if (isLocationOwnedByOpponent(location.row, location.column, piece,initialState)) {
                moveLocations.add(new Location(row - i, column + i));
                break;
            }
            moveLocations.add(new Location(row - i, column + i));
        }

        return getFollowupStates(row, column, moveLocations, piece,initialState);
    }

    private static List<Followup> handleRook(int row, int column, Piece piece,BoardState initialState) {
        List<Location> moveLocations = new ArrayList<>();
        // row up
        for (int i = row + 1; i < 8; i++) {
            if (isLocationOwnedBySelf(i, column, piece,initialState)) {
                break;
            } else if (isLocationOwnedByOpponent(i, column, piece,initialState)) {
                moveLocations.add(new Location(i, column));
                break;
            }
            moveLocations.add(new Location(i, column));
        }
        // row down
        for (int i = row - 1; i >= 0; i--) {
            if (isLocationOwnedBySelf(i, column, piece,initialState)) {
                break;
            } else if (isLocationOwnedByOpponent(i, column, piece,initialState)) {
                moveLocations.add(new Location(i, column));
                break;
            }
            moveLocations.add(new Location(i, column));
        }

        // column right
        for (int i = column + 1; i < 8; i++) {
            if (isLocationOwnedBySelf(row, i, piece,initialState)) {
                break;
            } else if (isLocationOwnedByOpponent(row, i, piece,initialState)) {
                moveLocations.add(new Location(row, i));
                break;
            }
            moveLocations.add(new Location(row, i));
        }

        // column left
        for (int i = column - 1; i >= 0; i--) {
            if (isLocationOwnedBySelf(row, i, piece,initialState)) {
                break;
            } else if (isLocationOwnedByOpponent(row, i, piece,initialState)) {
                moveLocations.add(new Location(row, i));
                break;
            }
            moveLocations.add(new Location(row, i));
        }

        var followupStates = getFollowupStates(row, column, moveLocations, piece,initialState);

        for (var followupState : followupStates) {
            // white queen rook initial position
            if (row == 0 && column == 0) {
                followupState.resultState.setLeftWhiteRookMoved(true);
            }
            // white king rook initial position
            if (row == 0 && column == 7) {
                followupState.resultState.setRightWhiteRookMoved(true);
            }

            // black queen rook initial position
            if (row == 7 && column == 0) {
                followupState.resultState.setLeftBlackRookMoved(true);
            }

            // black king rook initial position
            if (row == 7 && column == 7) {
                followupState.resultState.setRightBlackRookMoved(true);
            }
        }
        return followupStates;
    }

    private static List<Followup> handleKnight(int row, int column, Piece piece,BoardState initialState) {
        Location jumpLocation1 = new Location(row + 2, column - 1);
        Location jumpLocation2 =  new Location(row + 2, column + 1);
        Location jumpLocation3 =  new Location(row - 2, column - 1);
        Location jumpLocation4 =  new Location(row - 2, column + 1);
        Location jumpLocation5 =  new Location(row + 1, column - 2);
        Location jumpLocation6 =  new Location(row - 1, column - 2);
        Location jumpLocation7 =  new Location(row - 1, column + 2);
        Location jumpLocation8 =  new Location(row + 1, column + 2);
        List<Location> jumpLocations = List.of(jumpLocation1, jumpLocation2, jumpLocation3,
                jumpLocation4, jumpLocation5, jumpLocation6, jumpLocation7, jumpLocation8);
        jumpLocations = removeErroneousLocations(jumpLocations, piece,initialState);
        return getFollowupStates(row, column, jumpLocations, piece,initialState);
    }

    private static List<Followup> getFollowupStates(int initialRow,
         int initialColumn, List<Location> moveLocations, Piece piece, BoardState initialState) {
        List<Followup> out = new ArrayList<>();
        for (Location moveLocation : moveLocations) {
            BoardState copy = initialState.getCopy();
            copy.setPieceAt(initialRow,initialColumn,null);
            copy.setPieceAt(moveLocation.row,moveLocation.column,piece);

            Followup followup = new Followup(initialRow,initialColumn,moveLocation.row,moveLocation.column,piece,copy);
            out.add(followup);
        }
        return out;

    }


    private static List<Location> removeErroneousLocations(List<Location> locations, Piece piece,BoardState initialState) {
        List<Location> out = removeLocationsOutsideOfBounds(locations);
        out = removeLocationsOwnedBySelf(out, piece,initialState);
        return out;
    }

    private static List<Location> removeLocationsOutsideOfBounds(List<Location> locations) {
        return locations.stream().filter(FollowupBoardStates::isLocationInBounds).collect(Collectors.toList());
    }

    private static boolean isLocationInBounds(Location location) {
        return location.row >= 0 && location.column >= 0 && location.row < 8 && location.column < 8;
    }


    private static List<Location> removeLocationsOwnedBySelf(List<Location> locations, Piece piece,BoardState initialState) {
        return locations.stream().filter(location -> !isLocationOwnedBySelf(location.row, location.column, piece,initialState)).collect(Collectors.toList());
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


    public record Location(int row, int column){ }

    public record Followup(int startRow, int startColumn, int resultRow, int resultColumn, Piece movingPiece, BoardState resultState){ }


}
