package coincounter;

import java.io.File;

import org.opencv.core.Core;

/**
 * App
 */
public final class App {
    private App() {

    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Too few arguments.");
            System.exit(0);
        } else if (args.length > 1) {
            System.err.println("Too many arguments.");
            System.exit(0);
        }
        nu.pattern.OpenCV.loadShared();
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        File folder = new File(args[0]);
        for (File file : folder.listFiles()) {
            if (file.isFile()) {
                CoinCounter instance = new CoinCounter(file);
                instance.detectCoin();
            }
        }
        System.out.println("[DONE]");
        System.exit(0);
    }
}
