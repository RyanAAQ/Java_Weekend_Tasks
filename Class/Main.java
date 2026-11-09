package Class;

import java.util.stream.IntStream;

public class Main {
    static void main() {
            int sumOfFirstTenNumbers = sumFirstTenRealNumbers();
    if(sumOfFirstTenNumbers != 55){
        throw new RuntimeException("ERROR: invalid sum  recieved, answer is not 55");
    }
    System.out.println("Test Passed");
    System.out.println(multiplyFirstTenRealNumbers() );

}

public static int sumFirstTenRealNumbers(){
    return IntStream.rangeClosed(1, 10).sum();
}

public static int multiplyFirstTenRealNumbers(){
        return IntStream.rangeClosed(1, 10).reduce(1, (x,y) -> x*y);
}
}
