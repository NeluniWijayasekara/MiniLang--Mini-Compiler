import java.util.HashSet;
import java.util.Set;

// SymbolTable manages variable declarations and semantic error tracking
public class SymbolTable {
    private final Set<String> declaredVars = new HashSet<>();
    private boolean errorOccurred = false;

    // Declare a variable; report error if already declared
    public void declare(String varName, int line) {
        if (declaredVars.contains(varName)) {
            System.err.println(" Semantic Error: Variable '" + varName +
                    "' already declared at line " + line);
            errorOccurred = true;
        } else {
            declaredVars.add(varName);
        }
    }

    // Check if a variable is declared; report error if not
    public void checkDeclared(String varName, int line) {
        if (!declaredVars.contains(varName)) {
            System.err.println(" Semantic Error: Variable '" + varName +
                    "' not declared at line " + line);
            errorOccurred = true;
        }
    }

    // Returns true if any semantic errors occurred
    public boolean hasErrors() {
        return errorOccurred;
    }
}
