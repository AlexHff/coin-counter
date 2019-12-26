package coincounter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
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
        
        // Apply mean shifting
        Mat shifted = new Mat();
        Imgproc.pyrMeanShiftFiltering(src, shifted, 21, 51);

        // Convert to grayscale
        Mat gray = new Mat();
        Imgproc.cvtColor(shifted, gray, Imgproc.COLOR_BGR2GRAY);
        
        // Apply thresholding
        Mat thresh = new Mat();
        Imgproc.threshold(gray, thresh, 0, 255, Imgproc.THRESH_BINARY +
                Imgproc.THRESH_OTSU);

        // Create a kernel
        Mat kernel = new Mat(3, 3, CvType.CV_32F);
        float[] kernelData = new float[(int) (kernel.total() * kernel.channels())];
        kernelData[0] = 1;
        kernelData[1] = 1;
        kernelData[2] = 1;
        kernelData[3] = 1;
        kernelData[4] = -8;
        kernelData[5] = 1;
        kernelData[6] = 1;
        kernelData[7] = 1;
        kernelData[8] = 1;
        kernel.put(0, 0, kernelData);

        // Remove noise
        Mat opening = new Mat();
        Imgproc.morphologyEx(thresh, opening, Imgproc.MORPH_OPEN, kernel);

        // Get background
        Mat bg = new Mat();
        Imgproc.dilate(opening, bg, kernel);
        bg.convertTo(bg, CvType.CV_32F);

        // Perform the distance transform algorithm
        Mat dist = new Mat();
        Imgproc.distanceTransform(opening, dist, Imgproc.DIST_L2, 5);

        // Get foreground
        Mat fg = new Mat();
        MinMaxLocResult mmr = Core.minMaxLoc(dist);
        Imgproc.threshold(dist, fg, 0.7 * mmr.maxVal, 255, 0);
        fg.convertTo(fg, CvType.CV_32F);

        Mat imgResult = new Mat();
        Core.subtract(bg, fg, imgResult);

        // Convert back to 8bits gray scale
        imgResult.convertTo(imgResult, CvType.CV_8UC3);

        /*
        Mat markers = new Mat();
        fg.convertTo(fg, CvType.CV_8UC1);
        Imgproc.connectedComponents(fg, markers);

        int[] markersData = new int[(int) markers.total() * markers.channels()];
        for (int i = 0; i < markers.rows(); i++) {
            for (int j = 0; j < markers.cols(); j++) {
                markersData[i * markers.cols() + j]++;
            }
        }
        markers.put(0, 0, markersData);

        System.out.println(imgResult);
        System.out.println(markers);
        Imgproc.watershed(imgResult, markers);

        // Perform the distance transform algorithm
        Imgproc.distanceTransform(imgResult, dist, Imgproc.DIST_L2, 3);

        // Normalize the distance image for range = {0.0, 1.0}
        // so we can visualize and threshold it
        Core.normalize(dist, dist, 0.0, 1.0, Core.NORM_MINMAX);
        Mat distDisplayScaled = new Mat();
        Core.multiply(dist, new Scalar(255), distDisplayScaled);
        Mat distDisplay = new Mat();
        distDisplayScaled.convertTo(distDisplay, CvType.CV_8U);

        // Threshold to obtain the peaks
        // This will be the markers for the foreground objects
        Imgproc.threshold(dist, dist, 0.4, 1.0, Imgproc.THRESH_BINARY);

        // Dilate a bit the dist image
        Mat kernel1 = Mat.ones(3, 3, CvType.CV_8U);
        Imgproc.dilate(dist, dist, kernel1);
        Mat distDisplay2 = new Mat();
        dist.convertTo(distDisplay2, CvType.CV_8U);
        Core.multiply(distDisplay2, new Scalar(255), distDisplay2);

        */

        // Create the CV_8U version of the distance image
        Mat dist_8u = new Mat();
        dist.convertTo(dist_8u, CvType.CV_8U);

        // Find total markers
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(dist_8u, contours, hierarchy, Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_SIMPLE);

        // Create the marker image for the watershed algorithm
        Mat markers = Mat.zeros(dist.size(), CvType.CV_32S);

        // Draw the foreground markers
        for (int i = 0; i < contours.size(); i++) {
            Imgproc.drawContours(markers, contours, i, new Scalar(i + 1), -1);
        }

        // Draw the background marker
        Mat markersScaled = new Mat();
        markers.convertTo(markersScaled, CvType.CV_32F);
        Core.normalize(markersScaled, markersScaled, 0.0, 255.0, Core.NORM_MINMAX);
        Imgproc.circle(markersScaled, new Point(5, 5), 3, new Scalar(255, 255, 255), -1);

        Mat markersDisplay = new Mat();
        markersScaled.convertTo(markersDisplay, CvType.CV_8U);

        Imgproc.circle(markers, new Point(5, 5), 3, new Scalar(255, 255, 255), -1);

        // Perform the watershed algorithm
        System.out.println(imgResult);
        System.out.println(markers);
        Imgproc.watershed(imgResult, markers);
        Mat mark = Mat.zeros(markers.size(), CvType.CV_8U);
        markers.convertTo(mark, CvType.CV_8UC1);
        Core.bitwise_not(mark, mark);

        // Generate random colors
        Random rng = new Random(12345);
        List<Scalar> colors = new ArrayList<>(contours.size());
        for (int i = 0; i < contours.size(); i++) {
            int b = rng.nextInt(256);
            int g = rng.nextInt(256);
            int r = rng.nextInt(256);
            colors.add(new Scalar(b, g, r));
        }

        // Create the result image
        Mat dst = Mat.zeros(markers.size(), CvType.CV_8UC3);
        byte[] dstData = new byte[(int) (dst.total() * dst.channels())];
        dst.get(0, 0, dstData);

        // Fill labeled objects with random colors
        int[] markersData = new int[(int) (markers.total() * markers.channels())];
        markers.get(0, 0, markersData);
        for (int i = 0; i < markers.rows(); i++) {
            for (int j = 0; j < markers.cols(); j++) {
                int index = markersData[i * markers.cols() + j];
                if (index > 0 && index <= contours.size()) {
                    dstData[(i * dst.cols() + j) * 3 + 0] = (byte) colors.get(
                            index - 1).val[0];
                    dstData[(i * dst.cols() + j) * 3 + 1] = (byte) colors.get(
                            index - 1).val[1];
                    dstData[(i * dst.cols() + j) * 3 + 2] = (byte) colors.get(
                            index - 1).val[2];
                } else {
                    dstData[(i * dst.cols() + j) * 3 + 0] = 0;
                    dstData[(i * dst.cols() + j) * 3 + 1] = 0;
                    dstData[(i * dst.cols() + j) * 3 + 2] = 0;
                }
            }
        }
        dst.put(0, 0, dstData);

        /*
        // Convert to grayscale
        Mat gray = new Mat();
        Imgproc.cvtColor(img, gray, Imgproc.COLOR_RGB2GRAY);
        
        // Apply thresholding
        Mat thresh = new Mat();
        Imgproc.threshold(gray, thresh, 0, 255, Imgproc.THRESH_BINARY +
                Imgproc.THRESH_OTSU);
        HighGui.imshow(this.file.getName(), thresh);
        HighGui.waitKey(0);

        // Reduce the noise to avoid false circle detection
        Mat blurred = new Mat();
        Imgproc.GaussianBlur(thresh, blurred, new Size(17, 17), 0);
        HighGui.imshow(this.file.getName(), blurred);
        HighGui.waitKey(0);

        // Detect circles
        Mat circles = new Mat();
        Imgproc.HoughCircles(thresh, circles, Imgproc.HOUGH_GRADIENT, 1.2, 100);

        // Draw the detected circles
        for (int x = 0; x < circles.cols(); x++) {
            double[] c = circles.get(0, x);
            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
            // Circle center
            Imgproc.circle(img, center, 1,
                    new Scalar(0, 100, 100), 3, 8, 0);
            // Circle outline
            int radius = (int) Math.round(c[2]);
            Imgproc.circle(img, center, radius,
                    new Scalar(255, 0, 255), 3, 8, 0);
        }

        HighGui.imshow(this.file.getName(), img);
        HighGui.waitKey(0);
        */
    }
}