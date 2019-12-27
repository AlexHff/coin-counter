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

        // Mean shift
        Mat shifted = new Mat();
        Imgproc.pyrMeanShiftFiltering(src, shifted, 21, 51);

        // Convert to grayscale
        Mat gray = new Mat();
        Imgproc.cvtColor(shifted, gray, Imgproc.COLOR_RGB2GRAY);
        
        // Apply thresholding
        Mat thresh = new Mat();
        Imgproc.threshold(gray, thresh, 0, 255, Imgproc.THRESH_BINARY +
                Imgproc.THRESH_OTSU);

        // Reduce the noise to avoid false circle detection
        Mat blurred = new Mat();
        Imgproc.GaussianBlur(thresh, blurred, new Size(17, 17), 0);

        // Detect circles
        Mat coins = new Mat();
        Imgproc.HoughCircles(blurred, coins, Imgproc.HOUGH_GRADIENT, 1.2, 100);

        // Draw the detected coins
        for (int x = 0; x < coins.cols(); x++) {
            double[] c = coins.get(0, x);
            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
            // Circle center
            Imgproc.circle(src, center, 1,
                    new Scalar(0, 100, 100), 3, 8, 0);
            // Circle outline
            int radius = (int) Math.round(c[2]);
            Imgproc.circle(src, center, radius,
                    new Scalar(255, 0, 255), 3, 8, 0);
        }

        System.out.println(coins.size());

        HighGui.imshow(this.file.getName(), src);
        HighGui.waitKey(0);
    }
}