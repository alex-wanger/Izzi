package com.stuypulse.robot.subsystems.intake;

import com.stuypulse.robot.constants.Settings;
import com.stuypulse.stuylib.network.SmartBoolean;
import com.stuypulse.stuylib.network.SmartNumber;

public class SimIntake extends Intake {

    SmartNumber motor = new SmartNumber("Intake/SpeedSim", 0);
    SmartBoolean ir = new SmartBoolean("Intake/IR", false);

    public SimIntake() {
        
    }

    @Override
    public void acquire() {
        motor.set(Settings.Intake.ACQUIRE_SPEED.getAsDouble());
    }

    @Override 
    public void deacquire() {
        motor.set(Settings.Intake.DEACQUIRE_SPEED.getAsDouble());
    }

    @Override
    public void stop() {
        motor.set(0);
    }

    @Override
    public double getSpeed() {
        return motor.get();
    }

    @Override
    public boolean hasNote() {
        return false;
    }

    @Override
    public boolean isIRTriggered() {
        return ir.get();
    }
}
