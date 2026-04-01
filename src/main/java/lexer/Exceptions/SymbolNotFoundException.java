package lexer.Exceptions;

public class SymbolNotFoundException extends RuntimeException {
    public SymbolNotFoundException(String message) {
        super(message);
    }
}
