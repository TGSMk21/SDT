package lexer;

import lexer.Exceptions.SymbolAlreadyDeclaredException;
import lexer.Exceptions.SymbolNotFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages the mapping of identifiers and temporaries for the Syntax-Directed Translator.
 * This class handles scope management and unique name generation (v1, t1).
 */
public class SymbolTable {
    private final Map<String, Symbol> table = new HashMap<>();
    private int varCount = 1;
    private int tempCount = 1;

    /**
     * Inserts a new user defined identifier into the table.
     * Assigns a unique 'v' alias (v1) for postfix output.
     */
    public Symbol insert(String name) {
        if (table.containsKey(name)) {
            throw new SymbolAlreadyDeclaredException("Identifier '" + name + "' already declared");
        }

        Symbol symbol = new Symbol("Identifier");

        symbol.setValue("v" + varCount++); // Map to unique variable name
        table.put(name, symbol);

        return symbol;
    }

    /**
     * Creates a temporary variable for Three-Address Code generation.
     * Used for intermediate results of operations (t1 = a + b).
     */
    public Symbol insertTemp(String name) {
        if (table.containsKey(name)) {
            throw new SymbolAlreadyDeclaredException("Temporary already exist");
        }

        Symbol symbol = new Symbol("Temporary");

        symbol.setValue("t" + tempCount++);
        table.put(name,symbol);
        return symbol;
    }

    /**
     * Retrieves the symbol associated with a name.
     * Used during factor -> id production to find existing mappings.
     */
    public Symbol getOrInsert(String name) {
        if (table.containsKey(name)) {
            return table.get(name);
        } else {
            return insert(name);
        }
    }

    /**
     * Debugging method to print the current state of the symbol table.
     */
    public void getValue() {
        table.forEach((k, v) -> {
            System.out.print("Key -> " + k + " values -> " + v);
        });
    }

    public boolean contains(String name) {
        return table.containsKey(name);
    }

    /**
     * Updates an existing symbol's attributes.
     * Essential for storing data types during semantic analysis.
     */
    public void update(String name, String type) {
        Symbol s = table.get(name);
        if (s == null) {
            throw new SymbolNotFoundException("Cannot update '" + name + "': symbol not found");
        }

       if(type != null) s.setType(type);
    }

    /**
     * Inner class representing a single entry in the Symbol Table.
     * Stores the category, semantic type, and mapped output value.
     */
    public static class Symbol {
       private final String category; // "Identifier" or "Temporary"
       private String type; // e.g., "int", "float"
       private String value; // The alias used in output (v1, t1)

        public Symbol(String category) {
            this.category = category;
        }

        // Getters and Setters
        public String getCategory() {return category;}
        public String getType() {return type;}
        public String getValue() {return value;}
        public void setType(String type) {this.type = type;}
        public void setValue(String value) {this.value = value;}
        @Override
        public String toString() {
            return "Category = " + this.category + ", Type =" + this.type + " ,Value = " + this.value;
        }
    }
}
