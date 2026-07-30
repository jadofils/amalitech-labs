package multithreading.coordination;

/**
 * Semaphore(n) - a counter of n available "permits". acquire() takes one (blocking if none are
 * left), release() gives one back. Classic use: capping how many threads can use a limited
 * resource (a connection pool, a fixed number of parking spots) at the same time.
 */
public class Semaphore {

    public static void main(String[] args) throws InterruptedException {
        int parkingSpots = 2;
        java.util.concurrent.Semaphore parking = new java.util.concurrent.Semaphore(parkingSpots);

        Thread[] cars = new Thread[5];
        for (int i = 1; i <= cars.length; i++) {
            int carId = i;
            cars[i - 1] = new Thread(() -> {
                try {
                    System.out.println("car " + carId + " waiting for a spot (available: " + parking.availablePermits() + ")");
                    parking.acquire(); // blocks here if both spots are already taken
                    System.out.println("car " + carId + " parked");
                    Thread.sleep(200); // occupying the spot for a while
                    System.out.println("car " + carId + " leaving");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    parking.release(); // ALWAYS release, even on exception, or the spot is lost forever
                }
            });
            cars[i - 1].start();
        }

        for (Thread car : cars) {
            car.join();
        }
        System.out.println("all cars done, spots free again: " + parking.availablePermits());
    }
}
