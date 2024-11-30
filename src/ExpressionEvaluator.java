import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionEvaluator {
    private static final Map<String, Integer> WORD_TO_NUMBER = new HashMap<>() {{
        put("zero", 0);
        put("one", 1);
        put("two", 2);
        put("three", 3);
        put("four", 4);
        put("five", 5);
        put("six", 6);
        put("seven", 7);
        put("eight", 8);
        put("nine", 9);
        put("ten", 10);
        put("eleven", 11);
        put("twelve", 12);
        put("thirteen", 13);
        put("fourteen", 14);
        put("fifteen", 15);// Added "fifteen" (15) to the word-to-number map
        put("sixteen", 16);
        put("seventeen", 17);
        put("eighteen", 18);
        put("nineteen", 19);
        put("twenty", 20);
    }};

    public static double evaluateExpression(String expression) {
        try {

            // Remove non-numeric characters and convert word numbers
            String cleanedExpression = normalizeExpression(expression);
            // Tokenize the expression
            List<String> tokens = tokenize(cleanedExpression);
            // Validate numbers are within 0-20 range
            if (!validateNumbers(tokens)) {
                throw new IllegalArgumentException("Numbers must be between 0 and 20");
            }
            // Transform infix expression to postfix notation, respecting operator precedence and parentheses
            Queue<String> postfix = convertToPostfix(tokens);
            // Calculate the result by processing postfix tokens and performing arithmetic operations
            return evaluatePostfix(postfix);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid expression: " + e.getMessage());
        }
    }


    // Convert words to numbers, standardize operators
    private static String normalizeExpression(String expression) {
        String normalized = expression.toLowerCase()
                .replaceAll("multiplied by", "*")
                .replaceAll("divided by", "/")
                .replaceAll("plus", "+")
                .replaceAll("minus", "-")
                .replaceAll("[^a-z0-9+\\-*/()\\s]", "")
                .trim();

        // Replace word numbers
        for (Map.Entry<String, Integer> entry : WORD_TO_NUMBER.entrySet()) {
            normalized = normalized.replaceAll("\\b" + entry.getKey() + "\\b",
                    String.valueOf(entry.getValue()));// Fixed number normalization: no need to add 1 in the conversion
        }

        return normalized;
    }

    private static boolean validateNumbers(List<String> tokens) {
        for (String token : tokens) {
            if (token.matches("\\d+")) {
                int number = Integer.parseInt(token);
                if (number < 0 || number > 20) {// Added "twenty" (20) to the word-to-number map
                    return false;
                }
            }
        }
        return true;
    }


    // Tokenize the expression into individual components
    private static List<String> tokenize(String expression) {
        List<String> tokens = new ArrayList<>();
        Pattern tokenPattern = Pattern.compile("\\d+|[+\\-*/()]|[a-z]+"); // Adjusted regex to correctly split both digits and number words
        Matcher matcher = tokenPattern.matcher(expression);

        while (matcher.find()) {
            tokens.add(matcher.group());
        }
        return tokens;
    }

    // Convert mathematical expression from infix to postfix notation, managing operator precedence and parentheses
    private static Queue<String> convertToPostfix(List<String> tokens) {
        Queue<String> output = new LinkedList<>();
        Deque<String> operators = new ArrayDeque<>();// Replaced Stack with Deque

        for (String token : tokens) {
            if (token.matches("\\d+")) {
                output.offer(token);
            } else if (token.equals("(")) {
                operators.push(token);
            } else if (token.equals(")")) {
                while (!operators.isEmpty() && !operators.peek().equals("(")) {
                    output.offer(operators.pop());
                }
                if (!operators.isEmpty() && operators.peek().equals("(")) {
                    operators.pop();
                } else {
                    throw new IllegalArgumentException("Mismatched parentheses");
                }
            } else if (isOperator(token)) {
                while (!operators.isEmpty()
                        && isOperator(operators.peek())
                        && precedence(token) <= precedence(operators.peek())) { // Process operators with equal precedence as well
                    output.offer(operators.pop());
                }
                operators.push(token);
            }
        }

        while (!operators.isEmpty()) {
            if (operators.peek().equals("(")) {
                throw new IllegalArgumentException("Mismatched parentheses");
            }
            output.offer(operators.pop());
        }

        return output;
    }


    // Calculate result by processing postfix tokens using a deque-based evaluation algorithm
    private static double evaluatePostfix(Queue<String> postfix) {
        Deque<Double> evaluationDeque = new ArrayDeque<>();// Replaced Stack with Deque

        while (!postfix.isEmpty()) {
            String token = postfix.poll();

            if (token.matches("\\d+")) {
                evaluationDeque.push(Double.parseDouble(token));
            } else if (isOperator(token)) {
                if (evaluationDeque.size() < 2) {
                    throw new IllegalArgumentException("Invalid expression");
                }
                double b = evaluationDeque.pop();
                double a = evaluationDeque.pop();
                evaluationDeque.push(performOperation(a, b, token));
            }
        }

        if (evaluationDeque.size() != 1) {
            throw new IllegalArgumentException("Invalid expression");
        }

        return evaluationDeque.pop();
    }

    // Check if a token is an operator
    private static boolean isOperator(String token) {
        return token.matches("[+\\-*/]");
    }


    // Operator precedence
    private static int precedence(String operator) {
        switch (operator) {
            case "+":
            case "-":
                return 1;
            case "*":
            case "/":
                return 2;
            default:
                return 0;
        }
    }


    // Perform arithmetic operation
    private static double performOperation(double a, double b, String operator) {
        switch (operator) {
            case "+":
                return a + b;
            case "-":
                return a - b;
            case "*":
                return a * b;
            case "/":
                if (b == 0) {
                    throw new IllegalArgumentException("Division by zero");
                }//Checked division by zero
                return a / b;
            default:
                throw new IllegalArgumentException("Unknown operator: " + operator);
        }
    }

    public static void main(String[] args) {
        String[] testExpressions = {
                "19+19",
                "(1 + (2 + (3 + (4 + (5 + (6 + (7 + (8 + (9 + (10))))))))))",
                "six plus three",
                "Can you tell me 2 + 3?",
                "5 multiplied by 10 multiplied by 2",
                "12 plus (10 divided by 2)",
                "3 divided by (2 plus 1)",


        };

        for (String expr : testExpressions) {
            try {
                double result = evaluateExpression(expr);
                System.out.println("Expression: " + expr);
                System.out.println("Result: " + result);
                System.out.println("---");
            } catch (Exception e) {
                System.out.println("Error in expression '" + expr + "': " + e.getMessage());
                System.out.println("---");
            }
        }
    }
}