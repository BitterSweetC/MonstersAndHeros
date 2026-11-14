package legends;

import java.io.*;
import java.nio.file.*;

import legends.config.ConfigLoader;
import legends.config.GameConfig;
import legends.controller.MhGameController;

public class Main {
    public static void main(String[] args) {
        try {
            Path data = Paths.get("data");
            long seed = System.nanoTime();
            boolean colorEnabled = true;
            for(int i = 0; i < args.length; i++) {
                if(args[i].equals("--data") && i + 1 < args.length)
                    data = Paths.get(args[++i]);
                else if(args[i].equals("--seed") && i + 1 < args.length)
                    seed = Long.parseLong(args[++i]);
                else if(args[i].equals("--no-color"))
                    colorEnabled = false;
                else
                    throw new IllegalArgumentException("Usage: Main [--data directory] [--seed integer] [--no-color]");
            }
            GameConfig config = new ConfigLoader().load(data);
            new MhGameController(config, seed, colorEnabled).run();
        }
        catch(IOException e) {
            System.err.println("Cannot read game data: " + e.getMessage());
            System.exit(2);
        }
        catch(IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(2);
        }
        catch(UncheckedIOException e) {
            System.err.println("Console I/O failed: " + e.getMessage());
            System.exit(2);
        }
    }
}
