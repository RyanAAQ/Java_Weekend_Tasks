package Class.Functions.core;

import java.util.function.BiConsumer;

public class BiConsumerImpl {
    public static void main(String[] args) {
        BiConsumer<Integer, Integer> biConsumerImpl = (a, b) -> {};
        biConsumerImpl.accept(5, 10);
    }
    
}
