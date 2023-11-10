import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class Semaphore {
    private int permits;

    public Semaphore(int permits) {
        this.permits = permits;
    }

    public synchronized void acquire() throws InterruptedException {
        while (permits <= 0) {
            wait();
        }
        permits--;
    }

    public synchronized void release() {
        permits++;
        notify();
    }
}

class Router {
    private List<String> connections = new ArrayList<>();
    private Semaphore semaphore;

    public Router(int maxConnections) {
        semaphore = new Semaphore(maxConnections);
    }

    public void occupyConnection(String deviceName) throws InterruptedException {
        semaphore.acquire();
        synchronized (this) {
            connections.add(deviceName);
            System.out.println(deviceName + " connected to the router.");
        }
    }

    public void releaseConnection(String deviceName) {
        synchronized (this) {
            connections.remove(deviceName);
            System.out.println(deviceName + " disconnected from the router.");
        }
        semaphore.release();
    }
}

class Device extends Thread {
    private String name;
    private String type;
    private Router router;

    public Device(String name, String type, Router router) {
        this.name = name;
        this.type = type;
        this.router = router;
    }

    public void connect() throws InterruptedException {
        router.occupyConnection(name);
    }

    public void disconnect() {
        router.releaseConnection(name);
    }

    public void performOnlineActivity() {
        System.out.println(name + " is performing online activity.");
    }

    @Override
    public void run() {
        try {
            connect();
            performOnlineActivity();
            disconnect();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

public class Network {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter the max number of connections a router can accept (N):");
        int maxConnections = scanner.nextInt();

        System.out.println("Enter the total number of devices that wish to connect (TC):");
        int totalDevices = scanner.nextInt();

        Router router = new Router(maxConnections);

        for (int i = 0; i < totalDevices; i++) {
            System.out.println("Enter the name and type of device " + (i + 1) + ":");
            String name = scanner.next();
            String type = scanner.next();

            Device device = new Device(name, type, router);
            device.start();
        }

        scanner.close();
    }
}

