package Class.Functions.core;

import java.util.function.BiFunction;

public class BiFunctionImpl {
    public static void main(String[] args) {
        BiFunction<Integer, Integer, Integer> result = (numberOne, numberTwo) -> numberOne + numberTwo;
        System.out.println(result.apply(5, 10));
    }
}
