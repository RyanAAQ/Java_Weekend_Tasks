package Class.Functions.core;

import java.util.function.BiPredicate;

public class BiPredicateImpl {
    public static void main(String[] args) {
        BiPredicate<Integer, Integer> biPredicateImpl = (numberOne, numberTwo) -> numberOne > numberTwo;
        System.out.println(biPredicateImpl.test(5, 10));
    }
}
