package com.stuypulse.robot.subsystems.amper;
import java.util.Optional;

import com.revrobotics.CANSparkLowLevel.MotorType;
import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;
import com.stuypulse.robot.constants.Motors;
import com.stuypulse.robot.constants.Ports;
import com.stuypulse.robot.constants.Settings;
import com.stuypulse.robot.constants.Settings.Amper.Lift;
import com.stuypulse.stuylib.control.Controller;
import com.stuypulse.stuylib.control.feedback.PIDController;
import com.stuypulse.stuylib.control.feedforward.ElevatorFeedforward;
import com.stuypulse.stuylib.control.feedforward.MotorFeedforward;
import com.stuypulse.stuylib.network.SmartNumber;
import com.stuypulse.stuylib.streams.numbers.filters.MotionProfile;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;


public class AmperImpl extends Amper {
    
    private final CANSparkMax scoreMotor;
    private final CANSparkMax liftMotor;
    private final RelativeEncoder liftEncoder;

    private final DigitalInput alignedSwitch;
    private final DigitalInput minSwitch;
    private final DigitalInput maxSwitch;
    private final DigitalInput ampIRSensor;

    private final Controller controller;

    private Optional<Double> voltageOverride;

    private final SmartNumber maxVelocity;
    private final SmartNumber maxAcceleration;

    public AmperImpl() {
        scoreMotor = new CANSparkMax(Ports.Amper.SCORE, MotorType.kBrushless);
        liftMotor = new CANSparkMax(Ports.Amper.LIFT, MotorType.kBrushless);
        liftEncoder = liftMotor.getEncoder();

        liftEncoder.setPositionConversionFactor(Settings.Amper.Lift.Encoder.POSITION_CONVERSION);
        liftEncoder.setVelocityConversionFactor(Settings.Amper.Lift.Encoder.VELOCITY_CONVERSION);

        alignedSwitch = new DigitalInput(Ports.Amper.ALIGNED_BUMP_SWITCH);
        minSwitch = new DigitalInput(Ports.Amper.LIFT_BOTTOM_LIMIT);
        maxSwitch = new DigitalInput(Ports.Amper.LIFT_TOP_LIMIT);
        ampIRSensor = new DigitalInput(Ports.Amper.AMP_IR);

        maxVelocity = new SmartNumber("Amper/Lift/Max Velocity", Lift.VEL_LIMIT);
        maxAcceleration = new SmartNumber("Amper/Lift/Max Acceleration", Lift.ACCEL_LIMIT);

        controller = new MotorFeedforward(Lift.Feedforward.kS, Lift.Feedforward.kV, Lift.Feedforward.kA).position()
            .add(new ElevatorFeedforward(Lift.Feedforward.kG))
            .add(new PIDController(Lift.PID.kP, Lift.PID.kI, Lift.PID.kD))
            .setSetpointFilter(new MotionProfile(maxVelocity, maxAcceleration));

        voltageOverride = Optional.empty();

        Motors.Amper.LIFT_MOTOR.configure(liftMotor);
        Motors.Amper.SCORE_MOTOR.configure(scoreMotor);
    }

    /*** LIFT CONTROL ***/

    @Override
    public boolean liftAtBottom() {
        return !minSwitch.get();
    }

    @Override
    public boolean liftAtTop() {
        return !maxSwitch.get();
    }

    @Override
    public double getLiftHeight() {
        return liftEncoder.getPosition();
    }

    @Override
    public void stopLift() {
        liftMotor.stopMotor();
    }

    @Override
    public void setTargetHeight(double height) {
        super.setTargetHeight(height);

        voltageOverride = Optional.empty();
    }

    /*** IR SENSOR ***/

    @Override
    public boolean hasNote() {
        return !ampIRSensor.get();
    }

    /*** SCORE ROLLERS ***/

    @Override
    public void score() {
        scoreMotor.set(Settings.Amper.Score.SCORE_SPEED.get());
    }

    @Override
    public void intake() {
        scoreMotor.set(-Settings.Amper.Score.INTAKE_SPEED.get());
    }

    @Override
    public void stopRoller() {
        scoreMotor.stopMotor();
    }

    @Override
    public boolean touchingAmp() {
        return !alignedSwitch.get();
    }

    /*** LIFT CONFIG ***/

    @Override
    public void setVoltageOverride(double voltage) {
        voltageOverride = Optional.of(voltage);
    }

    @Override
    public void setConstraints(double maxVelocity, double maxAcceleration) {
        this.maxVelocity.set(maxVelocity);
        this.maxAcceleration.set(maxAcceleration);
    }

    @Override
    public void periodic() {
        super.periodic();

        controller.update(getTargetHeight(), getLiftHeight());

        double voltage = voltageOverride.orElse(controller.getOutput());
        
        if (liftAtBottom() && voltage < 0 || liftAtTop() && voltage > 0) {
            stopLift();
        } else {
            liftMotor.setVoltage(voltage);
        }

        SmartDashboard.putNumber("Amper/Intake Speed", scoreMotor.get());
        SmartDashboard.putNumber("Amper/Lift Speed", liftMotor.get());
        SmartDashboard.putNumber("Amper/Intake Current", scoreMotor.getOutputCurrent());
        SmartDashboard.putNumber("Amper/Lift Current", liftMotor.getOutputCurrent());
        SmartDashboard.putNumber("Amper/Lift Height", getLiftHeight());
    }

}
