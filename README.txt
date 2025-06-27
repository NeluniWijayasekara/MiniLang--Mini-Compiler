
MiniLang Compiler - COSC 44283 - Theory of Compilers

A simple compiler project developed for the COSC 44283 - Theory of Compilers course. MiniLang is a custom-designed language that supports basic programming constructs like declarations, assignments, loops, conditionals, and print statements. This project implements lexical analysis, parsing, semantic analysis, and intermediate code generation.

Project Information
-------------------
- Course: COSC 44283 - Theory of Compilers
- Student Name: Wijayasekara N.
- Student ID: PS/2020/188
- Project Title: MiniLang - Mini Compiler Development Project
- Language Used: Java
- IDE: IntelliJ IDEA
- OS: Windows 10/11

Features
--------
MiniLang supports:
- Variable declarations: int a;
- Assignments: a = 5;
- Arithmetic expressions: a = b + c * 2;
- Conditionals: if (a > 0) { ... } else { ... }
- Loops: while (a < 10) { ... }
- Input/Output: input(a);, print(a);

Project Structure
-----------------
MiniLangCompiler/
├── MiniLangLexer.java         # Lexical analyzer
├── MiniLangParser.java        # Parser and semantic checker
├── SymbolTable.java           # Symbol table for semantic analysis
├── MiniLangCompiler.java      # Intermediate code generator
├── sample_input.txt           # Sample MiniLang source code
└── README.txt                 # Project documentation

How to Run
----------
1. Compile the source files:
   javac MiniLangLexer.java MiniLangParser.java SymbolTable.java MiniLangCompiler.java

2. Run only lexical, syntax, and semantic analysis:
   java MiniLangParser

3. Run full compiler with intermediate code generation:
   java MiniLangCompiler

4. Input instructions:
   - Paste your MiniLang code into the terminal.
   - End input with a blank line.

Example Code
------------
int a;
int b;
a = 5;
b = a + 2;
print(b);

Sample Output (Intermediate Code)
---------------------------------
. a
. b
= a, 5
+ t1, a, 2
= b, t1
.> b

Compiler Phases Implemented
----------------------------
Lexical Analysis      - Done
Syntax Analysis       - Done
Semantic Analysis     - Done
Intermediate Code     - Done
Error Handling        - Done
Symbol Table          - Done

Error Reporting
---------------
- Unrecognized characters are flagged during lexical analysis.
- Syntax and semantic errors report line numbers and halt compilation.
- No intermediate code is generated if errors are detected.

License
-------
This project is for academic use only (University of Kelaniya). Redistribution or reuse without permission is prohibited.

Contact
-------
For questions or improvements, please contact:
Neluni Wijayasekara
neluni@gmai.com

