package Class.Functions.core;

import java.util.function.Predicate;

public class PredicateImpl {
    public static void main(String[] args) {
        Predicate<Integer> predicateImpl = (number) -> number > 0;
        System.out.println(predicateImpl.test(5));
    }
    
}
