/*
 * Decompiled with CFR 0.152.
 */
package appeng.client.gui;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Stack;

public class MathExpressionParser {
    private static final BigDecimal THIRTY = BigDecimal.valueOf(30L);
    private static final BigDecimal ONE_BILLION = BigDecimal.valueOf(1.0E9);

    public static Optional<BigDecimal> parse(String expression, DecimalFormat decimalFormat) {
        ArrayList<Comparable<BigDecimal>> output = new ArrayList<Comparable<BigDecimal>>();
        Stack<Character> operatorStack = new Stack<Character>();
        boolean wasNumberOrRightBracket = false;
        int i = 0;
        while (i < expression.length()) {
            char currentOperator;
            if (Character.isWhitespace(expression.charAt(i))) {
                ++i;
                continue;
            }
            if (!wasNumberOrRightBracket && expression.charAt(i) != '-') {
                ParsePosition position = new ParsePosition(i);
                Number number = decimalFormat.parse(expression, position);
                if (position.getErrorIndex() == -1) {
                    if (!(number instanceof BigDecimal)) {
                        return Optional.empty();
                    }
                    BigDecimal decimal = (BigDecimal)number;
                    output.add(decimal);
                    i = position.getIndex();
                    wasNumberOrRightBracket = true;
                    continue;
                }
            }
            if ((currentOperator = expression.charAt(i)) == '-' && !wasNumberOrRightBracket) {
                currentOperator = 'u';
            }
            wasNumberOrRightBracket = false;
            switch (currentOperator) {
                case '(': 
                case 'u': {
                    operatorStack.push(Character.valueOf(currentOperator));
                    break;
                }
                case ')': {
                    while (true) {
                        if (operatorStack.isEmpty()) {
                            return Optional.empty();
                        }
                        char c = ((Character)operatorStack.pop()).charValue();
                        if (c == '(') break;
                        output.add(Character.valueOf(c));
                    }
                    wasNumberOrRightBracket = true;
                    break;
                }
                case '*': 
                case '+': 
                case '-': 
                case '/': 
                case '^': {
                    char c;
                    while (!operatorStack.isEmpty() && (c = ((Character)operatorStack.peek()).charValue()) != '(' && MathExpressionParser.precedenceCheck(c, currentOperator)) {
                        operatorStack.pop();
                        output.add(Character.valueOf(c));
                    }
                    operatorStack.push(Character.valueOf(currentOperator));
                    break;
                }
                default: {
                    return Optional.empty();
                }
            }
            ++i;
        }
        while (!operatorStack.isEmpty()) {
            output.add((Comparable<BigDecimal>)operatorStack.pop());
        }
        Stack<BigDecimal> number = new Stack<BigDecimal>();
        for (Object e : output) {
            if (e instanceof BigDecimal) {
                BigDecimal bigDecimal = (BigDecimal)e;
                number.push(bigDecimal);
                continue;
            }
            char currentOperator = ((Character)e).charValue();
            if (currentOperator != 'u') {
                if (number.size() < 2) {
                    return Optional.empty();
                }
                BigDecimal right = (BigDecimal)number.pop();
                BigDecimal left = (BigDecimal)number.pop();
                switch (currentOperator) {
                    case '+': {
                        number.push(right.add(left));
                        break;
                    }
                    case '*': {
                        number.push(right.multiply(left));
                        break;
                    }
                    case '-': {
                        number.push(left.subtract(right));
                        break;
                    }
                    case '/': {
                        if (right.compareTo(BigDecimal.ZERO) == 0) {
                            return Optional.empty();
                        }
                        number.push(left.divide(right, 8, RoundingMode.FLOOR));
                        break;
                    }
                    case '^': {
                        right = right.stripTrailingZeros();
                        if (right.scale() > 0 || right.compareTo(BigDecimal.ZERO) < 0) {
                            return Optional.empty();
                        }
                        if (right.compareTo(THIRTY) > 0) {
                            return Optional.empty();
                        }
                        if (left.compareTo(ONE_BILLION) > 0) {
                            return Optional.empty();
                        }
                        number.push(left.pow(right.intValueExact()));
                        break;
                    }
                    case '(': 
                    case ')': {
                        return Optional.empty();
                    }
                    default: {
                        throw new IllegalStateException("Unreachable character : " + currentOperator);
                    }
                }
                continue;
            }
            if (number.isEmpty()) {
                return Optional.empty();
            }
            number.push(((BigDecimal)number.pop()).negate());
        }
        if (number.size() != 1) {
            return Optional.empty();
        }
        return Optional.of(((BigDecimal)number.pop()).stripTrailingZeros());
    }

    private static int getPrecedence(char operator) {
        return switch (operator) {
            case '^' -> -1;
            case 'u' -> 0;
            case '*', '/' -> 1;
            case '+', '-' -> 2;
            default -> throw new IllegalArgumentException("Invalid Operator : " + operator);
        };
    }

    private static boolean precedenceCheck(char first, char second) {
        return MathExpressionParser.getPrecedence(first) <= MathExpressionParser.getPrecedence(second);
    }
}

