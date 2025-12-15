grammar MiniC;

// --------- PARSER ---------

program
    : (declaration | funcDef)* EOF
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
    : typeSpecifier Identifier '(' params? ')' compoundStmt
    ;

params
    : param (',' param)*
    ;

param
    : typeSpecifier declarator
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
    : IntegerConst
    | CharConst
    | StringLiteral
    | 'true'
    | 'false'
    | '(' expr ')'
    | lvalue
    | call
    ;

call
    : Identifier '(' (expr (',' expr)*)? ')'
    ;

lvalue
    : Identifier ('[' expr ']')*
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
