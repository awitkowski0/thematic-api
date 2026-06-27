package bond.thematic.api.engine.camera;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class CameraTiltState {
    private static double roll;
    private static double prevRoll;
    private static double pitch;
    private static double prevPitch;
    private static double yaw;
    private static double prevYaw;

    public static void setRoll(double value) {
        prevRoll = roll;
        roll = value;
    }

    public static void setPitch(double value) {
        prevPitch = pitch;
        pitch = value;
    }

    public static void setYaw(double value) {
        prevYaw = yaw;
        yaw = value;
    }

    public static void set(double rollDeg, double pitchDeg, double yawDeg) {
        setRoll(rollDeg);
        setPitch(pitchDeg);
        setYaw(yawDeg);
    }

    public static double getRoll(float tickDelta) {
        return prevRoll + (roll - prevRoll) * tickDelta;
    }

    public static double getPitch(float tickDelta) {
        return prevPitch + (pitch - prevPitch) * tickDelta;
    }

    public static double getYaw(float tickDelta) {
        return prevYaw + (yaw - prevYaw) * tickDelta;
    }

    public static void reset() {
        prevRoll = roll;
        roll = 0;
        prevPitch = pitch;
        pitch = 0;
        prevYaw = yaw;
        yaw = 0;
    }

    public static void tick() {
        prevRoll = roll;
        prevPitch = pitch;
        prevYaw = yaw;
    }
}
