import java.util.List;
import java.util.Scanner;

public class MiniLangParser {
    private final List<MiniLangLexer.Token> tokens;
    private int current = 0;
    private final SymbolTable symbolTable = new SymbolTable();

    public MiniLangParser(List<MiniLangLexer.Token> tokens) {
        this.tokens = tokens;
    }

    // ===== Helper Methods =====
    private MiniLangLexer.Token peek() {
        return current < tokens.size() ? tokens.get(current) : null;
    }
    private void advance() {
        if (current < tokens.size()) current++;
    }
    private boolean match(MiniLangLexer.TokenType expected) {
        if (peek() != null && peek().type == expected) {
            advance();
            return true;
        }
        return false;
    }
    private void expect(MiniLangLexer.TokenType expected) {
        if (!match(expected)) {
            MiniLangLexer.Token t = peek();
            System.out.println("Syntax error at line " + (t != null ? t.line : "EOF") +
                    ": Expected " + expected + ", found " + (t != null ? t.type : "EOF"));
            System.exit(1);
        }
    }

    // ===== Entry Point =====
    public void parseProgram() {
        while (peek() != null) {
            parseStmt();
        }
        if (symbolTable.hasErrors()) {
            System.err.println("Semantic analysis completed with errors.");
            System.exit(1);
        }
    }

    // ===== Statement Parsers with Semantic Checks =====
    private void parseStmt() {
        MiniLangLexer.Token t = peek();
        if (t == null) return;
        switch (t.type) {
            case INT:    parseDeclStmt(); break;
            case ID:     parseAssignStmt(); break;
            case IF:     parseIfStmt(); break;
            case WHILE:  parseWhileStmt(); break;
            case PRINT:  parsePrintStmt(); break;
            case INPUT:  parseInputStmt(); break;
            default:     handleUnexpectedToken(t);
        }
    }

    // Declaration: int <id>;
    private void parseDeclStmt() {
        expect(MiniLangLexer.TokenType.INT);
        MiniLangLexer.Token idToken = peek();
        expect(MiniLangLexer.TokenType.ID);
        symbolTable.declare(idToken.value, idToken.line); // Semantic check
        expect(MiniLangLexer.TokenType.SEMICOLON);
    }

    // Assignment: <id> = <expr>;
    private void parseAssignStmt() {
        MiniLangLexer.Token idToken = peek();
        expect(MiniLangLexer.TokenType.ID);
        symbolTable.checkDeclared(idToken.value, idToken.line); // Semantic check
        expect(MiniLangLexer.TokenType.ASSIGN);
        parseExpr();
        expect(MiniLangLexer.TokenType.SEMICOLON);
    }

    // If statement
    private void parseIfStmt() {
        expect(MiniLangLexer.TokenType.IF);
        expect(MiniLangLexer.TokenType.LPAREN);
        parseCond();
        expect(MiniLangLexer.TokenType.RPAREN);
        parseBlock();
        if (match(MiniLangLexer.TokenType.ELSE)) {
            parseBlock();
        }
    }

    // While statement
    private void parseWhileStmt() {
        expect(MiniLangLexer.TokenType.WHILE);
        expect(MiniLangLexer.TokenType.LPAREN);
        parseCond();
        expect(MiniLangLexer.TokenType.RPAREN);
        parseBlock();
    }

    // Print statement
    private void parsePrintStmt() {
        expect(MiniLangLexer.TokenType.PRINT);
        expect(MiniLangLexer.TokenType.LPAREN);
        MiniLangLexer.Token idToken = peek();
        expect(MiniLangLexer.TokenType.ID);
        symbolTable.checkDeclared(idToken.value, idToken.line); // Semantic check
        expect(MiniLangLexer.TokenType.RPAREN);
        expect(MiniLangLexer.TokenType.SEMICOLON);
    }

    // Input statement
    private void parseInputStmt() {
        expect(MiniLangLexer.TokenType.INPUT);
        expect(MiniLangLexer.TokenType.LPAREN);
        MiniLangLexer.Token idToken = peek();
        expect(MiniLangLexer.TokenType.ID);
        symbolTable.checkDeclared(idToken.value, idToken.line); // Semantic check
        expect(MiniLangLexer.TokenType.RPAREN);
        expect(MiniLangLexer.TokenType.SEMICOLON);
    }

    // Block: { ... } or single statement
    private void parseBlock() {
        if (match(MiniLangLexer.TokenType.LBRACE)) {
            while (!match(MiniLangLexer.TokenType.RBRACE)) {
                parseStmt();
            }
        } else {
            parseStmt();
        }
    }

    // Condition: <expr> relop <expr>
    private void parseCond() {
        parseExpr();
        if (peek() != null && isRelOp(peek().type)) {
            advance();
            parseExpr();
        } else {
            reportMissingRelOp();
        }
    }

    // ===== Expression Parsing with Semantic Checks =====
    private void parseExpr() {
        parseTerm();
        while (peek() != null && isAddOp(peek().type)) {
            advance();
            parseTerm();
        }
    }

    private void parseTerm() {
        parseFactor();
        while (peek() != null && isMulOp(peek().type)) {
            advance();
            parseFactor();
        }
    }

    private void parseFactor() {
        if (match(MiniLangLexer.TokenType.ID)) {
            MiniLangLexer.Token idToken = tokens.get(current - 1);
            symbolTable.checkDeclared(idToken.value, idToken.line); // Semantic check
        } else if (match(MiniLangLexer.TokenType.NUMBER)) {
            // No semantic check needed for number
        } else if (match(MiniLangLexer.TokenType.LPAREN)) {
            parseExpr();
            expect(MiniLangLexer.TokenType.RPAREN);
        } else {
            reportInvalidFactor();
        }
    }

    // ===== Utility Methods =====
    private boolean isRelOp(MiniLangLexer.TokenType type) {
        return type == MiniLangLexer.TokenType.GT ||
                type == MiniLangLexer.TokenType.LT ||
                type == MiniLangLexer.TokenType.ASSIGN;
    }
    private boolean isAddOp(MiniLangLexer.TokenType type) {
        return type == MiniLangLexer.TokenType.PLUS ||
                type == MiniLangLexer.TokenType.MINUS;
    }
    private boolean isMulOp(MiniLangLexer.TokenType type) {
        return type == MiniLangLexer.TokenType.TIMES ||
                type == MiniLangLexer.TokenType.DIVIDE;
    }
    private void handleUnexpectedToken(MiniLangLexer.Token t) {
        System.out.println("Syntax error at line " + t.line +
                ": Unexpected token " + t.type);
        System.exit(1);
    }
    private void reportMissingRelOp() {
        System.out.println("Syntax error at line " +
                (peek() != null ? peek().line : "EOF") +
                ": Expected relational operator");
        System.exit(1);
    }
    private void reportInvalidFactor() {
        System.out.println("Syntax error at line " +
                (peek() != null ? peek().line : "EOF") +
                ": Expected ID, NUMBER, or '('");
        System.exit(1);
    }

    // ===== Main Method for Testing =====
    public static void main(String[] args) {
        System.out.println("Enter your MiniLang code (finish input with an empty line):");
        Scanner scanner = new Scanner(System.in);
        StringBuilder inputBuilder = new StringBuilder();
        String line;
        while (true) {
            line = scanner.nextLine();
            if (line.trim().isEmpty()) break;
            inputBuilder.append(line).append("\n");
        }
        String input = inputBuilder.toString();
        if (input.trim().isEmpty()) {
            System.out.println("No input provided. Exiting.");
            return;
        }
        // Lexical analysis
        List<MiniLangLexer.Token> tokens = MiniLangLexer.lex(input);
        // Syntax and semantic analysis
        MiniLangParser parser = new MiniLangParser(tokens);
        parser.parseProgram();
        System.out.println("Parsing and semantic analysis completed: No errors found.");
    }
}
