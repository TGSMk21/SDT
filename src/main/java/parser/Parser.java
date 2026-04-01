package parser;

import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;
import java.util.List;
import java.util.Scanner;

public class Parser {

    private List<Token> tokens;
    private int pos;
    private Token current;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.pos = 0;
        this.current = tokens.get(0);
    }

    // advance to next token
    private void advance() {
        pos++;
        if (pos < tokens.size()) {
            current = tokens.get(pos);
        }
    }

    // check if current token is a specific operator
    private boolean isOperator(TokenType.OperatorType op) {
        return current.tokenType == TokenType.OPERATOR
                && current.value == op;
    }

    // match an expected operator and advance
    private void matchOperator(TokenType.OperatorType expected) {
        if (isOperator(expected)) {
            advance();
        } else {
            throw new RuntimeException(
                    "Syntax error at token position " + pos
                            + ": expected '" + expected.getSymbol()
                            + "' but got '" + current + "'"
            );
        }
    }

    // expr → term rest
    public void expr() {
        term();
        rest();
    }

    // rest → + term { print('+') } rest
    //      | - term { print('-') } rest
    //      | ε
    private void rest() {
        if (isOperator(TokenType.OperatorType.PLUS)) {
            advance();
            term();
            System.out.print("+ ");
            rest();
        } else if (isOperator(TokenType.OperatorType.MINUS)) {
            advance();
            term();
            System.out.print("- ");
            rest();
        }
        // ε — do nothing
    }

    // term → factor term_tail
    private void term() {
        factor();
        termTail();
    }

    // term_tail → * factor { print('*') } term_tail
    //           | / factor { print('/') } term_tail
    //           | ε
    private void termTail() {
        if (isOperator(TokenType.OperatorType.MUL)) {
            advance();
            factor();
            System.out.print("* ");
            termTail();
        } else if (isOperator(TokenType.OperatorType.DIV)) {
            advance();
            factor();
            System.out.print("/ ");
            termTail();
        }
        // ε — do nothing
    }

    // factor → ( expr )
    //        | num { print(num.val) }
    //        | id  { print(id.lexeme) }
    private void factor() {
        if (isOperator(TokenType.OperatorType.LPAREN)) {
            matchOperator(TokenType.OperatorType.LPAREN);
            expr();
            matchOperator(TokenType.OperatorType.RPAREN);

        } else if (current.tokenType == TokenType.NUM) {
            double val = (Double) current.value;
            if (val == Math.floor(val)) {
                System.out.print((int) val + " ");
            } else {
                System.out.print(val + " ");
            }
            advance();

        } else if (current.tokenType == TokenType.ID) {
            System.out.print(current.value + " ");
            advance();

        } else {
            throw new RuntimeException(
                    "Syntax error at token position " + pos
                            + ": unexpected token '" + current + "'"
            );
        }
    }

    // main — test it here
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter an expression (or 'quit' to exit):");

        while (scanner.hasNextLine()) {
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("quit")) break;
            if (input.isEmpty()) continue;

            System.out.print("Postfix: ");
            try {
                List<Token> tokens = new Lexer().tokenise(input);
                if (tokens.isEmpty()) continue;  // lexer already printed error
                Parser parser = new Parser(tokens);
                parser.expr();
                System.out.println();
            } catch (RuntimeException e) {
                System.out.println("\n" + e.getMessage());
            }
            System.out.println();
        }
        scanner.close();
    }
}
