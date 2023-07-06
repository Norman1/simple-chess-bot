//package com.nghood.simplechess.model;
//
//import com.nghood.simplechess.utils.TimeMeasurement;
//import reactor.util.function.Tuple6;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class AttackBoardStateCalculator {
//
//
//    // first row, first column, second row, second column
//   // private List<Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState>> followupStates = new ArrayList<>();
//
//    public AttackBoardState calculateAttackBoardState(BoardState initialState, List<Tuple6<Integer, Integer, Integer, Integer, Piece, BoardState>> followupStates) {
//        /*
//         * Special cases:
//         * - Moving the pawn 1 or 2 forward is no attack.
//         * - Capturing a pawn with en passant does not set any field under attack
//         */
//        TimeMeasurement.start();
//        AttackBoardState attackBoardState = new AttackBoardState();
//        for(var followupState : followupStates){
//            //Special case: Moving a pawn 1 or 2 forward is no attack.
//            boolean isPawn = followupState.getT5() == Piece.WHITE_PAWN || followupState.getT5() == Piece.BLACK_PAWN;
//            if(isPawn && followupState.getT2() == followupState.getT4()){
//                continue;
//            }
//
//            // Special case: Capturing a pawn with en passant does not set any field under attack
//            if(isPawn && followupState.getT2() != followupState.getT4() && initialState.getPieceAt(followupState.getT3(),followupState.getT4())== null){
//                continue;
//            }
//            attackBoardState.setFieldUnderAttack(followupState.getT3(),followupState.getT4(),followupState.getT5());
//
//            // TODO castling king does not attack
//        }
//
//        TimeMeasurement.stop(TimeMeasurement.Category.ATTACK_BOARD_STATE);
//        return attackBoardState;
//    }
//}
