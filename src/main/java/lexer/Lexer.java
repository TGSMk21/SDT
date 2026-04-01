package lexer;

import java.util.*;
import lexer.util.RegexUtil;

/**
 * Lexical Parser returns a list of tokens given a string input
 * Loops through the string collecting substrings and converting them to their respective tokens
 * prints out an error message in the case a bad token is identified
 */

public class Lexer {
    private SymbolTable symbolTable;

    public Lexer(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }
    public List<Token> tokenise(String input) {
        List<Token> tokens = new ArrayList<>(); //list of tokens

        int i = 0; //counter to iterate through string
        input = input.trim(); //trim out trailing whitespaces

        while (i < input.length()) {
            char c = input.charAt(i);

            //skip whitespaces and move to next character
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            /*
              Once a digit is seen assume token is a number.
              Iterate through string of digits until next token is found.
              Extract the substring and parse it to a Double object, the add the token to the arrayList
             */
            if (Character.isDigit(c)) {
                int start = i;
                while (i < input.length() && Character.isDigit(input.charAt(i))) {
                    i++;
                }

                String numStr = input.substring(start, i);
                Double value = Double.valueOf(numStr);
                tokens.add(new Token(TokenType.NUM, value));
                continue; //move to next iteration for the new token
            }

            if (isValidIdentifierStart(c)) { //assume next token is an identifier if it starts with a letter, underscore or dollar sign
                int start = i;
                //extract substring of identifier
                while (i < input.length() && isValidIdentifierPart(input.charAt(i))) {
                    i++;
                }

                String idStr = input.substring(start, i);

                //Using regex, the identifier is validated to check if it follows naming conventions
                if (RegexUtil.VALID_IDENTIFIER.matcher(idStr).matches()) {
                    if (!symbolTable.contains(idStr)) {
                        symbolTable.insert(idStr);
                    }
                    tokens.add(new Token(TokenType.ID, idStr));
                    continue;
                } else {
                    //print error message and return empty list
                    System.out.println("Invalid identifier : '" + idStr + "'");
                    return new ArrayList<>();
                }
            }

            //assign operator type for operator based on character
            if (TokenType.isOperator(c)) {
                TokenType.OperatorType operatorType = switch (c) {
                    case '+' -> TokenType.OperatorType.PLUS;
                    case '-' -> TokenType.OperatorType.MINUS;
                    case '*' -> TokenType.OperatorType.MUL;
                    case '/' -> TokenType.OperatorType.DIV;
                    case '(' -> TokenType.OperatorType.LPAREN;
                    case ')' -> TokenType.OperatorType.RPAREN;
                    default -> null;
                };
                tokens.add(new Token(TokenType.OPERATOR, operatorType));
                i++;
            } else {
                //encompasses characters or tokens that are not defined or cannot be recognised as valid tokens by the parser
                //an error message is printed and an empty list is returned
                System.out.println("Error: unexpected character: '" + c + "'");
                return new ArrayList<>();
            }
        }

        tokens.add(new Token(TokenType.EOF)); //add eof token to signal end of input
        return tokens;
    }

    private boolean isValidIdentifierStart(char ch) {
        return Character.isLetter(ch) || ch == '_' || ch == '$';
    }

    private boolean isValidIdentifierPart(char ch) {
        return Character.isLetterOrDigit(ch) || ch == '_' || ch == '$';
    }
}
