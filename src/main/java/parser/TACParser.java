package parser;

import lexer.Lexer;
import lexer.SymbolTable;
import lexer.Token;
import lexer.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class TACParser {

    private List<Token> tokens;
    private int pos;
    private Token current;

    private SymbolTable symbolTable;

    /** Accumulated TAC instructions, emitted in order during parsing. */
    private final List<String> instructions = new ArrayList<>();

    /** Counter for generating unique temporary keys in the symbol table. */
    private int tempKeyCounter = 1;

    public TACParser(List<Token> tokens, SymbolTable symbolTable) {
        this.tokens = tokens;
        this.symbolTable = symbolTable;
        this.pos = 0;
        this.current = tokens.get(0);
    }
    private void advance() {
        pos++;
        if (pos < tokens.size()) {
            current = tokens.get(pos);
        }
    }

    private boolean isOperator(TokenType.OperatorType op) {
        return current.tokenType == TokenType.OPERATOR && current.value == op;
    }

    private void matchOperator(TokenType.OperatorType expected) {
        if (isOperator(expected)) {
            advance();
        } else {
            throw new RuntimeException(
                "Syntax error at position " + pos
                + ": expected '" + expected.getSymbol()
                + "' but got '" + current + "'"
            );
        }
    }

    private String newTemp() {
        String key = "_t" + tempKeyCounter++;          // internal key
        SymbolTable.Symbol sym = symbolTable.insertTemp(key);
        return sym.getValue();                         // e.g. "t1"
    }

    /**
     * Emits one TAC instruction: result = left op right
     * and adds it to the instruction list.
     */
    private void emit(String result, String left, String op, String right) {
        instructions.add(result + " = " + left + " " + op + " " + right);
    }

    public String expr() {
        String termPlace = term();
        return rest(termPlace);
    }


    private String rest(String inherited) {
        if (isOperator(TokenType.OperatorType.PLUS)) {
            advance();
            String termPlace = term();
            String t = newTemp();
            emit(t, inherited, "+", termPlace);
            return rest(t);                            // pass new temp as next left operand

        } else if (isOperator(TokenType.OperatorType.MINUS)) {
            advance();
            String termPlace = term();
            String t = newTemp();
            emit(t, inherited, "-", termPlace);
            return rest(t);
        }

        // ε production — nothing left to consume, return what we have
        return inherited;
    }

    /**
     * term → factor term_tail
     */
    private String term() {
        String factorPlace = factor();
        return termTail(factorPlace);
    }

    private String termTail(String inherited) {
        if (isOperator(TokenType.OperatorType.MUL)) {
            advance();
            String factorPlace = factor();
            String t = newTemp();
            emit(t, inherited, "*", factorPlace);
            return termTail(t);

        } else if (isOperator(TokenType.OperatorType.DIV)) {
            advance();
            String factorPlace = factor();
            String t = newTemp();
            emit(t, inherited, "/", factorPlace);
            return termTail(t);
        }

        return inherited;
    }


    private String factor() {
        if (isOperator(TokenType.OperatorType.LPAREN)) {
            matchOperator(TokenType.OperatorType.LPAREN);
            String place = expr();
            matchOperator(TokenType.OperatorType.RPAREN);
            return place;

        } else if (current.tokenType == TokenType.NUM) {
            double val = (Double) current.value;
            advance();
            // Format: drop the ".0" for whole numbers
            if (val == Math.floor(val)) {
                return String.valueOf((int) val);
            } else {
                return String.valueOf(val);
            }

        } else if (current.tokenType == TokenType.ID) {
            // Look up the symbol table to get the mapped alias (v1, v2, ...)
            String lexeme = (String) current.value;
            advance();
            SymbolTable.Symbol sym = symbolTable.getOrInsert(lexeme);
            return sym.getValue();                     // e.g. "v1"

        } else {
            throw new RuntimeException(
                "Syntax error at position " + pos
                + ": unexpected token '" + current + "'"
            );
        }
    }


    public String generateAndPrint() {
        String resultPlace = expr();
        System.out.println("Three-Address Code:");
        if (instructions.isEmpty()) {
            // Single atom — no operations, nothing to emit
            System.out.println("  (no operations — result is: " + resultPlace + ")");
        } else {
            for (String instr : instructions) {
                System.out.println("  " + instr);
            }
        }
        System.out.println("  Result in: " + resultPlace);
        return resultPlace;
    }

    public List<String> getInstructions() {
        return instructions;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter an expression (or 'quit' to exit):\n");

        while (scanner.hasNextLine()) {
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("quit")) break;
            if (input.isEmpty()) continue;

            System.out.println("Input:  " + input);

            try {
                // Fresh symbol table per expression so variable numbering restarts
                SymbolTable symbolTable = new SymbolTable();
                List<Token> tokens = new Lexer(symbolTable).tokenise(input);

                if (tokens.isEmpty()) {
                    // Lexer already printed the error
                    System.out.println();
                    continue;
                }

                TACParser tacParser = new TACParser(tokens, symbolTable);
                tacParser.generateAndPrint();

            } catch (RuntimeException e) {
                System.out.println("Error: " + e.getMessage());
            }

            System.out.println();
        }

        scanner.close();
    }
}
