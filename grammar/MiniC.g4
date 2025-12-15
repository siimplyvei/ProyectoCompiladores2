grammar MiniC;

// --------- PARSER ---------

program
    : (funcDef)+ EOF
    ;

declaration
    : typeSpecifier declaratorList ';'
    ;

declaratorList
    : declarator (',' declarator)*
    ;

declarator
    : Identifier ('[' IntegerConst ']')*
    | '*' declarator
    ;

typeSpecifier
    : 'int'
    | 'char'
    | 'bool'
    | 'void'
    | 'string'
    ;

funcDef
    : typeSpecifier Identifier '(' paramList? ')' compoundStmt
    ;

paramList
    : param (',' param)*
    ;

param
    : typeSpecifier Identifier
    ;

compoundStmt
    : '{' (declaration | statement)* '}'
    ;

statement
    : compoundStmt
    | ifStmt
    | whileStmt
    | forStmt
    | returnStmt
    | assignStmt
    | exprStmt
    ;

ifStmt
    : 'if' '(' expr ')' statement ('else' statement)?
    ;

whileStmt
    : 'while' '(' expr ')' statement
    ;

assignStmt
    : lvalue '=' expr
    ;

returnStmt
    : 'return' expr? ';'
    ;

exprStmt
    : expr? ';'
    ;

expr
    : logicalOrExpr
    ;

argList
    : expr (',' expr)*
    ;

logicalOrExpr
    : logicalAndExpr ('||' logicalAndExpr)*
    ;

logicalAndExpr
    : equalityExpr ('&&' equalityExpr)*
    ;

equalityExpr
    : relationalExpr (('==' | '!=') relationalExpr)*
    ;

forStmt
    : 'for' '(' forInit ';' forCond ';' forStep ')' statement
    ;

forInit
    : assignStmt
    | expr
    | /* vacío */
    ;

forCond
    : expr
    | /* vacío */
    ;

forStep
    : assignStmt
    | expr
    | /* vacío */
    ;

relationalExpr
    : additiveExpr (('<' | '>' | '<=' | '>=') additiveExpr)*
    ;

additiveExpr
    : multiplicativeExpr (('+' | '-') multiplicativeExpr)*
    ;

multiplicativeExpr
    : unaryExpr (('*' | '/' | '%') unaryExpr)*
    ;

unaryExpr
    : ('!' | '-' | '*' | '&') unaryExpr
    | primary
    ;

primary
    : Identifier '(' argList? ')'
    | '(' expr ')'
    | lvalue
    | IntegerConst
    | CharConst
    | StringLiteral
    | 'true'
    | 'false'
    ;

call
    : Identifier '(' (expr (',' expr)*)? ')'
    ;

lvalue
    : Identifier
    | Identifier '[' expr ']'
    | Identifier '[' expr ']' '[' expr ']'
    ;


// --------- LEXER ---------

Identifier
    : [A-Za-z_] [A-Za-z0-9_]*
    ;

IntegerConst
    : [0-9]+
    ;

CharConst
    : '\'' . '\''
    ;

StringLiteral
    : '"' (~["\r\n])* '"'
    ;

WS
    : [ \t\r\n]+ -> skip
    ;

LINE_COMMENT
    : '//' ~[\r\n]* -> skip
    ;

BLOCK_COMMENT
    : '/*' .*? '*/' -> skip
    ;
