package lexer.Exceptions;

public class SymbolAlreadyDeclaredException extends RuntimeException {
    public SymbolAlreadyDeclaredException(String message) {
        super(message);
    }
}
