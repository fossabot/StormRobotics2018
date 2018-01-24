package AutoModes.Commands;


import AutoModes.Modes.RightSwitch;
import Subsystems.DriveTrain;


import Subsystems.NavX;

import com.ctre.phoenix.motion.SetValueMotionProfile;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import edu.wpi.first.wpilibj.Notifier;
import edu.wpi.first.wpilibj.command.Command;
import jaci.pathfinder.Pathfinder;
import jaci.pathfinder.Trajectory;
import jaci.pathfinder.Waypoint;
import jaci.pathfinder.followers.EncoderFollower;
import jaci.pathfinder.modifiers.TankModifier;

import com.ctre.phoenix.motion.TrajectoryPoint;

import java.io.File;

public class ProfileFollower extends Command{

    EncoderFollower left;
    EncoderFollower right;
    Trajectory leftTra;
    Trajectory rightTra;




    public ProfileFollower(String csv){
        DriveTrain._rightMain.changeMotionControlFramePeriod(5);
        DriveTrain._leftMain.changeMotionControlFramePeriod(5);

        notifier.startPeriodic(0.005);
    }

    class PeriodicRunnable implements java.lang.Runnable {
        public void run() {
            DriveTrain._leftMain.processMotionProfileBuffer();
            DriveTrain._rightMain.processMotionProfileBuffer();
        }
    }

    Notifier notifier = new Notifier(new PeriodicRunnable());


    /**
     * The execute method is called repeatedly until this Command either finishes or is canceled.
     */
    @Override
    protected void execute() {
        /*
        super.execute();
        System.err.println("Execute ProfileFollower.");
        double l = left.calculate(leftMotor.getSelectedSensorPosition(0));
        double r = right.calculate(rightMotor.getSelectedSensorPosition(0));
        double gyro_heading = NavX.getNavx().getRawGyroZ();
        double desired_heading = Pathfinder.r2d(left.getHeading());

        double angleDifference = Pathfinder.boundHalfDegrees(desired_heading - gyro_heading);
        double turn = 0.8 * (-1.0/80.0) * angleDifference;
        System.out.println("Left: " + (l + turn));
        System.out.println("Right: " + (r - turn));
        leftMotor.set(ControlMode.PercentOutput, l + turn);
        rightMotor.set(ControlMode.PercentOutput, -(r - turn));
        */

    }

    /**
     * The initialize method is called the first time this Command is run after being started.
     */
    @Override
    protected void initialize() {
        super.initialize();

        Waypoint[] points = new Waypoint[] {
                new Waypoint(0, 0, Pathfinder.d2r(-45)),      // Waypoint @ x=-4, y=-1, exit angle=-45 degrees
                new Waypoint(5, 3, 0),                        // Waypoint @ x=-2, y=-2, exit angle=0 radians
                new Waypoint(7, 7, 0)                           // Waypoint @ x=0, y=0,   exit angle=0 radians
        };


        Trajectory.Config config = new Trajectory.Config(Trajectory.FitMethod.HERMITE_CUBIC, Trajectory.Config.SAMPLES_HIGH, 0.05, 1.7, 2.0, 60.0);
        Trajectory trajectory = Pathfinder.generate(points, config);

        TankModifier modifier = new TankModifier(trajectory).modify(0.5);

        left = new EncoderFollower(modifier.getLeftTrajectory());

        right = new EncoderFollower(modifier.getRightTrajectory());

        DriveTrain._leftMain.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 10);

        left.configureEncoder(DriveTrain._leftMain.getSelectedSensorPosition(0), 1000, 6);

        DriveTrain._rightMain.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 10);

        right.configureEncoder(DriveTrain._rightMain.getSelectedSensorPosition(0), 1000, 6);


        leftTra = modifier.getLeftTrajectory();

        rightTra = modifier.getRightTrajectory();

        TrajectoryPoint leftPoint = new TrajectoryPoint();
        TrajectoryPoint rightPoint = new TrajectoryPoint();

        for(int i = 0; i < trajectory.length(); i++){

            leftPoint.position = leftTra.segments[i].position;
            rightPoint.position = rightTra.segments[i].position;

            leftPoint.velocity = leftTra.segments[i].velocity;
            rightPoint.velocity = rightTra.segments[i].velocity;

            leftPoint.zeroPos = false;
            rightPoint.zeroPos = false;


            TrajectoryPoint.TrajectoryDuration duration = TrajectoryPoint.TrajectoryDuration.Trajectory_Duration_0ms;
            duration = duration.valueOf((int) leftTra.segments[i].dt);

            leftPoint.timeDur = duration;
            rightPoint.timeDur = duration;

            leftPoint.profileSlotSelect0 = 0; /* which set of gains would you like to use [0,3]? */
            leftPoint.profileSlotSelect1 = 0;

            rightPoint.profileSlotSelect0 = 0; /* which set of gains would you like to use [0,3]? */
            rightPoint.profileSlotSelect1 = 0;


            if (i == 0){
                leftPoint.zeroPos = true;
                rightPoint.zeroPos = true;
            }

            leftPoint.isLastPoint = false;
            rightPoint.isLastPoint = false;

            if (i + 1 == trajectory.length()) {
                leftPoint.isLastPoint = true;
                rightPoint.isLastPoint = true;
            }

            DriveTrain._leftMain.pushMotionProfileTrajectory(leftPoint);
            DriveTrain._rightMain.pushMotionProfileTrajectory(rightPoint);
        }


        DriveTrain._leftMain.set(ControlMode.MotionProfile, SetValueMotionProfile.Enable.value);
        DriveTrain._rightMain.set(ControlMode.MotionProfile, SetValueMotionProfile.Enable.value);

    }

    @Override
    protected boolean isFinished() {
        return false;
    }
}