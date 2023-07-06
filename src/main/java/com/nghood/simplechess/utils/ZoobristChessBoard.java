package com.nghood.simplechess.utils;

import java.util.Random;

public class ZoobristChessBoard {
    private static final int BOARD_SIZE = 8;
    private static final int PIECE_TYPES = 6;

    // Randomly generated numbers for each combination of piece type and position
    private static long[][][] zoobristTable = new long[BOARD_SIZE][BOARD_SIZE][PIECE_TYPES];

    // Random number for the current hash
    private static long currentHash;

    // Chess board representation
    private static int[][] board = new int[BOARD_SIZE][BOARD_SIZE];

    public static void main(String[] args) {
        // Initialize the random number generator and the Zoobrist table
        Random random = new Random();
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                for (int k = 0; k < PIECE_TYPES; k++) {
                    zoobristTable[i][j][k] = random.nextLong();
                }
            }
        }

        // Set up the chess board
        initializeBoard();

        // Calculate the initial hash
        calculateHash();

        // Print the current board and hash
        printBoard();
        System.out.println("Current Hash: " + currentHash);

        // Make a move and update the board
        makeMove(0, 0, 0, 3); // Move the piece at (0, 0) to (0, 3)

        // Print the updated board and hash
        printBoard();
        System.out.println("Current Hash: " + currentHash);

        // Make a move and update the board
        makeMove(0, 3, 0, 0); // Move the piece at (0, 0) to (0, 3)

        // Print the updated board and hash
        printBoard();
        System.out.println("Current Hash: " + currentHash);
    }

    // Initializes the chess board with pieces
    private static void initializeBoard() {
        // 0 represents an empty square, 1-6 represent different pieces
        board[0][0] = 6; // Example: Piece 1 at (0, 0)
        // Initialize the rest of the board
    }

    // Calculates the hash value for the current board position
    private static void calculateHash() {
        currentHash = 0;
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                int piece = board[i][j];
                if (piece != 0) {
                    currentHash ^= zoobristTable[i][j][piece - 1];
                }
            }
        }
    }

    // Makes a move on the chess board
    private static void makeMove(int fromX, int fromY, int toX, int toY) {
        // Update the board
        int piece = board[fromX][fromY];
        board[fromX][fromY] = 0;
        board[toX][toY] = piece;

        // Update the hash
        currentHash ^= zoobristTable[fromX][fromY][piece - 1];
        currentHash ^= zoobristTable[toX][toY][piece - 1];
    }

    // Prints the current chess board
    private static void printBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
    }
}