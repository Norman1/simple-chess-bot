package com.nghood.simplechess.evaluation;

import com.nghood.simplechess.model.BoardState;
import lombok.Data;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple4;

import java.util.ArrayList;
import java.util.List;

@Data
public class AlphaBetaTree {

    private List<AlphaBetaTree> childTrees = new ArrayList<>();
    private int treeValue;
    private Tuple4<Integer,Integer,Integer,Integer> movesToGetToPosition = null;
    private int currentDepth;
    private BoardState currentState;
    private int alpha;
    private int beta;

    /*
    * Fields for quiescence search. If after depth is 0 and it is white's move, we are only calculating a subset of white's moves.
    * We are hereby not allowing white to decrease it's score. Else white might go for something like taking a pawn with his queen and black takes the queen
    * the turn after and then there are no more non quiet moves. Set the minimum values at depth 0.
     */
    private Integer guaranteedBlackMaxValue;
    private Integer guaranteedWhiteMinValue;



  private  Tuple2<Integer,Integer> lastMovedPiece;

    public AlphaBetaTree(){

    }

}
