// rip to all my old code of the arduino system

package frc.robot.Misc;

import java.util.Optional;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class LED extends SubsystemBase {
    private BlinkinLEDController controller;
    private boolean note = false;
    private DigitalInput noteSensor;
    private STATUS mode;

    public LED(BlinkinLEDController controll, DigitalInput nSensor ) { controller = controll; noteSensor = nSensor; mode = STATUS.DEFAULT; }

    public void setMode(STATUS status) {
        switch (mode) {
            case BROWNOUT -> { mode = STATUS.BROWNOUT; }
            case BOOTING, AUTO -> { if (status != STATUS.SHOOTING) mode = status; }
            case VIBE -> { mode = STATUS.VIBE; }
            case SHOOTING, DEFAULT -> { mode = status; }
            case ERROR -> { if (status != STATUS.DEFAULT) mode = status; }
            default -> { mode = status; }
        }
    }

    public void setBrownout(boolean currentlyBrown) {
        if (currentlyBrown) mode = STATUS.BROWNOUT;
        else mode = STATUS.DEFAULT;
    }

    public STATUS getState() { updateCurrentMode(); return mode; }

    public void defaultState() { if (mode != STATUS.VIBE) { setMode(STATUS.DEFAULT); } else { mode = STATUS.DEFAULT; } }

    @Override
    public void periodic(){
        updateNoteSatus();
        SmartDashboard.putBoolean("Note Intaked", noteSensor.get());
        BlinkinLEDController.BlinkinPattern pattern = StatusEnumBlinkinTranslator(mode, note);
        controller.setPattern(pattern);
    }

    public void updateNoteSatus() {
        note = noteSensor.get();
    }

    private void updateCurrentMode() {
        mode = BlinkinEnumStatusTranslator(controller.getPattern());
    }

    public static enum STATUS {
        BOOTING,
        DEFAULT,
        SHOOTING,
        AUTO,
        VIBE,
        ERROR,
        BROWNOUT
    }

    public static STATUS BlinkinEnumStatusTranslator(BlinkinLEDController.BlinkinPattern pattern) {
        switch (pattern) {
            case RAINBOW_RAINBOW_PALETTE:
                return STATUS.VIBE;
            case RED, BLUE, GREEN:
                return STATUS.DEFAULT;
            case VIOLET:
                return STATUS.SHOOTING;
            case ORANGE:
                return STATUS.BROWNOUT;
            default:
                return STATUS.ERROR;
        }
    }

    public static BlinkinLEDController.BlinkinPattern StatusEnumBlinkinTranslator(STATUS status, boolean note) {
        if (note && status == STATUS.DEFAULT)return BlinkinLEDController.BlinkinPattern.GREEN;
        switch (status) {
            case VIBE:
                return BlinkinLEDController.BlinkinPattern.RAINBOW_RAINBOW_PALETTE;
            case DEFAULT:
                Optional<Alliance> ally = DriverStation.getAlliance();
                if (ally.isPresent()) {
                    Alliance currentAlliance = ally.get();
                    if (currentAlliance == Alliance.Red) return BlinkinLEDController.BlinkinPattern.RED;
                    if (currentAlliance == Alliance.Blue) return BlinkinLEDController.BlinkinPattern.BLUE;
                }
                else return BlinkinLEDController.BlinkinPattern.WHITE;
            case BROWNOUT:
                return BlinkinLEDController.BlinkinPattern.ORANGE;
            default:
                return BlinkinLEDController.BlinkinPattern.FIRE_LARGE;
        }
    }
}
