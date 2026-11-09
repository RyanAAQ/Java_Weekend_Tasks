package Class.Functions.core;

import java.util.function.Function;

public class FunctionImpl {
    public static void main(String[] args) {
        Function<Integer, Integer> functionImpl = (number) -> number * 2;
        System.out.println(functionImpl.apply(5));
    }
}
