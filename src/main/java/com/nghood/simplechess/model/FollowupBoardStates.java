package com.nghood.simplechess.model;

import ch.qos.logback.classic.pattern.ClassOfCallerConverter;
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

    private BoardState initialState;
    // first row, first column, second row, second column
    private List<Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState>> followupStates = new ArrayList<>();

    public FollowupBoardStates(BoardState initialState, AttackBoardState attackBoardState, boolean invertCurrentPlayer) {
        this.initialState = initialState;
        boolean isWhitePlayerMove = initialState.isWhitePlayerMove();
        if(invertCurrentPlayer){
            isWhitePlayerMove = !isWhitePlayerMove;
        }
        if(isKingLost(isWhitePlayerMove)){
            return;
        }


        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                Piece piece = initialState.getChessBoard()[row][column];
                if (piece == null) {
                    continue;
                }
                switch (piece) {
                    case WHITE_KNIGHT:
                        if (isWhitePlayerMove) {
                            followupStates.addAll(handleKnight(row, column, Piece.WHITE_KNIGHT));
                        }
                        break;
                    case BLACK_KNIGHT:
                        if (!isWhitePlayerMove) {
                            followupStates.addAll(handleKnight(row, column, Piece.BLACK_KNIGHT));
                        }
                        break;
                    case WHITE_ROOK:
                        if (isWhitePlayerMove) {
                            followupStates.addAll(handleRook(row, column, Piece.WHITE_ROOK));
                        }
                        break;
                    case BLACK_ROOK:
                        if (!isWhitePlayerMove) {
                            followupStates.addAll(handleRook(row, column, Piece.BLACK_ROOK));
                        }
                        break;
                    case WHITE_PAWN:
                        if (isWhitePlayerMove) {
                            followupStates.addAll(handleWhitePawn(row, column));
                        }
                        break;
                    case BLACK_PAWN:
                        if (!isWhitePlayerMove) {
                            followupStates.addAll(handleBlackPawn(row, column));
                        }
                        break;
                    case WHITE_BISHOP:
                        if (isWhitePlayerMove) {
                            followupStates.addAll(handleBishop(row, column, piece));
                        }
                        break;
                    case BLACK_BISHOP:
                        if (!isWhitePlayerMove) {
                            followupStates.addAll(handleBishop(row, column, piece));
                        }
                        break;
                    case WHITE_QUEEN:
                        if (isWhitePlayerMove) {
                            followupStates.addAll(handleQueen(row, column, piece));
                        }
                        break;
                    case BLACK_QUEEN:
                        if (!isWhitePlayerMove) {
                            followupStates.addAll(handleQueen(row, column, piece));
                        }
                        break;
                    case WHITE_KING:
                        if (isWhitePlayerMove) {
                            followupStates.addAll(handleKing(row, column, piece,attackBoardState));
                        }
                        break;
                    case BLACK_KING:
                        if (!isWhitePlayerMove) {
                            followupStates.addAll(handleKing(row, column, piece,attackBoardState));
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        setKingAndRookMovements(followupStates);
        followupStates.forEach(followup -> followup.getT6().nextMove());
    }

    // we allow king captures but then there are no followup states for the player having lost his king.
    private boolean isKingLost(boolean checkWhiteKing){
        boolean kingPresent = false;
        Piece[][] board = initialState.getChessBoard();
        for(int row = 0; row < 8; row++){
            for(int column = 0; column < 8; column++){
                if(board[row][column] != null){
                    if(checkWhiteKing &&board[row][column] == Piece.WHITE_KING){
                        kingPresent = true;
                    }
                    else if(!checkWhiteKing &&board[row][column] == Piece.BLACK_KING){
                        kingPresent = true;
                    }
                }
            }
        }

        return !kingPresent;
    }

    public FollowupBoardStates(BoardState initialState) {
        this(initialState,null,false);
    }



    public void setKingAndRookMovements(List<Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState>> followupStates) {
        //0,0; 0,4; 0,7; 7,0; 7,4; 7,7
        followupStates.forEach(followupState -> {
            BoardState boardState = followupState.getT6();
            if((followupState.getT1() == 0 && followupState.getT2() == 0) || (followupState.getT3() == 0 && followupState.getT4() == 0)){
                boardState.setLeftWhiteRookMoved(true);
            }
            else if(followupState.getT1() == 0 && followupState.getT2() == 4){
                boardState.setWhiteKingMoved(true);
            }else if((followupState.getT1() == 0 && followupState.getT2() == 7) || (followupState.getT3() == 0 && followupState.getT4() == 7)){
                boardState.setRightWhiteRookMoved(true);
            }else if((followupState.getT1() == 7 && followupState.getT2() == 0) || (followupState.getT3() == 7 && followupState.getT4() == 0)){
                boardState.setLeftBlackRookMoved(true);
            }else if(followupState.getT1() == 7 && followupState.getT2() == 4){
                boardState.setBlackKingMoved(true);
            }else if((followupState.getT1() == 7 && followupState.getT2() == 7) || (followupState.getT3() == 7 && followupState.getT4() == 7)){
                boardState.setRightBlackRookMoved(true);
            }
        });
    }


    private List<Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState>> handleKing(int row, int column, Piece piece,AttackBoardState attackBoardState) {
        Tuple2<Integer, Integer> upLeft = Tuples.of(row + 1, column - 1);
        Tuple2<Integer, Integer> up = Tuples.of(row + 1, column);
        Tuple2<Integer, Integer> upRight = Tuples.of(row + 1, column + 1);
        Tuple2<Integer, Integer> left = Tuples.of(row, column - 1);
        Tuple2<Integer, Integer> right = Tuples.of(row, column + 1);
        Tuple2<Integer, Integer> downLeft = Tuples.of(row - 1, column - 1);
        Tuple2<Integer, Integer> down = Tuples.of(row - 1, column);
        Tuple2<Integer, Integer> downRight = Tuples.of(row - 1, column + 1);
        List<Tuple2<Integer, Integer>> moveLocations = List.of(upLeft, up, upRight, left, right, downLeft, down, downRight);
        moveLocations = removeErroneousLocations(moveLocations, piece);

        var followupStates = getFollowupStates(row, column, moveLocations, piece);

        boolean kingIsWhite = piece.ordinal() <= 5;
        // Do not accept castling when the king or the fields 1 left and 1 right are under attack

        // white left castling
        Piece[][] board = initialState.getChessBoard();

        boolean leftWhiteCastleAttackBlocked = false;
        boolean rightWhiteCastleAttackBlocked = false;
        boolean leftBlackCastleAttackBlocked = false;
        boolean rightBlackCastleAttackBlocked = false;
        if(attackBoardState != null){
            leftWhiteCastleAttackBlocked = attackBoardState.isFieldUnderAttack(0,3) ||
                    attackBoardState.isFieldUnderAttack(0,4);
            rightWhiteCastleAttackBlocked = attackBoardState.isFieldUnderAttack(0,4)||
                    attackBoardState.isFieldUnderAttack(0,5);

            leftBlackCastleAttackBlocked = attackBoardState.isFieldUnderAttack(7,3) ||
                    attackBoardState.isFieldUnderAttack(7,4);
            rightBlackCastleAttackBlocked = attackBoardState.isFieldUnderAttack(7,4) ||
                    attackBoardState.isFieldUnderAttack(7,5);
        }


        List<Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState>> castlingFollowupStates = new ArrayList<>();
        if (kingIsWhite && !initialState.isWhiteKingMoved() && !initialState.isLeftWhiteRookMoved() && board[0][1] == null && board[0][2] == null && board[0][3] == null && !leftWhiteCastleAttackBlocked) {
            Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState> whiteLeftFollowup = Tuples.of(row, column, row, column - 2, Piece.WHITE_KING, initialState.getCopy());
            whiteLeftFollowup.getT6().getChessBoard()[0][0] = null;
            whiteLeftFollowup.getT6().getChessBoard()[0][4] = null;
            whiteLeftFollowup.getT6().getChessBoard()[0][2] = Piece.WHITE_KING;
            whiteLeftFollowup.getT6().getChessBoard()[0][3] = Piece.WHITE_ROOK;
            whiteLeftFollowup.getT6().setLeftWhiteRookMoved(true);
            castlingFollowupStates.add(whiteLeftFollowup);
        }
        // white right castling
        if (kingIsWhite && !initialState.isWhiteKingMoved() && !initialState.isRightWhiteRookMoved() && board[0][5] == null && board[0][6] == null &&!rightWhiteCastleAttackBlocked) {
            Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState> whiteRightFollowup = Tuples.of(row, column, row, column + 2, Piece.WHITE_KING, initialState.getCopy());
            whiteRightFollowup.getT6().getChessBoard()[0][7] = null;
            whiteRightFollowup.getT6().getChessBoard()[0][4] = null;
            whiteRightFollowup.getT6().getChessBoard()[0][6] = Piece.WHITE_KING;
            whiteRightFollowup.getT6().getChessBoard()[0][5] = Piece.WHITE_ROOK;
            whiteRightFollowup.getT6().setRightWhiteRookMoved(true);
            castlingFollowupStates.add(whiteRightFollowup);
        }

        // black left castling
        if (!kingIsWhite && !initialState.isBlackKingMoved() && !initialState.isLeftBlackRookMoved() && board[7][1] == null && board[7][2] == null && board[7][3] == null &&!leftBlackCastleAttackBlocked) {
            Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState> blackLeftFollowup = Tuples.of(row, column, row, column - 2, Piece.BLACK_KING, initialState.getCopy());
            blackLeftFollowup.getT6().getChessBoard()[7][0] = null;
            blackLeftFollowup.getT6().getChessBoard()[7][4] = null;
            blackLeftFollowup.getT6().getChessBoard()[7][2] = Piece.BLACK_KING;
            blackLeftFollowup.getT6().getChessBoard()[7][3] = Piece.BLACK_ROOK;
            blackLeftFollowup.getT6().setLeftBlackRookMoved(true);
            castlingFollowupStates.add(blackLeftFollowup);
        }

        // black right castling
        if (!kingIsWhite && !initialState.isBlackKingMoved() && !initialState.isRightBlackRookMoved() && board[7][5] == null && board[7][6] == null && !rightBlackCastleAttackBlocked) {
            Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState> blackRightFollowup = Tuples.of(row, column, row, column + 2, Piece.BLACK_KING, initialState.getCopy());
            blackRightFollowup.getT6().getChessBoard()[7][7] = null;
            blackRightFollowup.getT6().getChessBoard()[7][4] = null;
            blackRightFollowup.getT6().getChessBoard()[7][6] = Piece.BLACK_KING;
            blackRightFollowup.getT6().getChessBoard()[7][5] = Piece.BLACK_ROOK;
            blackRightFollowup.getT6().setRightBlackRookMoved(true);
            castlingFollowupStates.add(blackRightFollowup);
        }

        followupStates.addAll(castlingFollowupStates);
        return followupStates;
    }


    private List<Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState>> handleBlackPawn(int row, int column) {
        List<Tuple2<Integer, Integer>> moveLocations = new ArrayList<>();

        // move 1 down
        boolean down1IsTaken = isLocationOwnedBySomeone(row - 1, column);
        if (!down1IsTaken) {
            moveLocations.add(Tuples.of(row - 1, column));
        }

        // move 2 down
        boolean down2IsTaken = !isLocationInBounds(Tuples.of(row - 2, column)) || isLocationOwnedBySomeone(row - 2, column);
        if (row == 6 && !down1IsTaken) {
            if (!down2IsTaken) {
                moveLocations.add(Tuples.of(row - 2, column));
            }
        }

        // attack
        Tuple2<Integer, Integer> attackLeft = Tuples.of(row - 1, column - 1);
        Tuple2<Integer, Integer> attackRight = Tuples.of(row - 1, column + 1);
        if (isLocationInBounds(attackLeft) && isLocationOwnedByOpponent(row - 1, column - 1, Piece.BLACK_PAWN)) {
            moveLocations.add(attackLeft);
        }
        if (isLocationInBounds(attackRight) && isLocationOwnedByOpponent(row - 1, column + 1, Piece.BLACK_PAWN)) {
            moveLocations.add(attackRight);
        }

        var followupStates = getFollowupStates(row, column, moveLocations, Piece.BLACK_PAWN);

        // take en passant
        Tuple2<Integer, Integer> whiteEnPassantVulnerablePawn = initialState.getEnPassantVulnerablePawn();
        if (whiteEnPassantVulnerablePawn != null) {
            // have to be on 4th row and 1 column to the left or right
            if (row == 3 && (whiteEnPassantVulnerablePawn.getT2() == column - 1 || whiteEnPassantVulnerablePawn.getT2() == column + 1)) {
                Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState> takeEnPassantFollowupState = Tuples.of(row, column, row - 1, whiteEnPassantVulnerablePawn.getT2(), Piece.BLACK_PAWN, initialState.getCopy());
                takeEnPassantFollowupState.getT6().getChessBoard()[whiteEnPassantVulnerablePawn.getT1()][whiteEnPassantVulnerablePawn.getT2()] = null;
                takeEnPassantFollowupState.getT6().getChessBoard()[row][column] = null;
                takeEnPassantFollowupState.getT6().getChessBoard()[whiteEnPassantVulnerablePawn.getT1() - 1][whiteEnPassantVulnerablePawn.getT2()] = Piece.BLACK_PAWN;
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
            blackBishopState.getT6().getChessBoard()[blackBishopState.getT3()][blackBishopState.getT4()] = Piece.BLACK_BISHOP;
            followupStates.add(blackBishopState);

            // black knight
            Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState> blackKnightState =
                    Tuples.of(promotionState.getT1(), promotionState.getT2(), promotionState.getT3(), promotionState.getT4(),
                            promotionState.getT5(), promotionState.getT6().getCopy());
            blackKnightState.getT6().getChessBoard()[blackKnightState.getT3()][blackKnightState.getT4()] = Piece.BLACK_KNIGHT;
            followupStates.add(blackKnightState);

            // black rook
            Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState> blackRookState =
                    Tuples.of(promotionState.getT1(), promotionState.getT2(), promotionState.getT3(), promotionState.getT4(),
                            promotionState.getT5(), promotionState.getT6().getCopy());
            blackRookState.getT6().getChessBoard()[blackRookState.getT3()][blackRookState.getT4()] = Piece.BLACK_ROOK;
            followupStates.add(blackRookState);

            // white queen
            Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState> blackQueenState =
                    Tuples.of(promotionState.getT1(), promotionState.getT2(), promotionState.getT3(), promotionState.getT4(),
                            promotionState.getT5(), promotionState.getT6().getCopy());
            blackQueenState.getT6().getChessBoard()[blackQueenState.getT3()][blackQueenState.getT4()] = Piece.BLACK_QUEEN;
            followupStates.add(blackQueenState);
        }

        // moving 2 up is the second entry if up1IsTaken == up2IsTaken == false
        if (!down1IsTaken && !down2IsTaken && row == 1) {
            followupStates.get(1).getT6().setEnPassantVulnerablePawn(Tuples.of(row - 2, column));
        }

        return followupStates;
    }

    private List<Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState>> handleWhitePawn(int row, int column) {
        List<Tuple2<Integer, Integer>> moveLocations = new ArrayList<>();


        // move 1 up
        boolean up1IsTaken = isLocationOwnedBySomeone(row + 1, column);
        if (!up1IsTaken) {
            moveLocations.add(Tuples.of(row + 1, column));
        }

        // move 2 up
        boolean up2IsTaken = !isLocationInBounds(Tuples.of(row + 2, column)) || isLocationOwnedBySomeone(row + 2, column);
        if (row == 1 && !up1IsTaken) {
            if (!up2IsTaken) {
                moveLocations.add(Tuples.of(row + 2, column));
            }
        }

        // attack
        Tuple2<Integer, Integer> attackLeft = Tuples.of(row + 1, column - 1);
        Tuple2<Integer, Integer> attackRight = Tuples.of(row + 1, column + 1);
        if (isLocationInBounds(attackLeft) && isLocationOwnedByOpponent(row + 1, column - 1, Piece.WHITE_PAWN)) {
            moveLocations.add(attackLeft);
        }
        if (isLocationInBounds(attackRight) && isLocationOwnedByOpponent(row + 1, column + 1, Piece.WHITE_PAWN)) {
            moveLocations.add(attackRight);
        }

        var followupStates = getFollowupStates(row, column, moveLocations, Piece.WHITE_PAWN);

        // take en passant
        Tuple2<Integer, Integer> blackEnPassantVulnerablePawn = initialState.getEnPassantVulnerablePawn();
        if (blackEnPassantVulnerablePawn != null) {
            // have to be on 4th row and 1 column to the left or right
            if (row == 4 && (blackEnPassantVulnerablePawn.getT2() == column - 1 || blackEnPassantVulnerablePawn.getT2() == column + 1)) {
                Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState> takeEnPassantFollowupState = Tuples.of(row, column, row + 1, blackEnPassantVulnerablePawn.getT2(), Piece.WHITE_PAWN, initialState.getCopy());
                takeEnPassantFollowupState.getT6().getChessBoard()[blackEnPassantVulnerablePawn.getT1()][blackEnPassantVulnerablePawn.getT2()] = null;
                takeEnPassantFollowupState.getT6().getChessBoard()[row][column] = null;
                takeEnPassantFollowupState.getT6().getChessBoard()[blackEnPassantVulnerablePawn.getT1() + 1][blackEnPassantVulnerablePawn.getT2()] = Piece.WHITE_PAWN;
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
            whiteBishopState.getT6().getChessBoard()[whiteBishopState.getT3()][whiteBishopState.getT4()] = Piece.WHITE_BISHOP;
            followupStates.add(whiteBishopState);

            // white knight
            Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState> whiteKnightState =
                    Tuples.of(promotionState.getT1(), promotionState.getT2(), promotionState.getT3(), promotionState.getT4(),
                            promotionState.getT5(), promotionState.getT6().getCopy());
            whiteKnightState.getT6().getChessBoard()[whiteKnightState.getT3()][whiteKnightState.getT4()] = Piece.WHITE_KNIGHT;
            followupStates.add(whiteKnightState);

            // white rook
            Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState> whiteRookState =
                    Tuples.of(promotionState.getT1(), promotionState.getT2(), promotionState.getT3(), promotionState.getT4(),
                            promotionState.getT5(), promotionState.getT6().getCopy());
            whiteRookState.getT6().getChessBoard()[whiteRookState.getT3()][whiteRookState.getT4()] = Piece.WHITE_ROOK;
            followupStates.add(whiteRookState);

            // white queen
            Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState> whiteQueenState =
                    Tuples.of(promotionState.getT1(), promotionState.getT2(), promotionState.getT3(), promotionState.getT4(),
                            promotionState.getT5(), promotionState.getT6().getCopy());
            whiteQueenState.getT6().getChessBoard()[whiteQueenState.getT3()][whiteQueenState.getT4()] = Piece.WHITE_QUEEN;
            followupStates.add(whiteQueenState);
        }

        // moving 2 up is the second entry if up1IsTaken == up2IsTaken == false
        if (!up1IsTaken && !up2IsTaken && row == 1) {
            followupStates.get(1).getT6().setEnPassantVulnerablePawn(Tuples.of(row + 2, column));
        }

        return followupStates;
    }

    private List<Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState>> handleQueen(int row, int column, Piece piece) {
        var result = handleBishop(row, column, piece);
        // the isRookMoved stuff is no problem since if the queen is there the rook is gone from there
        result.addAll(handleRook(row, column, piece));
        return result;
    }

    private List<Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState>> handleBishop(int row, int column, Piece piece) {
        List<Tuple2<Integer, Integer>> moveLocations = new ArrayList<>();
        // left up
        for (int i = 1; i < 8; i++) {
            Tuple2<Integer, Integer> location = Tuples.of(row + i, column - i);
            if (!isLocationInBounds(location)) {
                break;
            }
            if (isLocationOwnedBySelf(location.getT1(), location.getT2(), piece)) {
                break;
            } else if (isLocationOwnedByOpponent(location.getT1(), location.getT2(), piece)) {
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
            if (isLocationOwnedBySelf(location.getT1(), location.getT2(), piece)) {
                break;
            } else if (isLocationOwnedByOpponent(location.getT1(), location.getT2(), piece)) {
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
            if (isLocationOwnedBySelf(location.getT1(), location.getT2(), piece)) {
                break;
            } else if (isLocationOwnedByOpponent(location.getT1(), location.getT2(), piece)) {
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
            if (isLocationOwnedBySelf(location.getT1(), location.getT2(), piece)) {
                break;
            } else if (isLocationOwnedByOpponent(location.getT1(), location.getT2(), piece)) {
                moveLocations.add(Tuples.of(row - i, column + i));
                break;
            }
            moveLocations.add(Tuples.of(row - i, column + i));
        }

        return getFollowupStates(row, column, moveLocations, piece);
    }

    private List<Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState>> handleRook(int row, int column, Piece piece) {
        List<Tuple2<Integer, Integer>> moveLocations = new ArrayList<>();
        // row up
        for (int i = row + 1; i < 8; i++) {
            if (isLocationOwnedBySelf(i, column, piece)) {
                break;
            } else if (isLocationOwnedByOpponent(i, column, piece)) {
                moveLocations.add(Tuples.of(i, column));
                break;
            }
            moveLocations.add(Tuples.of(i, column));
        }
        // row down
        for (int i = row - 1; i >= 0; i--) {
            if (isLocationOwnedBySelf(i, column, piece)) {
                break;
            } else if (isLocationOwnedByOpponent(i, column, piece)) {
                moveLocations.add(Tuples.of(i, column));
                break;
            }
            moveLocations.add(Tuples.of(i, column));
        }

        // column right
        for (int i = column + 1; i < 8; i++) {
            if (isLocationOwnedBySelf(row, i, piece)) {
                break;
            } else if (isLocationOwnedByOpponent(row, i, piece)) {
                moveLocations.add(Tuples.of(row, i));
                break;
            }
            moveLocations.add(Tuples.of(row, i));
        }

        // column left
        for (int i = column - 1; i >= 0; i--) {
            if (isLocationOwnedBySelf(row, i, piece)) {
                break;
            } else if (isLocationOwnedByOpponent(row, i, piece)) {
                moveLocations.add(Tuples.of(row, i));
                break;
            }
            moveLocations.add(Tuples.of(row, i));
        }

        var followupStates = getFollowupStates(row, column, moveLocations, piece);

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

    private List<Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState>> handleKnight(int row, int column, Piece piece) {
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
        jumpLocations = removeErroneousLocations(jumpLocations, piece);
        return getFollowupStates(row, column, jumpLocations, piece);
    }


    private List<Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState>> getFollowupStates(int initialRow,
                                                                                                  int initialColumn, List<Tuple2<Integer, Integer>> moveLocations, Piece piece) {
        List<Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState>> out = new ArrayList<>();
        for (Tuple2<Integer, Integer> moveLocation : moveLocations) {
            BoardState copy = initialState.getCopy();
            // TODO copy has t move to next move
            copy.getChessBoard()[initialRow][initialColumn] = null;
            copy.getChessBoard()[moveLocation.getT1()][moveLocation.getT2()] = piece;
            Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState> newSituation = Tuples.of(
                    initialRow, initialColumn, moveLocation.getT1(), moveLocation.getT2(), piece, copy);
            out.add(newSituation);
        }
        return out;

    }


    private List<Tuple2<Integer, Integer>> removeErroneousLocations(List<Tuple2<Integer, Integer>> locations, Piece piece) {
        List<Tuple2<Integer, Integer>> out = removeLocationsOutsideOfBounds(locations);
        out = removeLocationsOwnedBySelf(out, piece);
        return out;
    }

    private List<Tuple2<Integer, Integer>> removeLocationsOutsideOfBounds(List<Tuple2<Integer, Integer>> locations) {
        return locations.stream().filter(location -> isLocationInBounds(location)).collect(Collectors.toList());
    }

    private boolean isLocationInBounds(Tuple2<Integer, Integer> location) {
        return location.getT1() >= 0 && location.getT2() >= 0 && location.getT1() < 8 && location.getT2() < 8;
    }


    private List<Tuple2<Integer, Integer>> removeLocationsOwnedBySelf(List<Tuple2<Integer, Integer>> locations, Piece piece) {
        return locations.stream().filter(location -> !isLocationOwnedBySelf(location.getT1(), location.getT2(), piece)).collect(Collectors.toList());
    }

    private boolean isLocationOwnedBySelf(int row, int column, Piece testPiece) {
        boolean pieceIsWhite = testPiece.ordinal() <= 5;
        Piece pieceOnLocation = initialState.getChessBoard()[row][column];
        if (pieceOnLocation == null) {
            return false;
        }
        boolean locationIsWhite = pieceOnLocation.ordinal() <= 5;
        return pieceIsWhite == locationIsWhite;
    }

    private boolean isLocationOwnedByOpponent(int row, int column, Piece testPiece) {
        boolean pieceIsWhite = testPiece.ordinal() <= 5;
        Piece pieceOnLocation = initialState.getChessBoard()[row][column];
        if (pieceOnLocation == null) {
            return false;
        }
        boolean locationIsWhite = pieceOnLocation.ordinal() <= 5;
        return pieceIsWhite != locationIsWhite;
    }


    private boolean isLocationOwnedBySomeone(int row, int column) {
        return initialState.getChessBoard()[row][column] != null;
    }


}
