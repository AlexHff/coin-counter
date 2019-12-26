package coincounter;

import java.io.File;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 * CoinCounter
 */
public class CoinCounter {
    private File file;

    public CoinCounter(File file) {
        this.file = file;
    }

    public void detectCoin() {
        Mat src = Imgcodecs.imread(this.file.getAbsolutePath());
        if (src.empty()) {
            System.err.println("[ERROR] Cannot read image: " +
                    this.file.getAbsolutePath());
            System.exit(0);
        }

        // Convert to grayscale
        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_RGB2GRAY);

        // Reduce the noise to avoid false circle detection
        Mat blurred = new Mat();
        //Imgproc.medianBlur(gray, blurred, 5);
        Imgproc.GaussianBlur(gray, blurred, new Size(5, 5), 0);

        // Detect circles
        Mat circles = new Mat();
        Imgproc.HoughCircles(blurred, circles, Imgproc.HOUGH_GRADIENT, 1.2, 100);

        // Draw the detected circles
        for (int x = 0; x < circles.cols(); x++) {
            double[] c = circles.get(0, x);
            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
            // Circle center
            Imgproc.circle(src, center, 1,
                    new Scalar(0, 100, 100), 3, 8, 0);
            // Circle outline
            int radius = (int) Math.round(c[2]);
            Imgproc.circle(src, center, radius,
                    new Scalar(255, 0, 255), 3, 8, 0);
        }

        HighGui.imshow(this.file.getName(), src);
        HighGui.waitKey(0);
    }
}