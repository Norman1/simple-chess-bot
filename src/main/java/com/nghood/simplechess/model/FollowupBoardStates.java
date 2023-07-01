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
                    default: break;
                }
            }
        }
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

        return locations.stream().filter(location -> {
            boolean pieceIsWhite = piece.ordinal() <= 5;
            Piece jumpPiece  = initialState.getChessBoard()[location.getT1()][location.getT2()];
            if(jumpPiece == null){
                return true;
            }
            boolean locationIsWhite = jumpPiece.ordinal() <= 5;
            if(pieceIsWhite == locationIsWhite){
                return false;
            }
            return true;
        }).collect(Collectors.toList());
    }


}
