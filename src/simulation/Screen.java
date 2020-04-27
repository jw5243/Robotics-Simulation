package simulation;


/**
 * This class keeps track of where the robot is
 * and provides the methods to transform real coordinates into screen coordinates
 */
public class Screen {
    //the real width of the field
    private static final double ACTUAL_FIELD_SIZE = 12d * 12d; //in
    //These are the SCREEN dimensions
    private static double screenWidth = 1130d * Main.getScreenSizeFactor();
    private static double screenHeight = 1130d * Main.getScreenSizeFactor();
    //These are the REAL coordinates on the field that will be the center of the screen
    private static double centerXReal = 0;
    private static double centerYReal = 0;
    //Increasing this makes the width and height in real scale smaller since it is zooming in
    private static double zoomPercent = 1.0;

    /**
     * Sets the center point of the screen in real coordinates
     * This is used to follow the robot
     */
    public static void setCenterPoint(double centerX, double centerY) {
        setCenterXReal(centerX);
        setCenterYReal(centerY);
    }

    /**
     * This sets our width and height in pixels.
     * We need to know this to transform real coordinates into screen coordinates.
     * @param width width in pixels
     * @param height height in pixels
     */
    public static void setDimensionsPixels(double width, double height) {
        setScreenWidth(width);
        setScreenHeight(height);
    }
    
    /**
     * Converts a real coordinate to a screen coordinate, relative to the top left of the window
     * @param p real coordinates
     * @return screen coordinates
     */
    public static FloatPoint convertToScreen(FloatPoint p) {
        //get where the top left of the screen is in real coordinates
        FloatPoint topLeft = getTopLeftScreenRealPosition();
        //now get where the point is in respect to the top left.
        //HOWEVER the top left is above us, we don't want y to be negative so swap the y
        FloatPoint relativeFromTopLeft = new FloatPoint(p.x - topLeft.x, topLeft.y - p.y, p.radius);
        
        //now we can get the percent that we are across the screen
        double percentX = relativeFromTopLeft.x / getWindowSizeInRealScale().x;
        double percentY = relativeFromTopLeft.y / getWindowSizeInRealScale().y;
        
        //now that we have percents, multiply by the width and height to get pixel coordinates
        return new FloatPoint(percentX * getScreenWidth(), percentY * getScreenHeight(), p.radius);
    }
    
    /**
     * This is only used by us to find the theoretical field location of the top left of the window
     * @return real coordinates of where the top left of the window is
     */
    private static FloatPoint getTopLeftScreenRealPosition() {
        //first get the window size in real dimensions
        FloatPoint windowSizeReal = getWindowSizeInRealScale();
        // the top left point of the screen is just the center of the screen translated up
        return new FloatPoint(getCenterXReal() - windowSizeReal.x / 2d, getCenterYReal() + windowSizeReal.y / 2d);
    }
    
    /**
     * Gets the screen size in real scale. This needs to consider zoom
     * @return FloatPoint of the window's size in real coordinates
     */
    private static FloatPoint getWindowSizeInRealScale() {
        //now we can return size in real scale by multiplying the screen sizes by the screen pixel sizes
        return new FloatPoint(getScreenWidth() * getInchesPerPixel(), getScreenHeight() * getInchesPerPixel());
    }
    
    /**
     * Set the zoom of teh screen with this
     * @param zoom the zoom where 1.0 means the screen is one field size big, 2.0 means it is half
     */
    private static void setZoomPercent(double zoom) {
        setZoomPercent(zoom);
    }

    /**
     * Gets how many inches are in each pixel of the screen
     *
     * @return
     */
    public static double getInchesPerPixel() {
        return getActualFieldSize() / getFieldSizePixels();
    }

    /**
     * Gets the field size in pixels
     * @return the field size in pixels
     */
    public static double getFieldSizePixels() {
        //get the biggest dimension if it is the width or the height
        double biggestWindowDimensionPixels = Math.max(getScreenHeight(), getScreenWidth());
        return biggestWindowDimensionPixels / getZoomPercent();
    }

    public static double getActualFieldSize() {
        return ACTUAL_FIELD_SIZE;
    }

    public static double getScreenWidth() {
        return screenWidth;
    }

    public static void setScreenWidth(double screenWidth) {
        Screen.screenWidth = screenWidth;
    }

    public static double getScreenHeight() {
        return screenHeight;
    }

    public static void setScreenHeight(double screenHeight) {
        Screen.screenHeight = screenHeight;
    }

    public static double getCenterXReal() {
        return centerXReal;
    }

    public static void setCenterXReal(double centerXReal) {
        Screen.centerXReal = centerXReal;
    }

    public static double getCenterYReal() {
        return centerYReal;
    }

    public static void setCenterYReal(double centerYReal) {
        Screen.centerYReal = centerYReal;
    }

    public static double getZoomPercent() {
        return zoomPercent;
    }
}
