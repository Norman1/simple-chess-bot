package com.nghood.simplechess.evaluation;

import com.nghood.simplechess.model.BoardState;
import reactor.util.function.Tuple2;

public interface BestMoveCalculation {

    Tuple2<String, BoardState> calculateBestMove(BoardState initialState);


}
