package com.ngandroid.lib.interpreter;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by davityle on 1/13/15.
 *
 Lexical analysis This is the initial part of reading and analysing the program text:
 The text is read and divided into tokens, each of which corresponds to a symbol
 in the programming language, e.g., a variable name, keyword or number.

 Syntax analysis This phase takes the list of tokens produced by the lexical analysis
 and arranges these in a tree-structure (called the syntax tree) that reflects the
 structure of the program. This phase is often called parsing.

 Type checking This phase analyses the syntax tree to determine if the program
 violates certain consistency requirements, e.g., if a variable is used but not
 declared or if it is used in a context that does not make sense given the type
 of the variable, such as trying to use a boolean value as a function pointer.

 Intermediate code generation The program is translated to a simple machine independent
 intermediate language.

 Register allocation The symbolic variable names used in the intermediate code
 are translated to numbers, each of which corresponds to a register in the
 target machine code. .3. INTERPRETERS 3

 Machine code generation The intermediate language is translated to assembly
 language (a textual representation of machine code) for a specific machine
 architecture.

 Assembly and linking The assembly-language code is translated into binary representation
 and addresses of variables, functions, etc., are determined.
 */
public class Tokenizer {

    private enum State {
        BEGIN,
        CHAR_SEQUENCE,
        MODEL_FIELD,
        FUNCTION_PARAMETER,
        MODEL_FIELD_END,
        OPEN_PARENTHESIS,
        CLOSE_PARENTHESIS,
        END,
        COMMA,
        QUESTION_MARK,
        COLON,
        PERIOD,
        NOT_EQUALS,
        EQUALS_EQUALS,
        OPERATOR,
        STRING,
        DIGIT
    }

    private int index, readIndex;
    private String script;
    private Queue<Token> tokens;
    private char currentCharacter = 0;

    public Tokenizer(String script){
        this.script = script.replaceAll("\\s+(?=([^']*'[^']*')*[^']*$)","");
    }

    public Queue<Token> getTokens(){
        if(tokens == null){
            generateTokens();
        }
        return tokens;
    }

    private void generateTokens() {
        tokens = new LinkedList<>();
        index = 0;
        readIndex = 0;

        State state = State.BEGIN;
        while (state != State.END) {
            state = nextState(state);
        }
        if(readIndex != index){
            emit(TokenType.RUBBISH);
        }

        tokens.add(new Token(TokenType.EOF, null));
    }

    private State nextState(State state) {
        State result;
        switch (state) {
            case BEGIN:
                result = getNextState();
                break;
            case CHAR_SEQUENCE:
                char c = peek();
                if(c == '.'){
                    emit(TokenType.MODEL_NAME);
                }else if(c == '('){
                    emit(TokenType.FUNCTION_NAME);
                }else if(!Character.isLetter(c)){
                    emit(TokenType.RUBBISH);
                }
                result = getNextState();
                break;
            case QUESTION_MARK:
                //*
                emit(TokenType.TERNARY_QUESTION_MARK);
                result = getNextState();
                break;
            case COLON:
                //*
                emit(TokenType.TERNARY_COLON);
                result = getNextState();
                break;
            case PERIOD:
                result = State.MODEL_FIELD;
                emit(TokenType.PERIOD);
                break;
            case MODEL_FIELD: {
                if(!Character.isLetter(peek())){
                    emit(TokenType.MODEL_FIELD);
                    result = getNextState();
                }else{
                    advance();
                    result = State.MODEL_FIELD;
                }
                break;
            }
            case FUNCTION_PARAMETER: {
                State current = getNextState();
                char peek = peek();
                if(peek == ')' || peek == ','){
                    emit(TokenType.MODEL_FIELD);
                }else if(peek == '.'){
                    emit(TokenType.MODEL_NAME);
                }
                if(current == State.PERIOD){
                    emit(TokenType.PERIOD);
                    result = State.FUNCTION_PARAMETER;
                }else if (current == State.CHAR_SEQUENCE) {
                    result = State.FUNCTION_PARAMETER;
                } else{
                    result = current;
                }
                break;
            }
            case COMMA:
                emit(TokenType.COMMA);
                result = State.FUNCTION_PARAMETER;
                break;
            case OPEN_PARENTHESIS:
                result = State.FUNCTION_PARAMETER;
                emit(TokenType.OPEN_PARENTHESIS);
                break;
            case CLOSE_PARENTHESIS:
                //*
                emit(TokenType.CLOSE_PARENTHESIS);
                result = getNextState();
                break;
            case END:
                result = State.END;
                break;
            case NOT_EQUALS:
                //*
                emit(TokenType.OPERATOR);
                result = getNextState();
                break;
            case OPERATOR:
                if(currentCharacter == '!'){
                    if(peek() == '=') {
                        advance();
                        return State.NOT_EQUALS;
                    }else {
                        emit(TokenType.OPERATOR);
                    }
                }else if(currentCharacter == '=') {
                    if(getNextState() != State.OPERATOR || currentCharacter != '='){
                        // TODO error
                        throw new RuntimeException("There is no assignment operator in ngAndroid, use == to denote an equality check.");
                    }
                    return State.EQUALS_EQUALS;
                }else {
                    emit(TokenType.OPERATOR);
                }
                result = getNextState();
                break;
            case DIGIT:
                //*
                if(!Character.isDigit(peek())){
                    emit(TokenType.NUMBER_CONSTANT);
                }
                result = getNextState();
                break;
            case EQUALS_EQUALS:
                emit(TokenType.OPERATOR);
                result = getNextState();
                break;
            case STRING:
                advance();
                char character = peek();
                if(character == 0){
                    // TODO error
                    throw new RuntimeException("Unterminated string");
                }
                if(character == '\''){
                    advance();
                    emit(TokenType.STRING);
                    result = getNextState();
                }else{
                   result = State.STRING;
                }
                break;
            default:
                // TODO error
                throw new RuntimeException();
        }
        return result;
    }

    private void advance(){
        index++;
    }

    private State getNextState() {
        if (index >= script.length()) {
            return State.END;
        }

        currentCharacter = script.charAt(index++);

        if(Character.isDigit(currentCharacter)){
            System.out.println(currentCharacter + " is a digit");
            return State.DIGIT;
        }

        if (Character.isLetter(currentCharacter)) {
            return State.CHAR_SEQUENCE;
        }

        switch (currentCharacter) {
            case ',':
                return State.COMMA;
            case '.':
                return State.PERIOD;
            case '(':
                return State.OPEN_PARENTHESIS;
            case ')':
                return State.CLOSE_PARENTHESIS;
            case '?':
                return State.QUESTION_MARK;
            case ':':
                return State.COLON;
            case '\'':
                return State.STRING;
            case '!':
            case '*':
            case '+':
            case '=':
            case '-':
            case '/':
                return State.OPERATOR;
        }
        // TODO throw error
        throw new RuntimeException("Invalid character : " + currentCharacter);
    }

    private char peek(){
       return index < script.length() ? script.charAt(index) : 0;
    }

    private void emit(TokenType tokenType) {
        Token token = new Token(tokenType, script.substring(readIndex, index));
        readIndex = index;
        tokens.add(token);
    }


    // * order is important

}
