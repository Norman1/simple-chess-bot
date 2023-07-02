package com.nghood.simplechess.io;

public class MoveTransformer {

    public static String getMove(int row1,int column1, int row2, int column2){
        //a2a3
        String out = "";
        out += getColumnString(column1);
        out += getRowString(row1);
        out += getColumnString(column2);
        out += getRowString(row2);
        return out;
    }

    private static String getRowString(int index){
        int rowInt = index +1;
        return Integer.toString(rowInt);
    }

    private static String getColumnString(int index){
        String out = "";
        if(index == 0){
            out += "a";
        }
        else if(index == 1){
            out += "b";
        }
        else if(index == 2){
            out += "c";
        }
        else if(index == 3){
            out += "d";
        }
        else if(index == 4){
            out += "e";
        }
        else if(index == 5){
            out += "f";
        }
        else if(index == 6){
            out += "g";
        }
        else if(index == 7){
            out += "h";
        }
        return out;
    }

}
