package lambdas.builtin;

/** java.lang.Runnable - takes nothing, returns nothing. Single abstract method: run(). The oldest "functional interface" in the JDK. */
public class Runnable {

    public static void main(String[] args) {
        java.lang.Runnable sayHello = () -> System.out.println("running...");
        sayHello.run();

        // Runnable predates java.util.function - this is exactly why threads have always accepted a lambda here.
        Thread thread = new Thread(() -> System.out.println("running on: " + Thread.currentThread().getName()));
        thread.start();
    }
}
