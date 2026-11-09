package Class.Functions.core;

import java.util.function.UnaryOperator;

public class UnaryOperatorImpl {
    public static void main(String[] args) {
        UnaryOperator<Integer> unaryOperatorImpl = (number) -> number * 2;
        System.out.println(unaryOperatorImpl.apply(5));
    }
}
