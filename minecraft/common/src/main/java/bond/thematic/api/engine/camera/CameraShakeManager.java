package bond.thematic.api.engine.camera;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class CameraShakeManager {
    private static float intensity;
    private static int duration;
    private static int elapsed;
    private static float offsetX;
    private static float offsetY;
    private static float offsetZ;

    public static void trigger(float intensity, int durationTicks) {
        CameraShakeManager.intensity = intensity;
        CameraShakeManager.duration = durationTicks;
        CameraShakeManager.elapsed = 0;
    }

    public static void tick() {
        if (elapsed >= duration) {
            offsetX = 0;
            offsetY = 0;
            offsetZ = 0;
            return;
        }
        float progress = (float) elapsed / duration;
        float decay = 1.0f - progress;
        float activeIntensity = intensity * decay * decay;
        offsetX = (float) (Math.random() - 0.5) * 2 * activeIntensity;
        offsetY = (float) (Math.random() - 0.5) * 2 * activeIntensity;
        offsetZ = (float) (Math.random() - 0.5) * 2 * activeIntensity;
        elapsed++;
    }

    public static float getOffsetX() { return offsetX; }
    public static float getOffsetY() { return offsetY; }
    public static float getOffsetZ() { return offsetZ; }

    public static boolean isActive() { return elapsed < duration; }

    public static void stop() {
        elapsed = duration;
        offsetX = 0;
        offsetY = 0;
        offsetZ = 0;
    }
}
