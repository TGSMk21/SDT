package parser;

import lexer.Lexer;
import lexer.SymbolTable;
import lexer.Token;
import lexer.TokenType;

import java.util.List;
import java.util.Scanner;

public class Main {

    static class PostfixParser {
        private final List<Token> tokens;
        private final SymbolTable symbolTable;
        private final boolean useSymbolTable;
        private int pos;
        private Token current;

        PostfixParser(List<Token> tokens, SymbolTable symbolTable, boolean useSymbolTable) {
            this.tokens = tokens;
            this.symbolTable = symbolTable;
            this.useSymbolTable = useSymbolTable;
            this.pos = 0;
            this.current = tokens.get(0);
        }

        private void advance() {
            pos++;
            if (pos < tokens.size()) current = tokens.get(pos);
        }

        private boolean isOp(TokenType.OperatorType op) {
            return current.tokenType == TokenType.OPERATOR && current.value == op;
        }

        private void matchOp(TokenType.OperatorType op) {
            if (isOp(op)) advance();
            else throw new RuntimeException(
                "Expected '" + op.getSymbol() + "' at position " + pos + ", got: " + current);
        }

        void expr()     { term(); rest(); }

        void rest() {
            if (isOp(TokenType.OperatorType.PLUS))       { advance(); term(); System.out.print("+ "); rest(); }
            else if (isOp(TokenType.OperatorType.MINUS)) { advance(); term(); System.out.print("- "); rest(); }
        }

        void term()     { factor(); termTail(); }

        void termTail() {
            if (isOp(TokenType.OperatorType.MUL))        { advance(); factor(); System.out.print("* "); termTail(); }
            else if (isOp(TokenType.OperatorType.DIV))   { advance(); factor(); System.out.print("/ "); termTail(); }
        }

        void factor() {
            if (isOp(TokenType.OperatorType.LPAREN)) {
                matchOp(TokenType.OperatorType.LPAREN);
                expr();
                matchOp(TokenType.OperatorType.RPAREN);
            } else if (current.tokenType == TokenType.NUM) {
                double v = (Double) current.value;
                System.out.print((v == Math.floor(v) ? (int) v : v) + " ");
                advance();
            } else if (current.tokenType == TokenType.ID) {
                String lexeme = (String) current.value;
                if (useSymbolTable) {
                    System.out.print(symbolTable.getOrInsert(lexeme).getValue() + " ");
                } else {
                    System.out.print(lexeme + " ");
                }
                advance();
            } else {
                throw new RuntimeException("Unexpected token at position " + pos + ": " + current);
            }
        }
    }

    private static void runPostfix(String input) {
        try {
            SymbolTable st = new SymbolTable();
            List<Token> tokens = new Lexer(st).tokenise(input);
            if (tokens.isEmpty()) return;
            System.out.print("Postfix: ");
            new PostfixParser(tokens, st, false).expr();
            System.out.println();
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void runPostfixWithSymbolTable(String input) {
        try {
            SymbolTable st = new SymbolTable();
            List<Token> tokens = new Lexer(st).tokenise(input);
            if (tokens.isEmpty()) return;
            System.out.print("Postfix: ");
            new PostfixParser(tokens, st, true).expr();
            System.out.println();
            // Print the variable mappings so the output makes sense
            System.out.print("Variable mappings: ");
            st.getValue();
            System.out.println();
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void runThreeAddressCode(String input) {
        try {
            SymbolTable st = new SymbolTable();
            List<Token> tokens = new Lexer(st).tokenise(input);
            if (tokens.isEmpty()) return;
            TACParser tac = new TACParser(tokens, st);
            String result = tac.expr();
            List<String> instrs = tac.getInstructions();
            System.out.println("Three-Address Code:");
            if (instrs.isEmpty()) {
                System.out.println("  (no operations — result is: " + result + ")");
            } else {
                for (String instr : instrs) System.out.println("  " + instr);
            }
            System.out.println("  Result in: " + result);
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }


    private static void printMenu() {

        System.out.println("  Select a translation mode:");
        System.out.println("  [1] Postfix Notation");
        System.out.println("  [2] Postfix with Symbol-Table Mapping");
        System.out.println("  [3] Three-Address Code Generation");
        System.out.println("  [q] Quit");

    }



    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String mode = null;

        printMenu();
        System.out.print("Select mode: ");

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();

            if (line.equalsIgnoreCase("q") || line.equalsIgnoreCase("quit")) {
                System.out.println("Goodbye.");
                break;
            }

            if (line.equalsIgnoreCase("m")) {
                printMenu();
                System.out.print("Select mode: ");
                continue;
            }

            if (line.equals("1") || line.equals("2") || line.equals("3")) {
                mode = line;
                String label = switch (mode) {
                    case "1" -> "Postfix Notation";
                    case "2" -> "Postfix with Symbol-Table Mapping";
                    case "3" -> "Three-Address Code Generation";
                    default  -> "";
                };
                System.out.println("Mode set: " + label);
                System.out.println("Enter an expression (or 'm' for menu, 'q' to quit):");
                continue;
            }

            if (line.isEmpty()) continue;

            if (mode == null) {
                System.out.println("Please select a mode first (1, 2, or 3).");
                printMenu();
                System.out.print("Select mode: ");
                continue;
            }

            System.out.println("Input: " + line);
            switch (mode) {
                case "1" -> runPostfix(line);
                case "2" -> runPostfixWithSymbolTable(line);
                case "3" -> runThreeAddressCode(line);
            }
            System.out.println();
        }

        scanner.close();
    }
}
