package com.nghood.simplechess.evaluation;

import com.nghood.simplechess.model.BoardState;
import lombok.Data;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple4;

import java.sql.Array;
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

  private  Tuple2<Integer,Integer> lastMovedPiece;

    public AlphaBetaTree(){

    }

}
