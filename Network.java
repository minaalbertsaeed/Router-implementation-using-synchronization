import java.util.*;
import java.io.*;

class Router {

    public int maxNumber, ConnectedDevices;
    public boolean[] connected;
    public Semaphore sema;

    public Router(int maxConnections) {
        sema = new Semaphore(maxConnections);
        connected = new boolean[maxConnections];
        sema = new Semaphore(maxConnections);
    }

    public synchronized int occupyConnection(Device device) throws InterruptedException {
        int cnt = 0;
        while (cnt < maxNumber) {
            if (connected[cnt] == false) {
                ConnectedDevices++;
                device.connectionID = cnt + 1;
                connected[cnt] = true;
                Thread.sleep(100);
                break;
            }
            cnt++;
        }
        return device.connectionID;
    }

    public synchronized void releaseConnection(Device device) {
        ConnectedDevices--;
        connected[device.connectionID - 1] = false;
        notify();
        device.logout();

    }

}

class Semaphore {
    int value;

    public Semaphore(int initial) {
        this.value = initial;
    }

    public synchronized void wait(Device device) throws InterruptedException {
        value--;
        if (value < 0) {
            System.out.println(device.name + " (" + device.type + ")" + " arrived and waiting");
            wait();

        } else {
            System.out.println(device.name + " (" + device.type + ")" + " arrived");
        }

        device.router.occupyConnection(device);
    }

    public synchronized void signal() {
        value++;
        if (value <= 0) {
            notify();
        }
    }
}

class Device extends Thread {
    public String name;
    public String type;
    public Router router;
    public int connectionID;

    public Device(String name, String type, Router router) {
        this.name = name;
        this.type = type;
        this.router = router;
        connectionID = 1;
    }

    @Override
    public void run() {
        try {
            router.sema.wait(this);
            connectionID = router.occupyConnection(this);
            System.out.println("Connection " + connectionID + ": " + name + " Occupied");
            activity();
            router.releaseConnection(this);
            router.sema.signal();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void activity() throws InterruptedException {
        login();
        System.out.println("Connection " + connectionID + ": " + name + " Performs online activity");
        Thread.sleep(2000);
    }

    public void login() {
        System.out.println("Connection " + connectionID + ": " + name + " login");
    }

    public void logout() {
        System.out.println("Connection " + connectionID + ": " + name + " Logged out");
    }
}

public class Network {
    public static void main(String[] args) throws FileNotFoundException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter the max number of connections a router can accept (N):");
        int maxConnections = scanner.nextInt();

        System.out.println("Enter the total number of devices that wish to connect (TC):");
        int totalDevices = scanner.nextInt();

        Router router = new Router(maxConnections);

        ArrayList<Device> devices = new ArrayList<>();
        for (int i = 0; i < totalDevices; i++) {
            System.out.println("Enter the name and type of device " + (i + 1) + ":");
            String name = scanner.next();
            String type = scanner.next();

            Device device = new Device(name, type, router);
            devices.add(device);
        }

        // redirect the print statments to output.txt

        FileOutputStream fos = new FileOutputStream("output.txt");
        PrintStream ps = new PrintStream(fos);
        System.setOut(ps);

        for (int i = 0; i < devices.size(); i++) {
            devices.get(i).start();
        }
        scanner.close();

    }
}
