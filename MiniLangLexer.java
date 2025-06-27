import java.util.*;
import java.util.regex.*;
import java.io.*;

public class MiniLangLexer {

    enum TokenType {
        INT, IF, ELSE, WHILE, PRINT, INPUT,
        ID, NUMBER,
        PLUS, MINUS, TIMES, DIVIDE,
        ASSIGN, GT, LT,
        LPAREN, RPAREN, LBRACE, RBRACE, SEMICOLON,
        COMMENT, WHITESPACE,
        UNKNOWN
    }

    static class Token {
        TokenType type;
        String value;
        int line;

        Token(TokenType type, String value, int line) {
            this.type = type;
            this.value = value;
            this.line = line;
        }

        public String toString() {
            return type + "(" + value + ") at line " + line;
        }
    }

    private static final Map<TokenType, String> tokenPatterns = new LinkedHashMap<>();
    static {
        tokenPatterns.put(TokenType.WHITESPACE, "\\s+");
        tokenPatterns.put(TokenType.COMMENT, "//.*");
        tokenPatterns.put(TokenType.INT, "\\bint\\b");
        tokenPatterns.put(TokenType.IF, "\\bif\\b");
        tokenPatterns.put(TokenType.ELSE, "\\belse\\b");
        tokenPatterns.put(TokenType.WHILE, "\\bwhile\\b");
        tokenPatterns.put(TokenType.PRINT, "\\bprint\\b");
        tokenPatterns.put(TokenType.INPUT, "\\binput\\b");
        tokenPatterns.put(TokenType.NUMBER, "\\b\\d+\\b");
        tokenPatterns.put(TokenType.ID, "\\b[a-zA-Z_][a-zA-Z0-9_]*\\b");
        tokenPatterns.put(TokenType.PLUS, "\\+");
        tokenPatterns.put(TokenType.MINUS, "-");
        tokenPatterns.put(TokenType.TIMES, "\\*");
        tokenPatterns.put(TokenType.DIVIDE, "/");
        tokenPatterns.put(TokenType.ASSIGN, "=");
        tokenPatterns.put(TokenType.GT, ">");
        tokenPatterns.put(TokenType.LT, "<");
        tokenPatterns.put(TokenType.LPAREN, "\\(");
        tokenPatterns.put(TokenType.RPAREN, "\\)");
        tokenPatterns.put(TokenType.LBRACE, "\\{");
        tokenPatterns.put(TokenType.RBRACE, "\\}");
        tokenPatterns.put(TokenType.SEMICOLON, ";");
    }

    private static final Pattern masterPattern;
    private static final List<TokenType> masterTypes = new ArrayList<>();

    static {
        StringBuilder masterPat = new StringBuilder();
        for (Map.Entry<TokenType, String> entry : tokenPatterns.entrySet()) {
            if (masterPat.length() > 0) masterPat.append("|");
            masterPat.append("(").append(entry.getValue()).append(")");
            masterTypes.add(entry.getKey());
        }
        masterPattern = Pattern.compile(masterPat.toString());
    }

    public static List<Token> lex(String input) {
        List<Token> tokens = new ArrayList<>();
        String[] lines = input.split("\n");
        boolean errorFound = false;

        for (int lineNum = 0; lineNum < lines.length; lineNum++) {
            String line = lines[lineNum];
            int pos = 0;
            while (pos < line.length()) {
                Matcher matcher = masterPattern.matcher(line);
                matcher.region(pos, line.length());
                if (matcher.lookingAt()) {
                    for (int i = 1; i <= matcher.groupCount(); i++) {
                        if (matcher.group(i) != null) {
                            TokenType type = masterTypes.get(i - 1);
                            String value = matcher.group(i);
                            if (type == TokenType.WHITESPACE || type == TokenType.COMMENT) {
                                // skip
                            } else {
                                tokens.add(new Token(type, value, lineNum + 1));
                            }
                            pos = matcher.end();
                            break;
                        }
                    }
                } else {
                    System.err.println("Lexical Error: Unrecognized character '" +
                            line.charAt(pos) + "' at line " + (lineNum + 1));
                    errorFound = true;
                    pos++;
                }
            }
        }
        if (errorFound) {
            System.err.println("Lexical analysis completed with errors.");
        }
        return tokens;
    }

    public static void main(String[] args) throws IOException {
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
        List<Token> tokens = lex(input);
        System.out.println("\nTokens:");
        for (Token token : tokens) {
            System.out.println(token);
        }
    }
}
