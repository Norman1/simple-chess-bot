package com.nghood.simplechess.play;

import com.nghood.simplechess.evaluation.*;
import com.nghood.simplechess.io.BoardPrinter;
import com.nghood.simplechess.io.MoveTransformer;
import com.nghood.simplechess.model.AttackBoardState;
import com.nghood.simplechess.model.AttackBoardStateCalculator2;
import com.nghood.simplechess.model.BoardState;
import com.nghood.simplechess.model.FollowupBoardStates;
import com.nghood.simplechess.utils.TimeMeasurement;
import lombok.SneakyThrows;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple4;
import reactor.util.function.Tuples;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Scanner;

/**
 * Makes the bot competes against itself and prints the board each turn.
 */
public class CompeteAgainstPlayer {

    public static boolean IS_PLAYER_WHITE = false;

    private static void compete() {
        BoardState boardState = new BoardState();
        boardState.setupInitialBoard();
        BoardPrinter boardPrinter = new BoardPrinter();
        boardPrinter.printBoard(boardState);
        while (true) {
            System.out.println();
            System.out.println("=================");
            System.out.println("Turn: " + boardState.getTurn());
            String movingPlayer = boardState.isWhitePlayerMove() ? "White" : "Black";
            System.out.println("Moving player: " + movingPlayer);
            Tuple2<String, BoardState> move = null;
            if (IS_PLAYER_WHITE == boardState.isWhitePlayerMove()) {
                while (move == null){
                    move = performHumanMove(boardState);
                }
            } else {
                move = performBotMove(boardState);
            }

            System.out.println("Performing move " + move.getT1());
            boardPrinter.printBoard(move.getT2());
            boardState = move.getT2();
        }
    }

    private static Tuple2<String, BoardState> performHumanMove(BoardState boardState) {
        AttackBoardState opponentAttack = AttackBoardStateCalculator2.getAttackBoardState(boardState, !IS_PLAYER_WHITE);
        List<FollowupBoardStates.Followup> followupStates = FollowupBoardStates.getFollowups(boardState, opponentAttack);
        String moveString = readUserInput();
        for (FollowupBoardStates.Followup followup : followupStates) {
            int startRow = followup.startRow();
            int startColumn = followup.startColumn();
            int resultRow = followup.resultRow();
            int resultColumn = followup.resultColumn();

            Tuple4<Integer, Integer, Integer, Integer> moveTuple = null;

            moveTuple = getMoveTuple(moveString);
            if(moveTuple == null){
                return performHumanMove(boardState);
            }

            if (startRow == moveTuple.getT1() && startColumn == moveTuple.getT2() && resultRow == moveTuple.getT3()
                    && resultColumn == moveTuple.getT4()) {
                return Tuples.of(moveString, followup.resultState());
            }

        }


        return null;
    }

    private static Tuple4<Integer, Integer, Integer, Integer> getMoveTuple(String inputString) {
        try {
            String startColumnString = inputString.substring(0, 1);
            String startRowString = inputString.substring(1, 2);
            String resultColumnString = inputString.substring(2, 3);
            String resultRowString = inputString.substring(3, 4);

            int startColumn = getRow(startColumnString);
            int startRow = Integer.parseInt(startRowString) - 1;
            int resultColumn = getRow(resultColumnString);
            int resultRow = Integer.parseInt(resultRowString) - 1;
            return Tuples.of(startRow, startColumn, resultRow, resultColumn);
        } catch (Exception e) {
            return null;
        }

    }

    private static int getRow(String rowString) {
        switch (rowString) {
            case "a":
                return 0;
            case "b":
                return 1;
            case "c":
                return 2;
            case "d":
                return 3;
            case "e":
                return 4;
            case "f":
                return 5;
            case "g":
                return 6;
            case "h":
                return 7;
        }
        return -1;
    }

    private static Tuple2<String, BoardState> performBotMove(BoardState boardState) {
        BestMoveCalculation bestMoveCalculator = new BestMoveCalculatorAlphaBeta();
        Tuple2<String, BoardState> bestMove = bestMoveCalculator.calculateBestMove(boardState);

        return bestMove;
    }

    @SneakyThrows
    private static String readUserInput() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Enter next move: ");
        String move = br.readLine();
        return move;
    }


    public static void main(String[] args) {
        compete();
    }


}
