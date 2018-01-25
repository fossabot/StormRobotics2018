package Subsystems;

import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.command.PIDSubsystem;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.kauailabs.navx.frc.AHRS;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import robot.Constants;

public class Elevator extends Subsystem{

    public static final TalonSRX _left = new TalonSRX(Constants.PORT_MOTOR_DRIVE_ELEVATOR_MAIN); //formerly _leftMain
    public static final TalonSRX _right = new TalonSRX(Constants.PORT_MOTOR_DRIVE_ELEVATOR_2);  //formerly _left2

    double height;



    public Elevator(double _height) {
        _right.follow(_left);
        height = _height;
    }

    protected void initDefaultCommand() {

    }

    //@Override


    public static void elevate(double liftSpeed, boolean squareValues, double tolerance){

        if(squareValues){
            liftSpeed = Math.pow(liftSpeed, 2);
        }



        _left.set(ControlMode.PercentOutput, liftSpeed);

    }



}
