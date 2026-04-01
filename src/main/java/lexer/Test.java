package lexer;

import java.util.List;
//test class for the lexical parser
public class Test {
    public static void main(String[] args) {
        SymbolTable symbolTable = new SymbolTable();
        List<Token> tokens = new Lexer(symbolTable).tokenise("bal + 4 * 5 - 3");
        //print each token
        tokens.forEach(System.out::println);

        symbolTable.getValue();
    }
}
