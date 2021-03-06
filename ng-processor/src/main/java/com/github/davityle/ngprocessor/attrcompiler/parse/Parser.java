
package com.github.davityle.ngprocessor.attrcompiler.parse;

import com.github.davityle.ngprocessor.attrcompiler.node.BinaryOperator;
import com.github.davityle.ngprocessor.attrcompiler.node.Expression;
import com.github.davityle.ngprocessor.attrcompiler.node.FunctionCall;
import com.github.davityle.ngprocessor.attrcompiler.node.FunctionName;
import com.github.davityle.ngprocessor.attrcompiler.node.Identifier;
import com.github.davityle.ngprocessor.attrcompiler.node.Node;
import com.github.davityle.ngprocessor.attrcompiler.node.NumberConstant;
import com.github.davityle.ngprocessor.attrcompiler.node.ObjectField;
import com.github.davityle.ngprocessor.attrcompiler.node.SpecialIdentifier;
import com.github.davityle.ngprocessor.attrcompiler.node.StringLiteral;
import com.github.davityle.ngprocessor.attrcompiler.node.TernaryOperator;
import com.github.davityle.ngprocessor.attrcompiler.node.UnaryOperator;
import com.github.davityle.ngprocessor.attrcompiler.node.XmlValue;
import com.github.davityle.ngprocessor.util.Option;

import java.util.ArrayList;

public class Parser {
    private ArrayList<Token> tokens;
    private int currentIndex;

    private Parser(String source) throws ParseException {
        Tokenizer tok = new Tokenizer(source);
        tokens = new ArrayList<>(tok.tokenize());
        currentIndex = 0;
    }

    private Token peek(int offset) {
        if (offset + currentIndex < tokens.size()) {
            return tokens.get(offset + currentIndex);
        } else {
            return tokens.get(tokens.size() - 1);
        }
    }

    private Token advance() {
        if (currentIndex < tokens.size()) {
            ++currentIndex;
        }

        return tokens.get(currentIndex - 1);
    }

    private Token require(TokenType type) throws ParseException {
        Token result = advance();
        if (result.getTokenType() == type) {
            return result;
        } else {
            throw new ParseException(result);
        }
    }

    private boolean optional(TokenType type) {
        Token next = peek(0);
        if (next.getTokenType() == type) {
            advance();
            return true;
        }

        return false;
    }

    private Node parse() throws ParseException {
        return parseBinaryOperator();
    }

    private Expression parseBinaryOperator() throws ParseException {
        return parseBinaryOperator(0);
    }

    private Expression parseBinaryOperator(int minPrecedence) throws ParseException {
        Expression lhs = parseUnaryOperator();

        while (peek(0).getTokenType() == TokenType.BINARY_OPERATOR || peek(0).getTokenType() == TokenType.TERNARY_QUESTION_MARK) {
            Token binaryOp = peek(0);
            if (binaryOp.getTokenType() == TokenType.TERNARY_QUESTION_MARK) {
                if (TokenType.TERNARY_PRECEDENCE >= minPrecedence) {
                    require(TokenType.TERNARY_QUESTION_MARK);
                    Expression ifTrue = parseBinaryOperator(TokenType.TERNARY_PRECEDENCE + 1);
                    require(TokenType.TERNARY_COLON);
                    Expression ifFalse = parseBinaryOperator(TokenType.TERNARY_PRECEDENCE + 1);

                    lhs = new TernaryOperator(binaryOp, lhs, ifTrue, ifFalse);
                } else {
                    break;
                }
            } else {
                TokenType.BinaryOperator operatorType = TokenType.BinaryOperator.getOperator(binaryOp.getScript());

                if (operatorType.getPrecedence() >= minPrecedence) {
                    advance();
                    lhs = new BinaryOperator(binaryOp, lhs, parseBinaryOperator(operatorType.getPrecedence() + 1));
                } else {
                    break;
                }
            }
        }

        return lhs;
    }

    private Expression parseUnaryOperator() throws ParseException {
        Token next = peek(0);

        if (TokenType.UnaryOperator.isUnaryOperator(next.getScript())) {
            advance();
            return new UnaryOperator(next, parseUnaryOperator());
        } else {
            return parsePostfixValue();
        }
    }

    private Expression parsePostfixValue() throws ParseException {
        Expression lhs = parseValue();

        while (TokenType.isPostfixOperator(peek(0).getTokenType())) {
            if (peek(0).getTokenType() == TokenType.OPEN_PARENTHESIS) {
                lhs = new FunctionCall(peek(0), lhs, parseParameterList());
            } else if (peek(0).getTokenType() == TokenType.PERIOD) {
                advance();
                if(peek(1).getTokenType() == TokenType.OPEN_PARENTHESIS) {
                    lhs = new FunctionName(lhs, require(TokenType.IDENTIFIER));
                } else {
                    lhs = new ObjectField(lhs, require(TokenType.IDENTIFIER));
                }
            } else {
                throw new ParseException(peek(0));
            }
        }

        return lhs;
    }

    private ArrayList<? extends Expression> parseParameterList() throws ParseException {
        ArrayList<Expression> result = new ArrayList<>();

        require(TokenType.OPEN_PARENTHESIS);

        while (!optional(TokenType.CLOSE_PARENTHESIS)) {
            result.add(parseBinaryOperator());

            if (!optional(TokenType.COMMA) && peek(0).getTokenType() != TokenType.CLOSE_PARENTHESIS) {
                throw new ParseException(peek(0));
            }
        }

        return result;
    }

    private Expression parseValue() throws ParseException {
        Token next = peek(0);

        if (next.getTokenType() == TokenType.OPEN_PARENTHESIS) {
            return parseParenthesis();
        } else if (TokenType.isNumberConstant(next.getTokenType())) {
            return new NumberConstant(advance());
        } else if (next.getTokenType() == TokenType.SPECIAL_IDENTIFIER) {
            return SpecialIdentifier.getSpecialIdentifier(advance());
        } else if (next.getTokenType() == TokenType.XML_VALUE) {
            return XmlValue.getXmlValue(advance(), require(TokenType.XML_VALUE_KEY));
        }   else if (next.getTokenType() == TokenType.IDENTIFIER) {
            return new Identifier(advance());
        } else if (next.getTokenType() == TokenType.STRING) {
            return new StringLiteral(advance());
        }

        throw new ParseException(next);
    }

    private Expression parseParenthesis() throws ParseException {
        require(TokenType.OPEN_PARENTHESIS);
        Expression result = parseBinaryOperator();
        require(TokenType.CLOSE_PARENTHESIS);
        return result;
    }

    public static Node parse(String source) throws ParseException {
        Parser parser = new Parser(source);
        return parser.parse();
    }

    public static Option<Node> tryParse(String source) {
        try {
            return Option.of(parse(source));
        } catch (ParseException e) {
            return Option.absent();
        }
    }
}