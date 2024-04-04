package CODE;

import java.util.HashMap;
import java.util.Map;
public class Environment {
    final Environment enclosing;
    private final Map<String, Object> values = new HashMap<>();
    private final Map<String, String> types = new HashMap<>();
    Environment() {
        enclosing = null;
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }
    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }
        if (enclosing != null) return enclosing.get(name);
        throw new RuntimeError(name,
                "Undefined variable '" + name.lexeme + "'.");
    }
    String getType(Token name) {
        if (types.containsKey(name.lexeme)) {
            return types.get(name.lexeme);
        }
        if (enclosing != null) return enclosing.getType(name);
        throw new RuntimeError(name,
                "Undefined variable '" + name.lexeme + "'.");
    }
    void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            if (values.get(name.lexeme).getClass() == value.getClass()) {
                values.put(name.lexeme, value);
                return;
            }
            throw new RuntimeError(name,
                    "Assignment not matching types '" + name.lexeme + "'.");
        }
        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }
        throw new RuntimeError(name,
                "Undefined variable '" + name.lexeme + "'.");
    }
    void define(Token name, Object value, String type) {
        if (type.equals("BOOL") && value instanceof Boolean || type.equals("INT") && value instanceof Integer || type.equals("FLOAT") && value instanceof Double ) {
            values.put(name.lexeme, value);
            types.put(name.lexeme, type);
        } else {
            throw new RuntimeError(name,
                    "Wrong type assignment '" + name.lexeme + "'.");
        }

    }

}
