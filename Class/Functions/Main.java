package Class.Functions;

public class Main {
    static void main() {
        Flyable jet = (distance) -> {
            System.out.println("I am a jet that flies at " + distance + "mph");
        };
        jet.fly(117);

        Flyable bird = (distance) ->
                System.out.println("I am a bird that flies at "+distance+" mph");
                bird.fly(11);
    }
}