package Class.Functions.core;

import java.util.function.Supplier;

public class SupplierImpl {
    public static void main(String[] args) {
        Supplier<String> message = () -> "HEHEHEHEEH";
        System.out.println(message.get());
    }}
