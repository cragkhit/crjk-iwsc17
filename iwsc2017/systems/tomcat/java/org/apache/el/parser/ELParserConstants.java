package org.apache.el.parser;
public interface ELParserConstants {
    int EOF = 0;
    int LITERAL_EXPRESSION = 1;
    int START_DYNAMIC_EXPRESSION = 2;
    int START_DEFERRED_EXPRESSION = 3;
    int START_SET_OR_MAP = 8;
    int RBRACE = 9;
    int INTEGER_LITERAL = 10;
    int FLOATING_POINT_LITERAL = 11;
    int EXPONENT = 12;
    int STRING_LITERAL = 13;
    int TRUE = 14;
    int FALSE = 15;
    int NULL = 16;
    int DOT = 17;
    int LPAREN = 18;
    int RPAREN = 19;
    int LBRACK = 20;
    int RBRACK = 21;
    int COLON = 22;
    int SEMICOLON = 23;
    int COMMA = 24;
    int GT0 = 25;
    int GT1 = 26;
    int LT0 = 27;
    int LT1 = 28;
    int GE0 = 29;
    int GE1 = 30;
    int LE0 = 31;
    int LE1 = 32;
    int EQ0 = 33;
    int EQ1 = 34;
    int NE0 = 35;
    int NE1 = 36;
    int NOT0 = 37;
    int NOT1 = 38;
    int AND0 = 39;
    int AND1 = 40;
    int OR0 = 41;
    int OR1 = 42;
    int EMPTY = 43;
    int INSTANCEOF = 44;
    int MULT = 45;
    int PLUS = 46;
    int MINUS = 47;
    int QUESTIONMARK = 48;
    int DIV0 = 49;
    int DIV1 = 50;
    int MOD0 = 51;
    int MOD1 = 52;
    int CONCAT = 53;
    int ASSIGN = 54;
    int ARROW = 55;
    int IDENTIFIER = 56;
    int FUNCTIONSUFFIX = 57;
    int IMPL_OBJ_START = 58;
    int LETTER = 59;
    int DIGIT = 60;
    int ILLEGAL_CHARACTER = 61;
    int DEFAULT = 0;
    int IN_EXPRESSION = 1;
    int IN_SET_OR_MAP = 2;
    String[] tokenImage = {
        "<EOF>",
        "<LITERAL_EXPRESSION>",
        "\"${\"",
        "\"#{\"",
        "\" \"",
        "\"\\t\"",
        "\"\\n\"",
        "\"\\r\"",
        "\"{\"",
        "\"}\"",
        "<INTEGER_LITERAL>",
        "<FLOATING_POINT_LITERAL>",
        "<EXPONENT>",
        "<STRING_LITERAL>",
        "\"true\"",
        "\"false\"",
        "\"null\"",
        "\".\"",
        "\"(\"",
        "\")\"",
        "\"[\"",
        "\"]\"",
        "\":\"",
        "\";\"",
        "\",\"",
        "\">\"",
        "\"gt\"",
        "\"<\"",
        "\"lt\"",
        "\">=\"",
        "\"ge\"",
        "\"<=\"",
        "\"le\"",
        "\"==\"",
        "\"eq\"",
        "\"!=\"",
        "\"ne\"",
        "\"!\"",
        "\"not\"",
        "\"&&\"",
        "\"and\"",
        "\"||\"",
        "\"or\"",
        "\"empty\"",
        "\"instanceof\"",
        "\"*\"",
        "\"+\"",
        "\"-\"",
        "\"?\"",
        "\"/\"",
        "\"div\"",
        "\"%\"",
        "\"mod\"",
        "\"+=\"",
        "\"=\"",
        "\"->\"",
        "<IDENTIFIER>",
        "<FUNCTIONSUFFIX>",
        "\"#\"",
        "<LETTER>",
        "<DIGIT>",
        "<ILLEGAL_CHARACTER>",
    };
}
