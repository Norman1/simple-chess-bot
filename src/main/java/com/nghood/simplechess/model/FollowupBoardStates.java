package com.nghood.simplechess.model;

import ch.qos.logback.classic.pattern.ClassOfCallerConverter;
import lombok.Data;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuple5;
import reactor.util.function.Tuple6;
import reactor.util.function.Tuples;

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
    private List<Tuple6<Integer,Integer,Integer,Integer,Piece,BoardState>> followupStates = new ArrayList<>();

    public FollowupBoardStates(BoardState initialState){
        this.initialState  = initialState;
        for(int row = 0; row < 8; row ++){
            for(int column = 0; column < 8; column ++){
                Piece piece = initialState.getChessBoard()[row][column];
                if(piece == null){
                    continue;
                }
                switch(piece){
                    case WHITE_KNIGHT:
                       followupStates.addAll(handleKnight(row,column,Piece.WHITE_KNIGHT));
                        break;
                    case BLACK_KNIGHT:
                        followupStates.addAll(handleKnight(row,column,Piece.BLACK_KNIGHT));
                        break;
                    case  WHITE_ROOK:
                        followupStates.addAll(handleRook(row,column,Piece.WHITE_ROOK));
                        break;
                    case BLACK_ROOK:
                        followupStates.addAll(handleRook(row,column,Piece.BLACK_ROOK));
                        break;
                    case WHITE_PAWN:
                        //followupStates.addAll(handleWhitePawn(row,column));
                        break;
                    case BLACK_PAWN:
                        break;
                    default: break;
                }
            }
        }
    }

    private  List<Tuple6<Integer,Integer,Integer,Integer,Piece,BoardState>> handleWhitePawn(int row, int column){
        List<Tuple2<Integer,Integer>> moveLocations = new ArrayList<>();


        // move 1 up
        boolean up1IsTaken = isLocationOwnedBySomeone(row+1,column);
        if(!up1IsTaken){
            moveLocations.add(Tuples.of(row+1,column));
        }

        // move 2 up
        boolean up2IsTaken = isLocationOwnedBySomeone(row+2,column);
        if(row == 1 && !up1IsTaken){
            if(!up2IsTaken){
                moveLocations.add(Tuples.of(row+2,column));
            }
        }

        // attack



        // take en passant

        // promote

        var followupStates = getFollowupStates(row,column,moveLocations,Piece.WHITE_PAWN);
        // moving 2 up is the second entry if up1IsTaken == up2IsTaken == false
        if(!up1IsTaken && !up2IsTaken){
            followupStates.get(1).getT6().setEnPassantVulnerablePawn(Tuples.of(row+2,column));
        }

        return followupStates;
    }

    private  List<Tuple6<Integer,Integer,Integer,Integer,Piece,BoardState>> handleRook(int row, int column, Piece piece){
        List<Tuple2<Integer,Integer>> moveLocations = new ArrayList<>();
        // row up
        for(int i = row+1; i < 8; i++){
            if(isLocationOwnedBySelf(i,column,piece)){
                break;
            }else if(isLocationOwnedByOpponent(i,column,piece)){
                moveLocations.add(Tuples.of(i,column));
                break;
            }
            moveLocations.add(Tuples.of(i,column));
        }
       // row down
        for(int i = row-1; i >=0; i--) {
            if (isLocationOwnedBySelf(i, column, piece)) {
                break;
            }else if(isLocationOwnedByOpponent(i,column,piece)){
                moveLocations.add(Tuples.of(i,column));
                break;
            }
            moveLocations.add(Tuples.of(i, column));
        }

        // column right
        for(int i = column+1; i < 8; i++){
            if(isLocationOwnedBySelf(row,i,piece)){
                break;
            }else if(isLocationOwnedByOpponent(row,i,piece)){
                moveLocations.add(Tuples.of(row,i));
                break;
            }
            moveLocations.add(Tuples.of(row,i));
        }

        // column left
        for(int i = column-1; i >=0; i--){
            if(isLocationOwnedBySelf(row,i,piece)){
                break;
            }else if(isLocationOwnedByOpponent(row,i,piece)){
                moveLocations.add(Tuples.of(row,i));
                break;
            }
            moveLocations.add(Tuples.of(row,i));
        }

        var followupStates = getFollowupStates(row,column,moveLocations,piece);

        for(var followupState : followupStates){
            // white queen rook initial position
            if(row == 0 && column == 0){
                followupState.getT6().setLeftWhiteRookMoved(true);
            }
            // white king rook initial position
            if(row == 0 && column == 7){
                followupState.getT6().setRightWhiteRookMoved(true);
            }

            // black queen rook initial position
            if(row == 7 && column == 0){
                followupState.getT6().setLeftBlackRookMoved(true);
            }

            // black king rook initial position
            if(row == 7 && column == 7){
                followupState.getT6().setRightBlackRookMoved(true);
            }
        }
        return followupStates;
    }

    private  List<Tuple6<Integer,Integer,Integer,Integer,Piece,BoardState>> handleKnight(int row, int column, Piece piece){
        Tuple2<Integer,Integer> jumpLocation1 = Tuples.of(row+2,column-1);
        Tuple2<Integer,Integer> jumpLocation2 = Tuples.of(row+2,column+1);
        Tuple2<Integer,Integer> jumpLocation3 = Tuples.of(row-2,column-1);
        Tuple2<Integer,Integer> jumpLocation4 = Tuples.of(row-2,column+1);
        Tuple2<Integer,Integer> jumpLocation5 = Tuples.of(row+1,column-2);
        Tuple2<Integer,Integer> jumpLocation6 = Tuples.of(row-1,column-2);
        Tuple2<Integer,Integer> jumpLocation7 = Tuples.of(row-1,column+2);
        Tuple2<Integer,Integer> jumpLocation8 = Tuples.of(row+1,column+2);
        List<Tuple2<Integer,Integer>> jumpLocations = List.of(jumpLocation1,jumpLocation2,jumpLocation3,
                jumpLocation4,jumpLocation5,jumpLocation6,jumpLocation7,jumpLocation8);
        jumpLocations = removeErroneousLocations(jumpLocations,piece);
        return getFollowupStates(row,column,jumpLocations,piece);
    }


   private List<Tuple6<Integer,Integer,Integer,Integer,Piece,BoardState>> getFollowupStates(int initialRow,
      int initialColumn, List<Tuple2<Integer,Integer>> moveLocations, Piece piece){
       List<Tuple6<Integer,Integer,Integer,Integer,Piece,BoardState>> out = new ArrayList<>();
        for(Tuple2<Integer,Integer> moveLocation : moveLocations){
            BoardState copy = initialState.getCopy();
            // TODO copy has t move to next move
            copy.getChessBoard()[initialRow][initialColumn] = null;
            copy.getChessBoard()[moveLocation.getT1()][moveLocation.getT2()] = piece;
            Tuple6<Integer,Integer,Integer,Integer,Piece,BoardState> newSituation =Tuples.of(
              initialRow,initialColumn,moveLocation.getT1(),moveLocation.getT2(),piece,copy);
            out.add(newSituation);
        }
       return out;

    }



    private List<Tuple2<Integer,Integer>> removeErroneousLocations(List<Tuple2<Integer,Integer>> locations,Piece piece){
        List<Tuple2<Integer,Integer>> out = removeLocationsOutsideOfBounds(locations);
        out = removeLocationsOwnedBySelf(out,piece);
        return out;
    }

    private List<Tuple2<Integer,Integer>> removeLocationsOutsideOfBounds(List<Tuple2<Integer,Integer>> locations){
        return locations.stream().filter(location -> location.getT1() >= 0 && location.getT2() >= 0 && location.getT1() < 8
        && location.getT2() < 8).collect(Collectors.toList());
    }


    private List<Tuple2<Integer,Integer>> removeLocationsOwnedBySelf(List<Tuple2<Integer,Integer>> locations, Piece piece){
        return locations.stream().filter(location -> !isLocationOwnedBySelf(location.getT1(),location.getT2(),piece)).collect(Collectors.toList());
    }

    private boolean isLocationOwnedBySelf(int row, int column, Piece testPiece){
        boolean pieceIsWhite = testPiece.ordinal() <= 5;
        Piece pieceOnLocation  = initialState.getChessBoard()[row][column];
        if(pieceOnLocation == null){
            return false;
        }
        boolean locationIsWhite = pieceOnLocation.ordinal() <= 5;
        return pieceIsWhite == locationIsWhite;
    }

    private boolean isLocationOwnedByOpponent(int row, int column, Piece testPiece){
        boolean pieceIsWhite = testPiece.ordinal() <= 5;
        Piece pieceOnLocation  = initialState.getChessBoard()[row][column];
        if(pieceOnLocation == null){
            return false;
        }
        boolean locationIsWhite = pieceOnLocation.ordinal() <= 5;
        return pieceIsWhite != locationIsWhite;
    }



    private boolean isLocationOwnedBySomeone(int row, int column){
        return initialState.getChessBoard()[row][column] != null;
    }




}
