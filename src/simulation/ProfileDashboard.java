package simulation;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class ProfileDashboard extends Application {
    private static boolean hasInitialized = false;
    private static Stage                     primaryStage;
    private static VBox                      primaryContainer;
    private static LineChart<Number, Number> profile;

    public static void addProfilePoint(final ProfileType profileType, final Direction direction, final double timeStamp,
                                       final double profileValue) {
        new Task<Void>() {
            /**
             * Invoked when the Task is executed, the call method must be overridden and
             * implemented by subclasses. The call method actually performs the
             * background thread logic. Only the updateProgress, updateMessage, updateValue and
             * updateTitle methods of Task may be called from code within this method.
             * Any other interaction with the Task from the background thread will result
             * in runtime exceptions.
             *
             * @return The result of the background work, if any.
             */
            @Override
            protected Void call() {
                final ObservableList<XYChart.Series<Number, Number>> profiles = getProfile().getData();
                //if(profiles.size() >= ProfileType.values().length + Arrays.stream(Direction.values()).max(
                //    Comparator.comparingInt(Direction::getValue)).get().getValue()) {
                    Platform.runLater(() -> profiles.get(profileType.getValue() + direction.getValue()).getData()
                                                    .add(new XYChart.Data<>(timeStamp, profileValue)));
                //}
                
                return null;
            }
        }.run();
        
        /*final double clampedTimeStamp = timeStamp < 0d ? 0d : timeStamp;
        final double clampedProfileValue = profileValue < 0d ? 0d : profileValue;
        final ObservableList<XYChart.Series<Number, Number>> profiles = getProfile().getData();
        
        if(profiles.size() >= ProfileType.values().length + Arrays.stream(Direction.values()).max(Comparator
        .comparingInt(Direction::getValue)).get().getValue()) {
            profiles.get(profileType.getValue() + direction.getValue()).getData().add(new XYChart.Data<>
            (clampedTimeStamp, clampedProfileValue));
        }*/
    }
    
    public static void resetProfile() {
        new Task<Void>() {
            /**
             * Invoked when the Task is executed, the call method must be overridden and
             * implemented by subclasses. The call method actually performs the
             * background thread logic. Only the updateProgress, updateMessage, updateValue and
             * updateTitle methods of Task may be called from code within this method.
             * Any other interaction with the Task from the background thread will result
             * in runtime exceptions.
             *
             * @return The result of the background work, if any.
             * @throws Exception an unhandled exception which occurred during the
             *         background operation
             */
            @Override
            protected Void call() throws Exception {
                Platform.runLater(() -> {
                    getProfile().setData(FXCollections.observableArrayList());
                    //getProfile().getData().add(new XYChart.Series<>("X-Position", FXCollections.observableList(new ArrayList<>())));
                    //getProfile().getData().add(new XYChart.Series<>("X-Velocity", FXCollections.observableList(new ArrayList<>())));
                    //getProfile().getData()
                    //            .add(new XYChart.Series<>("X-Acceleration", FXCollections.observableList(new ArrayList<>())));
                    //getProfile().getData().add(new XYChart.Series<>("X-Jerk", FXCollections.observableList(new ArrayList<>())));
                    //getProfile().getData().add(new XYChart.Series<>("Y-Position", FXCollections.observableList(new ArrayList<>())));
                    //getProfile().getData().add(new XYChart.Series<>("Y-Velocity", FXCollections.observableList(new ArrayList<>())));
                    //getProfile().getData()
                    //            .add(new XYChart.Series<>("Y-Acceleration", FXCollections.observableList(new ArrayList<>())));
                    //getProfile().getData().add(new XYChart.Series<>("Y-Jerk", FXCollections.observableList(new ArrayList<>())));
                    getProfile().getData().add(new XYChart.Series<>("Lift-Position", FXCollections.observableList(new ArrayList<>())));
                    getProfile().getData().add(new XYChart.Series<>("Lift-Velocity", FXCollections.observableList(new ArrayList<>())));
                    getProfile().getData()
                            .add(new XYChart.Series<>("Lift-Acceleration", FXCollections.observableList(new ArrayList<>())));
                    getProfile().getData().add(new XYChart.Series<>("Lift-Setpoint", FXCollections.observableList(new ArrayList<>())));
                });
                
                return null;
            }
        }.run();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    
    public static void setPrimaryStage(Stage primaryStage) {
        ProfileDashboard.primaryStage = primaryStage;
    }
    
    public static VBox getPrimaryContainer() {
        return primaryContainer;
    }
    
    public static void setPrimaryContainer(VBox primaryContainer) {
        ProfileDashboard.primaryContainer = primaryContainer;
    }
    
    public static LineChart<Number, Number> getProfile() {
        return profile;
    }
    
    public static void setProfile(LineChart<Number, Number> profile) {
        ProfileDashboard.profile = profile;
    }
    
    public static boolean hasInitialized() {
        return hasInitialized;
    }
    
    public static void setHasInitialized(boolean hasInitialized) {
        ProfileDashboard.hasInitialized = hasInitialized;
    }
    
    /**
     * The application initialization method. This method is called immediately
     * after the Application class is loaded and constructed. An application may
     * override this method to perform initialization prior to the actual starting
     * of the application.
     *
     * <p>
     * The implementation of this method provided by the Application class does nothing.
     * </p>
     *
     * <p>
     * NOTE: This method is not called on the JavaFX Application Thread. An
     * application must not construct a Scene or a Stage in this
     * method.
     * An application may construct other JavaFX objects in this method.
     * </p>
     */
    @Override
    public void init() {
        setPrimaryContainer(new VBox());
        if(Main.isAutoRangeGraph()) {
            setProfile(
                    new LineChart<>(new NumberAxis(), new NumberAxis()));
        } else {
            setProfile(
                    new LineChart<>(new NumberAxis("Time (seconds)", 0d, 10d, 5d), new NumberAxis("Profile", -60d, 100d, 10d)));
        }

        //getProfile().getData().add(new XYChart.Series<>("X-Position", FXCollections.observableList(new ArrayList<>())));
        //getProfile().getData().add(new XYChart.Series<>("X-Velocity", FXCollections.observableList(new ArrayList<>())));
        //getProfile().getData()
        //            .add(new XYChart.Series<>("X-Acceleration", FXCollections.observableList(new ArrayList<>())));
        //getProfile().getData().add(new XYChart.Series<>("X-Jerk", FXCollections.observableList(new ArrayList<>())));
        //getProfile().getData().add(new XYChart.Series<>("Y-Position", FXCollections.observableList(new ArrayList<>())));
        //getProfile().getData().add(new XYChart.Series<>("Y-Velocity", FXCollections.observableList(new ArrayList<>())));
        //getProfile().getData()
        //            .add(new XYChart.Series<>("Y-Acceleration", FXCollections.observableList(new ArrayList<>())));
        //getProfile().getData().add(new XYChart.Series<>("Y-Jerk", FXCollections.observableList(new ArrayList<>())));
        getProfile().getData().add(new XYChart.Series<>("Lift-Position", FXCollections.observableList(new ArrayList<>())));
        getProfile().getData().add(new XYChart.Series<>("Lift-Velocity", FXCollections.observableList(new ArrayList<>())));
        getProfile().getData()
                .add(new XYChart.Series<>("Lift-Acceleration", FXCollections.observableList(new ArrayList<>())));
        getProfile().getData().add(new XYChart.Series<>("Lift-Setpoint", FXCollections.observableList(new ArrayList<>())));

        if(Main.isAutoRangeGraph()) {
            getProfile().getXAxis().setLabel("Time (seconds)");
            getProfile().getYAxis().setLabel("Profile");
            getProfile().getXAxis().setAnimated(true);
            getProfile().getYAxis().setAnimated(true);
        }
    }
    
    /**
     * The main entry point for all JavaFX applications.
     * The start method is called after the init method has returned,
     * and after the system is ready for the application to begin running.
     *
     * <p>
     * NOTE: This method is called on the JavaFX Application Thread.
     * </p>
     *
     
     * @param primaryStage the primary stage for this application, onto which
     * the application scene can be set. The primary stage will be embedded in
     * the browser if the application was launched as an applet.
     * Applications may create other stages, if needed, but they will not be
     * primary stages and will not be embedded in the browser.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        setPrimaryStage(primaryStage);
        
        getPrimaryStage().setTitle("Smart Dashboard - Revised - Team 5243");
        getPrimaryStage().setScene(getScene());
        
        try {
            getPrimaryStage().getIcons().add(new Image("https://avatars0.githubusercontent.com/u/14865780?v=3&s=400"));
        } catch(IllegalArgumentException e) {
            System.out.println("Icon unable to be loaded.  Loading default icon.");
        } finally {
            setHasInitialized(true);
            getPrimaryStage().show();
        }
    }
    
    /**
     * Creates a {@code Scene} instance to be used by the {@code primaryStage} given from the
     * {@code start()} {@code Method}.
     *
     * <p>
     *     All properties and {@code Nodes} are set and refined for the {@code Scene} to use.
     * </p>
     *
     * @return {@code Scene} instance taking part of the majority of the {@code Stage}, containing
     *         all of the {@code Nodes} and their properties to ensure proper functionality.
     *
     * @see Stage
     * @see Scene
     * @see Node
     */
    private Scene getScene() {
        getPrimaryContainer().getChildren().add(getProfile());
        getProfile().setCreateSymbols(false);
        Scene scene = new Scene(getPrimaryContainer(), 812d, 516d);
        scene.getStylesheets().add(getClass().getResource("dark_theme.css").toString());
        return scene;
    }
    
    public enum ProfileType {
        POSITION(0), VELOCITY(1), ACCELERATION(2), JERK(3);
        
        private int value;
        
        ProfileType(final int value) {
            setValue(value);
        }
        
        public int getValue() {
            return value;
        }
        
        public void setValue(int value) {
            this.value = value;
        }
    }
    
    public enum Direction {
        X(0), Y(4), LIFT(0);
        
        private int value;
        
        Direction(final int value) {
            setValue(value);
        }
        
        public int getValue() {
            return value;
        }
        
        public void setValue(int value) {
            this.value = value;
        }
    }
}
