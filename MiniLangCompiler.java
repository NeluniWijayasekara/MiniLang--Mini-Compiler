import java.util.*;

public class MiniLangCompiler {
    private List<MiniLangLexer.Token> tokens;
    private int current = 0;
    private HashMap<String, Integer> symbolTable = new HashMap<>(); // variable -> line declared

    // For code generation
    private int tempCount = 1;
    private int labelCount = 1;

    public MiniLangCompiler(List<MiniLangLexer.Token> tokens) {
        this.tokens = tokens;
    }

    // Helper methods for code generation
    private String newTemp() {
        return "t" + (tempCount++);
    }
    private String newLabel() {
        return "L" + (labelCount++);
    }
    private void emit(String code) {
        System.out.println(code);
    }

    // Token helpers
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
    private void checkVariableDeclared(String varName, int line) {
        if (!symbolTable.containsKey(varName)) {
            System.out.println("Semantic error at line " + line + ": Variable '" + varName + "' used before declaration.");
            System.exit(1);
        }
    }

    // Main parse function
    public void parseProgram() {
        while (peek() != null) {
            parseStmt();
        }
    }

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
            default:
                System.out.println("Syntax error at line " + t.line + ": Unexpected token " + t.type);
                System.exit(1);
        }
    }

    // int a;
    private void parseDeclStmt() {
        expect(MiniLangLexer.TokenType.INT);
        MiniLangLexer.Token idToken = peek();
        expect(MiniLangLexer.TokenType.ID);
        expect(MiniLangLexer.TokenType.SEMICOLON);

        String varName = idToken.value;
        if (symbolTable.containsKey(varName)) {
            System.out.println("Semantic error at line " + idToken.line + ": Variable '" + varName + "' redeclared.");
            System.exit(1);
        } else {
            symbolTable.put(varName, idToken.line);
            emit(". " + varName);
        }
    }

    // a = expr;
    private void parseAssignStmt() {
        MiniLangLexer.Token idToken = peek();
        expect(MiniLangLexer.TokenType.ID);
        checkVariableDeclared(idToken.value, idToken.line);
        expect(MiniLangLexer.TokenType.ASSIGN);
        String rhs = parseExpr();
        expect(MiniLangLexer.TokenType.SEMICOLON);
        emit("= " + idToken.value + ", " + rhs);
    }

    // if (cond) block [else block]
    private void parseIfStmt() {
        expect(MiniLangLexer.TokenType.IF);
        expect(MiniLangLexer.TokenType.LPAREN);
        String condTemp = parseCond();
        expect(MiniLangLexer.TokenType.RPAREN);
        String labelElse = newLabel();
        String labelEnd = newLabel();
        emit("?:= " + labelElse + ", " + condTemp);
        parseBlock();
        if (peek() != null && peek().type == MiniLangLexer.TokenType.ELSE) {
            emit(":= " + labelEnd);
            emit(": " + labelElse);
            advance();
            parseBlock();
            emit(": " + labelEnd);
        } else {
            emit(": " + labelElse);
        }
    }

    // while (cond) block
    private void parseWhileStmt() {
        expect(MiniLangLexer.TokenType.WHILE);
        String labelStart = newLabel();
        String labelEnd = newLabel();
        emit(": " + labelStart);
        expect(MiniLangLexer.TokenType.LPAREN);
        String condTemp = parseCond();
        expect(MiniLangLexer.TokenType.RPAREN);
        emit("?:= " + labelEnd + ", " + condTemp);
        parseBlock();
        emit(":= " + labelStart);
        emit(": " + labelEnd);
    }

    // print(a);
    private void parsePrintStmt() {
        expect(MiniLangLexer.TokenType.PRINT);
        expect(MiniLangLexer.TokenType.LPAREN);
        MiniLangLexer.Token idToken = peek();
        expect(MiniLangLexer.TokenType.ID);
        checkVariableDeclared(idToken.value, idToken.line);
        expect(MiniLangLexer.TokenType.RPAREN);
        expect(MiniLangLexer.TokenType.SEMICOLON);
        emit(".> " + idToken.value);
    }

    // input(a);
    private void parseInputStmt() {
        expect(MiniLangLexer.TokenType.INPUT);
        expect(MiniLangLexer.TokenType.LPAREN);
        MiniLangLexer.Token idToken = peek();
        expect(MiniLangLexer.TokenType.ID);
        checkVariableDeclared(idToken.value, idToken.line);
        expect(MiniLangLexer.TokenType.RPAREN);
        expect(MiniLangLexer.TokenType.SEMICOLON);
        emit(".< " + idToken.value);
    }

    // { ... } or single statement
    private void parseBlock() {
        if (peek() != null && peek().type == MiniLangLexer.TokenType.LBRACE) {
            expect(MiniLangLexer.TokenType.LBRACE);
            while (peek() != null && peek().type != MiniLangLexer.TokenType.RBRACE) {
                parseStmt();
            }
            expect(MiniLangLexer.TokenType.RBRACE);
        } else {
            parseStmt();
        }
    }

    // cond: expr relop expr (like a > 0)
    private String parseCond() {
        String left = parseExpr();
        MiniLangLexer.Token opToken = peek();
        if (peek() != null &&
                (peek().type == MiniLangLexer.TokenType.GT ||
                        peek().type == MiniLangLexer.TokenType.LT ||
                        peek().type == MiniLangLexer.TokenType.ASSIGN)) { // '=' as '=='
            advance();
            String right = parseExpr();
            String temp = newTemp();
            String op = opToken.type == MiniLangLexer.TokenType.GT ? ">" :
                    opToken.type == MiniLangLexer.TokenType.LT ? "<" : "=";
            emit(op + " " + temp + ", " + left + ", " + right);
            return temp;
        } else {
            System.out.println("Syntax error at line " + (peek() != null ? peek().line : "EOF") + ": Expected relational operator");
            System.exit(1);
            return null;
        }
    }

    // expr: term { (+|-) term }
    private String parseExpr() {
        String left = parseTerm();
        while (peek() != null &&
                (peek().type == MiniLangLexer.TokenType.PLUS ||
                        peek().type == MiniLangLexer.TokenType.MINUS)) {
            MiniLangLexer.Token opToken = peek();
            advance();
            String right = parseTerm();
            String temp = newTemp();
            String op = opToken.type == MiniLangLexer.TokenType.PLUS ? "+" : "-";
            emit(op + " " + temp + ", " + left + ", " + right);
            left = temp;
        }
        return left;
    }

    // term: factor { (*|/) factor }
    private String parseTerm() {
        String left = parseFactor();
        while (peek() != null &&
                (peek().type == MiniLangLexer.TokenType.TIMES ||
                        peek().type == MiniLangLexer.TokenType.DIVIDE)) {
            MiniLangLexer.Token opToken = peek();
            advance();
            String right = parseFactor();
            String temp = newTemp();
            String op = opToken.type == MiniLangLexer.TokenType.TIMES ? "*" : "/";
            emit(op + " " + temp + ", " + left + ", " + right);
            left = temp;
        }
        return left;
    }

    // factor: ID | NUMBER | (expr)
    private String parseFactor() {
        if (peek() != null && peek().type == MiniLangLexer.TokenType.ID) {
            MiniLangLexer.Token idToken = peek();
            advance();
            checkVariableDeclared(idToken.value, idToken.line);
            return idToken.value;
        } else if (peek() != null && peek().type == MiniLangLexer.TokenType.NUMBER) {
            String num = peek().value;
            advance();
            return num;
        } else if (peek() != null && peek().type == MiniLangLexer.TokenType.LPAREN) {
            expect(MiniLangLexer.TokenType.LPAREN);
            String expr = parseExpr();
            expect(MiniLangLexer.TokenType.RPAREN);
            return expr;
        } else {
            System.out.println("Syntax error at line " + (peek() != null ? peek().line : "EOF") +
                    ": Expected ID, NUMBER, or '('");
            System.exit(1);
            return null;
        }
    }

    // MAIN METHOD
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
        List<MiniLangLexer.Token> tokens = MiniLangLexer.lex(input);
        MiniLangCompiler compiler = new MiniLangCompiler(tokens);
        compiler.parseProgram();
        System.out.println("Intermediate code generation completed.");
    }
}
