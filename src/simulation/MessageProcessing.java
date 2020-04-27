package simulation;

import java.util.ArrayList;

public class MessageProcessing {
    public enum LinearExtensionType {
        CONTINUOUS, CASCADE
    }

    //this handles the list of debugPoints to be drawn on the screen
    private static ArrayList<FloatPoint> debugPoints = new ArrayList<>();

    //this is used to show paths
    private static ArrayList<FloatPoint> pointLog = new ArrayList<>();

    private static ArrayList<Line> debugLines = new ArrayList<>();

    private static double robotX = 0d;
    private static double robotY = 0d;
    private static double robotAngle = 0d;

    private static double robotVelocityX = 0;
    private static double robotVelocityY = 0;

    private static double linearExtensionPosition = 0d; //in
    private static double stageLength = 18d; //in
    private static int linearStageCount = 1;
    private static LinearExtensionType linearExtensionType = LinearExtensionType.CASCADE;

    public static void processMessage(String receivedMessage) {
        //first there might be multiple messages in one packet. These are separated by '%'
        String[] splitMessages = receivedMessage.split("%");
        //go through all the messages now
        for(String message : splitMessages) {
            //the individual messages are split using commas
            String[] splitString = message.split(",");
            String id = splitString[0];
            if(id.equals("ROBOT")) {
                //System.out.println("updating robot");
                processRobotLocation(splitString);
            } else {
                if(id.equals("P")){//POINT codes for debug point, just display it on the screen as a dot
                    processPoint(splitString);
                } else {
                    if(id.equals("LINE")){
                        //System.out.println("updating line");
                        processLine(splitString);
                    } else {
                        if(id.equals("LP")) {//log point
                            //add point
                            addPoint(splitString);
                        } else if(id.equals("LogClear")) {
                            getPointLog().clear();
                        } else if(id.equals("Position")) {
                            processPosition(splitString);
                        } else if(id.equals("Velocity")) {
                            processVelocity(splitString);
                        } else if(id.equals("Acceleration")) {
                            processAcceleration(splitString);
                        } else if(id.equals("Jerk")) {
                            processJerk(splitString);
                        } else if(id.equals("ProfileClear")) {
                            if(ProfileDashboard.hasInitialized()) {
                                ProfileDashboard.resetProfile();
                            }
                        } else if(id.equals("LinearPosition")) {
                            processLinearExtensionPosition(splitString);
                        } else if(id.equals("StageLength")) {
                            processStageLength(splitString);
                        } else if(id.equals("StageCount")) {
                            processStageCount(splitString);
                        } else if(id.equals("Continuous")) {
                            setLinearExtensionType(LinearExtensionType.CONTINUOUS);
                        } else if(id.equals("Cascade")) {
                            setLinearExtensionType(LinearExtensionType.CASCADE);
                        } else if(id.equals("LiftPosition")) {
                            processLiftPosition(splitString);
                        } else if(id.equals("LiftVelocity")) {
                            processLiftVelocity(splitString);
                        } else if(id.equals("LiftAcceleration")) {
                            processLiftAcceleration(splitString);
                        } else if(id.equals("LiftJerk")) {
                            processLiftJerk(splitString);
                        } else if(id.equals("LiftInput")) {
                            processLiftInput(splitString);
                        }
                        
                        if(id.length() >= 5) {
                            if(id.substring(0, 5).equals("CLEAR")){
                                //System.out.println("clearing");
                                clear();
                            }
                        }
                    }
                }
            }
        }
    }

    public static void processStageLength(String[] splitString) {
        if(splitString.length != 2) {
            return;
        }

        setStageLength(Double.parseDouble(splitString[1]));
    }

    public static void processStageCount(String[] splitString) {
        if(splitString.length != 2) {
            return;
        }

        setLinearStageCount(Integer.parseInt(splitString[1]));
    }

    public static void processLinearExtensionPosition(String[] splitString) {
        if(splitString.length != 2) {
            return;
        }

        setLinearExtensionPosition(Double.parseDouble(splitString[1]));
    }

    public static void processPosition(String[] splitString) {
        if(splitString.length != 4 || !ProfileDashboard.hasInitialized()) {
            return;
        }
        
        ProfileDashboard.addProfilePoint(ProfileDashboard.ProfileType.POSITION, ProfileDashboard.Direction.X, Double.parseDouble(splitString[1]), Double.parseDouble(splitString[2]));
        ProfileDashboard.addProfilePoint(ProfileDashboard.ProfileType.POSITION, ProfileDashboard.Direction.Y, Double.parseDouble(splitString[1]), Double.parseDouble(splitString[3]));
    }
    
    public static void processVelocity(String[] splitString) {
        if(splitString.length != 4 || !ProfileDashboard.hasInitialized()) {
            return;
        }

        setRobotVelocityX((int)(100 * Double.parseDouble(splitString[1])) / 100d);
        setRobotVelocityY((int)(100 * Double.parseDouble(splitString[2])) / 100d);
        ProfileDashboard.addProfilePoint(ProfileDashboard.ProfileType.VELOCITY, ProfileDashboard.Direction.X, Double.parseDouble(splitString[1]), Double.parseDouble(splitString[2]));
        ProfileDashboard.addProfilePoint(ProfileDashboard.ProfileType.VELOCITY, ProfileDashboard.Direction.Y, Double.parseDouble(splitString[1]), Double.parseDouble(splitString[3]));
    }
    
    public static void processAcceleration(String[] splitString) {
        if(splitString.length != 4 || !ProfileDashboard.hasInitialized()) {
            return;
        }
        
        ProfileDashboard.addProfilePoint(ProfileDashboard.ProfileType.ACCELERATION, ProfileDashboard.Direction.X, Double.parseDouble(splitString[1]), Double.parseDouble(splitString[2]));
        ProfileDashboard.addProfilePoint(ProfileDashboard.ProfileType.ACCELERATION, ProfileDashboard.Direction.Y, Double.parseDouble(splitString[1]), Double.parseDouble(splitString[3]));
    }
    
    public static void processJerk(String[] splitString) {
        if(splitString.length != 4 || !ProfileDashboard.hasInitialized()) {
            return;
        }
        
        ProfileDashboard.addProfilePoint(ProfileDashboard.ProfileType.JERK, ProfileDashboard.Direction.X, Double.parseDouble(splitString[1]), Double.parseDouble(splitString[2]));
        ProfileDashboard.addProfilePoint(ProfileDashboard.ProfileType.JERK, ProfileDashboard.Direction.Y, Double.parseDouble(splitString[1]), Double.parseDouble(splitString[3]));
    }

    public static void processLiftPosition(String[] splitString) {
        if(splitString.length != 3 || !ProfileDashboard.hasInitialized()) {
            return;
        }

        ProfileDashboard.addProfilePoint(ProfileDashboard.ProfileType.POSITION, ProfileDashboard.Direction.LIFT, Double.parseDouble(splitString[1]), Double.parseDouble(splitString[2]));
    }

    public static void processLiftVelocity(String[] splitString) {
        if(splitString.length != 3 || !ProfileDashboard.hasInitialized()) {
            return;
        }

        ProfileDashboard.addProfilePoint(ProfileDashboard.ProfileType.VELOCITY, ProfileDashboard.Direction.LIFT, Double.parseDouble(splitString[1]), Double.parseDouble(splitString[2]));
    }

    public static void processLiftAcceleration(String[] splitString) {
        if(splitString.length != 3 || !ProfileDashboard.hasInitialized()) {
            return;
        }

        ProfileDashboard.addProfilePoint(ProfileDashboard.ProfileType.ACCELERATION, ProfileDashboard.Direction.LIFT, Double.parseDouble(splitString[1]), Double.parseDouble(splitString[2]));
    }

    public static void processLiftJerk(String[] splitString) {
        if(splitString.length != 3 || !ProfileDashboard.hasInitialized()) {
            return;
        }

        ProfileDashboard.addProfilePoint(ProfileDashboard.ProfileType.JERK, ProfileDashboard.Direction.LIFT, Double.parseDouble(splitString[1]), Double.parseDouble(splitString[2]));
    }

    public static void processLiftInput(String[] splitString) {
        if(splitString.length != 3) {
            return;
        }

        ProfileDashboard.addProfilePoint(ProfileDashboard.ProfileType.INPUT, ProfileDashboard.Direction.LIFT, Double.parseDouble(splitString[1]), Double.parseDouble(splitString[2]));
    }

    /**
     * This processes the robot location and saves it's position
     * @param splitString
     */
    private static void processRobotLocation(String[] splitString) {
        if(splitString.length != 4) {
            return;
        }

        setRobotX(Double.parseDouble(splitString[1]));
        setRobotY(Double.parseDouble(splitString[2]));
        setRobotAngle(Double.parseDouble(splitString[3]));
    }

    /**
     * Takes a String[] and parses it into a point, adding it to the list of display points.
     * @param splitString
     */
    private static void processPoint(String[] splitString) {
        if(splitString.length == 3) {
            getDebugPoints().add(new FloatPoint(Double.parseDouble(splitString[1]), Double.parseDouble(splitString[2])));
        } else if(splitString.length == 4) {
            getDebugPoints().add(new FloatPoint(Double.parseDouble(splitString[1]), Double.parseDouble(splitString[2]),
                    Double.parseDouble(splitString[3]) / Screen.getInchesPerPixel()));
        }
    }

    /**
     * Adds to the list of point log
     * @param splitString String[] to be parsed into a point
     */
    private static void addPoint(String[] splitString) {
        if(splitString.length != 3) {
            return;
        }

        FloatPoint toBeAddedMaybe = new FloatPoint(Double.parseDouble(splitString[1]),
                Double.parseDouble(splitString[2]));
        //make sure the point doesn't already exist (close enough) in the list
        boolean alreadyExists = false;
        for(FloatPoint p : getPointLog()){
            if(Math.hypot(p.x - toBeAddedMaybe.x, p.y - toBeAddedMaybe.y) < 0.5d) {
                alreadyExists = true;
            }
        }

        //add it if it's unique
        if(!alreadyExists) {
            getPointLog().add(toBeAddedMaybe);
        }
    }

    private static void processLine(String[] splitString) {
        if(splitString.length != 5) {
            return;
        }

        getDebugLines().add(new Line(Double.parseDouble(splitString[1]),
                Double.parseDouble(splitString[2]),
                Double.parseDouble(splitString[3]),
                Double.parseDouble(splitString[4])));
    }

    /**
     * Clears the debug points ArrayList, occurs when the CLEAR command is send by the phone
     */
    private static void clear() {
        try {
            Main.getDrawSemaphore().acquire();
            Main.getDisplayPoints().clear();
            Main.getDisplayLines().clear();

            Main.getDisplayPoints().addAll(getDebugPoints());
            Main.getDisplayLines().addAll(getDebugLines());

            getDebugPoints().clear();
            getDebugLines().clear();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        Main.getDrawSemaphore().release();
    }

    public static ArrayList<FloatPoint> getDebugPoints() {
        return debugPoints;
    }

    public static void setDebugPoints(ArrayList<FloatPoint> debugPoints) {
        MessageProcessing.debugPoints = debugPoints;
    }

    public static ArrayList<FloatPoint> getPointLog() {
        return pointLog;
    }

    public static void setPointLog(ArrayList<FloatPoint> pointLog) {
        MessageProcessing.pointLog = pointLog;
    }

    public static ArrayList<Line> getDebugLines() {
        return debugLines;
    }

    public static void setDebugLines(ArrayList<Line> debugLines) {
        MessageProcessing.debugLines = debugLines;
    }

    public static double getRobotX() {
        return robotX;
    }

    public static double getRobotY() {
        return robotY;
    }

    public static double getRobotAngle() {
        return robotAngle;
    }

    public static void setRobotX(double robotX) {
        MessageProcessing.robotX = robotX;
    }

    public static void setRobotY(double robotY) {
        MessageProcessing.robotY = robotY;
    }

    public static void setRobotAngle(double robotAngle) {
        MessageProcessing.robotAngle = robotAngle;
    }

    public static double getRobotVelocityX() {
        return robotVelocityX;
    }

    public static void setRobotVelocityX(double robotVelocityX) {
        MessageProcessing.robotVelocityX = robotVelocityX;
    }

    public static double getRobotVelocityY() {
        return robotVelocityY;
    }

    public static void setRobotVelocityY(double robotVelocityY) {
        MessageProcessing.robotVelocityY = robotVelocityY;
    }

    public static double getLinearExtensionPosition() {
        return linearExtensionPosition;
    }

    public static void setLinearExtensionPosition(double linearExtensionPosition) {
        MessageProcessing.linearExtensionPosition = linearExtensionPosition;
    }

    public static LinearExtensionType getLinearExtensionType() {
        return linearExtensionType;
    }

    public static void setLinearExtensionType(LinearExtensionType linearExtensionType) {
        MessageProcessing.linearExtensionType = linearExtensionType;
    }

    public static int getLinearStageCount() {
        return linearStageCount;
    }

    public static void setLinearStageCount(int linearStageCount) {
        MessageProcessing.linearStageCount = linearStageCount;
    }

    public static double getStageLength() {
        return stageLength;
    }

    public static void setStageLength(double stageLength) {
        MessageProcessing.stageLength = stageLength;
    }
}
