package Class.Functions.core;

import java.util.function.BinaryOperator;

public class BinaryOperatorImpl {
    public static void main(String[] args) {
        BinaryOperator<Integer> binaryOperatorImpl = (numberOne, numberTwo) -> numberOne + numberTwo;
        System.out.println(binaryOperatorImpl.apply(5, 10));
    }
}
