package simulation;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

public class Main extends Application {
    private static final String FIELD_RELATIVE_PATH = "/SkystoneField.png";
    private static final String LOG_BACKGROUND_RELATIVE_PATH = "/LogBackground.png";
    private static final String ROBOT_RELATIVE_PATH = "/Robot.png";
    private static final double SCREEN_SIZE_FACTOR = 0.75d;
    private static final double LOG_SIZE_FACTOR = 1 / 2.25d;

    private static final int DEFAULT_HORIZONTAL_SPACING = 100;
    private static final int DEFAULT_VERTICAL_SPACING   = 100;

    private static Semaphore drawSemaphore = new Semaphore(1);
    private static ArrayList<FloatPoint> displayPoints = new ArrayList<>();
    private static ArrayList<Line>       displayLines  = new ArrayList<>();

    private Scene scene;
    private Group rootGroup;
    private Group logGroup;
    private Canvas fieldCanvas;
    private ImageView fieldBackgroundImageView;
    private ImageView logBackgroundImageView;
    private HBox mainHBox;

    @Override
    public void start(Stage primaryStage) throws FileNotFoundException {
        setRootGroup(new Group());
        setLogGroup(new Group());
        setMainHBox(new HBox());

        setScene(new Scene(getRootGroup(), Screen.getScreenWidth(), Screen.getScreenHeight()));
        getMainHBox().prefWidthProperty().bind(primaryStage.widthProperty());
        getMainHBox().prefHeightProperty().bind(primaryStage.heightProperty());

        primaryStage.setTitle("FTC Robotics Simulation 15026");

        Image fieldImage = new Image(new FileInputStream(System.getProperty("user.dir") + getFieldRelativePath()));
        setFieldBackgroundImageView(new ImageView());
        getFieldBackgroundImageView().setImage(fieldImage);
        //getFieldBackgroundImageView().setFitHeight(fieldImage.getHeight() * getScreenSizeFactor());
        //getFieldBackgroundImageView().setFitWidth(fieldImage.getWidth() * getScreenSizeFactor());
        getRootGroup().getChildren().add(getFieldBackgroundImageView());

        setFieldCanvas(new Canvas(Screen.getScreenWidth(), Screen.getScreenHeight()));
        GraphicsContext graphicsContext = getFieldCanvas().getGraphicsContext2D();
        getRootGroup().getChildren().add(getFieldCanvas());

        VBox debuggingHSpacer = new VBox();
        getMainHBox().getChildren().add(debuggingHSpacer);

        getLogGroup().getChildren().add(new HBox());

        Image logImage = new Image(new FileInputStream(System.getProperty("user.dir") + getLogBackgroundRelativePath()));
        setLogBackgroundImageView(new ImageView());
        getLogBackgroundImageView().setImage(logImage);
        getLogBackgroundImageView().setFitHeight(logImage.getHeight() * getLogSizeFactor());
        getLogBackgroundImageView().setFitWidth(logImage.getWidth() * getLogSizeFactor());

        getLogGroup().setTranslateY(10d);

        getLogGroup().getChildren().add(getLogBackgroundImageView());

        Label debuggingLabel = new Label();
        debuggingLabel.setFont(new Font("Courier New", 20d));
        debuggingLabel.textFillProperty().setValue(new Color(0d, 1d, 1d, 1d));
        debuggingLabel.setPrefWidth(getLogBackgroundImageView().getFitWidth() - 25d);
        //debuggingLabel.relocate(16d, getLogBackgroundImageView().getFitHeight() / 4.7d);
        debuggingLabel.setTranslateX(16d);
        debuggingLabel.setTranslateY(getLogBackgroundImageView().getFitHeight() / 4.7d);
        debuggingLabel.setWrapText(true);

        getRootGroup().getChildren().add(getLogBackgroundImageView());

        getLogGroup().getChildren().add(debuggingLabel);
        getMainHBox().getChildren().add(getLogGroup());
        getRootGroup().getChildren().add(getMainHBox());

        getScene().setFill(Color.BLACK);
        primaryStage.setScene(getScene());
        primaryStage.setResizable(false);
        primaryStage.show();

        UdpUnicastClient udpUnicastClient = new UdpUnicastClient(15026);
        Thread           runner           = new Thread(udpUnicastClient);
        runner.start();

        new AnimationTimer() {
            @Override
            public void handle(long nanoseconds) {
                try {
                    getDrawSemaphore().acquire();
                    getFieldCanvas().setWidth(Screen.getFieldSizePixels());
                    getFieldCanvas().setHeight(Screen.getFieldSizePixels());
                    getFieldBackgroundImageView().setFitWidth(Screen.getFieldSizePixels());
                    getFieldBackgroundImageView().setFitHeight(Screen.getFieldSizePixels());
                    debuggingHSpacer.setPrefWidth(16d);
                    debuggingLabel.setMaxWidth(getScene().getWidth() * 0.4d);
                    debuggingLabel.setText("Robot Coordinates:\n" + "X (in): " + MessageProcessing.getRobotX() +
                            ",\nY (in): " + MessageProcessing.getRobotY() + ",\nθ (deg): " + MessageProcessing.getRobotAngle() +
                            "\n\nLinear Extension Height:\nY (in): " + MessageProcessing.getLinearExtensionPosition() + "\nOverextension (in): " +
                            Math.max(0d, (int)(100d * (MessageProcessing.getLinearExtensionPosition() - (MessageProcessing.getLinearStageCount() - 1) * MessageProcessing.getStageLength())) / 100d));
                    drawScreen(graphicsContext);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                getDrawSemaphore().release();
            }
        }.start();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void drawScreen(GraphicsContext graphicsContext) {
        graphicsContext.clearRect(0d, 0d, Screen.getScreenWidth(), Screen.getScreenHeight());
        drawLinearExtension(graphicsContext);
        drawRobot(graphicsContext);
        drawDebugLines(graphicsContext);
        drawDebugPoints(graphicsContext);
    }

    private void drawLinearExtension(GraphicsContext graphicsContext) {
        if(MessageProcessing.getLinearStageCount() > 1) {
            final double lineWidth = 5d;
            final FloatPoint startPosition = new FloatPoint(Screen.getScreenWidth() / 2 - 16d / Screen.getInchesPerPixel(), 0d);
            graphicsContext.setLineWidth(lineWidth);
            graphicsContext.setStroke(new Color(0d, 1d, 1d, 1d));
            if(MessageProcessing.getLinearExtensionType().equals(MessageProcessing.LinearExtensionType.CASCADE)) {
                double stageHeightIncrement = MessageProcessing.getLinearExtensionPosition() / (MessageProcessing.getLinearStageCount() - 1);
                for(int i = 0; i < MessageProcessing.getLinearStageCount(); i++) {
                    graphicsContext.strokeRect(startPosition.x, Screen.getScreenHeight() - startPosition.y - MessageProcessing.getStageLength() / Screen.getInchesPerPixel(),
                            4d / Screen.getInchesPerPixel(), MessageProcessing.getStageLength() / Screen.getInchesPerPixel());
                    startPosition.x += 4d / Screen.getInchesPerPixel();
                    startPosition.y += stageHeightIncrement / Screen.getInchesPerPixel();
                }
            } else {
                int fullyExtendedStages = (int)(MessageProcessing.getLinearExtensionPosition() / MessageProcessing.getStageLength());
                double currentStageDisplacement = MessageProcessing.getLinearExtensionPosition() - fullyExtendedStages * MessageProcessing.getStageLength();
                for(int i = 0; i < MessageProcessing.getLinearStageCount(); i++) {
                    graphicsContext.strokeRect(startPosition.x, Screen.getScreenHeight() - startPosition.y - MessageProcessing.getStageLength() / Screen.getInchesPerPixel(),
                            4d / Screen.getInchesPerPixel(), MessageProcessing.getStageLength() / Screen.getInchesPerPixel());
                    startPosition.x += 4d / Screen.getInchesPerPixel();
                    if(i == MessageProcessing.getLinearStageCount() - fullyExtendedStages - 2) {
                        startPosition.y += currentStageDisplacement / Screen.getInchesPerPixel();
                    } else if(i > MessageProcessing.getLinearStageCount() - fullyExtendedStages - 2) {
                        startPosition.y += MessageProcessing.getStageLength() / Screen.getInchesPerPixel();
                    }
                }
            }
        }
    }

    private void followRobot(double robotX, double robotY) {
        Screen.setCenterPoint(Screen.getInchesPerPixel() * Screen.getScreenWidth() / 2.0,
                Screen.getInchesPerPixel() * Screen.getScreenHeight() / 2.0
        );

        //get where the origin of the field is in pixels
        FloatPoint originInPixels = Screen.convertToScreen(new FloatPoint(0, Screen.getActualFieldSize()));
        getFieldBackgroundImageView().setX(originInPixels.x);
        getFieldBackgroundImageView().setY(originInPixels.y);
    }

    private void drawRobot(GraphicsContext graphicsContext) {
        final double robotRadius = Math.sqrt(2d) * 18d / 2d; //in
        final double robotX      = MessageProcessing.getRobotX();
        final double robotY      = MessageProcessing.getRobotY();
        final double robotAngle  = MessageProcessing.getRobotAngle();

        followRobot(robotX, robotY);

        final double topLeftX = robotX + (robotRadius * (Math.cos(robotAngle + Math.toRadians(45))));
        final double topLeftY = robotY + (robotRadius * (Math.sin(robotAngle + Math.toRadians(45))));

        try {
            FloatPoint bottomLeft = Screen.convertToScreen(new FloatPoint(topLeftX, topLeftY));
            final double width = 18d / Screen.getInchesPerPixel();
            graphicsContext.save();
            graphicsContext.transform(new Affine(new Rotate(Math.toDegrees(-robotAngle) + 90, bottomLeft.x, bottomLeft.y)));
            Image image = new Image(new FileInputStream(System.getProperty("user.dir") + getRobotRelativePath()));
            graphicsContext.drawImage(image, bottomLeft.x, bottomLeft.y, width, width);
            graphicsContext.restore();
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void drawDebugLines(GraphicsContext graphicsContext) {
        final double lineWidth = 3d;
        getDisplayLines().forEach(displayLine -> {
            FloatPoint displayLineInitialPoint = Screen.convertToScreen(
                    new FloatPoint(displayLine.x1, displayLine.y1));
            FloatPoint displayLineTerminationPoint = Screen.convertToScreen(
                    new FloatPoint(displayLine.x2, displayLine.y2));
            graphicsContext.setLineWidth(lineWidth);
            graphicsContext.setStroke(new Color(0d, 1d, 1d, 0.6d));
            graphicsContext.strokeLine(displayLineInitialPoint.x, displayLineInitialPoint.y,
                    displayLineTerminationPoint.x, displayLineTerminationPoint.y);
        });
    }

    private void drawDebugPoints(GraphicsContext graphicsContext) {
        final double pointRadius = 5d;
        final Function<Color, Consumer<FloatPoint>> drawPoint = (color) -> (displayLocation) -> {
            graphicsContext.setStroke(color);
            graphicsContext.strokeOval(
                    displayLocation.x - pointRadius, displayLocation.y - pointRadius,
                    2d * pointRadius, 2d * pointRadius);
        };

        getDisplayPoints().stream()
                .map(Screen::convertToScreen)
                .forEach(drawPoint.apply(new Color(0d, 1d, 1d, 0.6d)));

        if(!MessageProcessing.getPointLog().isEmpty()) {
            AtomicInteger index = new AtomicInteger(0);
            getDisplayPoints().stream()
                    .map(Screen::convertToScreen)
                    .forEach((displayPoint) -> {
                        drawPoint.apply(new Color(1d, 0d + (double) (index.getAndIncrement() / MessageProcessing.getPointLog().size()),
                                0d, 0.9d)).accept(displayPoint);
                    });
        }
    }

    public Group getRootGroup() {
        return rootGroup;
    }

    public void setRootGroup(Group rootGroup) {
        this.rootGroup = rootGroup;
    }

    public static double getScreenSizeFactor() {
        return SCREEN_SIZE_FACTOR;
    }

    public ImageView getFieldBackgroundImageView() {
        return fieldBackgroundImageView;
    }

    public void setFieldBackgroundImageView(ImageView fieldBackgroundImageView) {
        this.fieldBackgroundImageView = fieldBackgroundImageView;
    }

    public Canvas getFieldCanvas() {
        return fieldCanvas;
    }

    public void setFieldCanvas(Canvas fieldCanvas) {
        this.fieldCanvas = fieldCanvas;
    }

    public HBox getMainHBox() {
        return mainHBox;
    }

    public void setMainHBox(HBox mainHBox) {
        this.mainHBox = mainHBox;
    }

    public static String getFieldRelativePath() {
        return FIELD_RELATIVE_PATH;
    }

    public static String getLogBackgroundRelativePath() {
        return LOG_BACKGROUND_RELATIVE_PATH;
    }

    public ImageView getLogBackgroundImageView() {
        return logBackgroundImageView;
    }

    public void setLogBackgroundImageView(ImageView logBackgroundImageView) {
        this.logBackgroundImageView = logBackgroundImageView;
    }

    public static double getLogSizeFactor() {
        return LOG_SIZE_FACTOR;
    }

    public Group getLogGroup() {
        return logGroup;
    }

    public void setLogGroup(Group logGroup) {
        this.logGroup = logGroup;
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public static int getDefaultHorizontalSpacing() {
        return DEFAULT_HORIZONTAL_SPACING;
    }

    public static int getDefaultVerticalSpacing() {
        return DEFAULT_VERTICAL_SPACING;
    }

    public static Semaphore getDrawSemaphore() {
        return drawSemaphore;
    }

    public static void setDrawSemaphore(Semaphore drawSemaphore) {
        Main.drawSemaphore = drawSemaphore;
    }

    public static ArrayList<FloatPoint> getDisplayPoints() {
        return displayPoints;
    }

    public static void setDisplayPoints(ArrayList<FloatPoint> displayPoints) {
        Main.displayPoints = displayPoints;
    }

    public static ArrayList<Line> getDisplayLines() {
        return displayLines;
    }

    public static void setDisplayLines(ArrayList<Line> displayLines) {
        Main.displayLines = displayLines;
    }

    public static String getRobotRelativePath() {
        return ROBOT_RELATIVE_PATH;
    }
}
