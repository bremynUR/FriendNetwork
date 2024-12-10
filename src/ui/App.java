package ui;

import java.util.ArrayList;

public class App {
    public static void main(String[] args) throws Exception {
        Network network = new Network(new ArrayList<>());
        network.generateNetwork();
        network.iterateNetwork();
    }
}
