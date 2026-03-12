package dev.kosmx.animatorTestmod;

import bond.thematic.api.core.data.KeyframeAnimation;
import bond.thematic.api.layered.StateMachineAnimation;
import bond.thematic.api.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.UUID;

public class FlightController {
    private static final HashMap<UUID, Integer> lastFlightDir = new HashMap<>();

    public static StateMachineAnimation createFlightAnimation(PlayerEntity player) {
        return new StateMachineAnimation(player, 5.0f, (p) -> {
            if (p.getAbilities() == null || !p.getAbilities().flying || p.isOnGround()) {
                lastFlightDir.put(p.getUuid(), 0);
                return null;
            }

            // Directional flight logic based on rotation and movement
            Vec3d velocity = new Vec3d(p.getX() - p.prevX, p.getY() - p.prevY, p.getZ() - p.prevZ);
            if (velocity.lengthSquared() < 0.0001) {
                // Try fallback to standard velocity if prevX/Y/Z are zeroed (e.g. first tick)
                velocity = p.getVelocity();
            }
            
            // Get standard horizontal forward vector
            float yawRad = p.getYaw() * ((float)Math.PI / 180F);
            Vec3d forwardVec = new Vec3d(-Math.sin(yawRad), 0, Math.cos(yawRad));
            
            double forwardMove = forwardVec.x * velocity.x + forwardVec.z * velocity.z;
            double upMove = velocity.y;
            
            int currentDir = lastFlightDir.getOrDefault(p.getUuid(), 0);
            double threshold = 0.03; // Even lower threshold for creative drift
            KeyframeAnimation result;

            if (Math.abs(forwardMove) < threshold && Math.abs(upMove) < threshold) {
                // If we were flying forward/backward, play the return animation ONCE
                if (currentDir == 1) {
                    result = getAnim("super.flight.front.return");
                    lastFlightDir.put(p.getUuid(), 0); 
                } else if (currentDir == -1) {
                    result = getAnim("super.flight.back.return");
                    lastFlightDir.put(p.getUuid(), 0);
                } else {
                    lastFlightDir.put(p.getUuid(), 0);
                    result = getAnim("super.flight.idle");
                }
            } else if (Math.abs(forwardMove) >= Math.abs(upMove)) {
                lastFlightDir.put(p.getUuid(), forwardMove > 0 ? 1 : -1);
                result = getAnim(forwardMove > 0 ? "super.flight.front" : "super.flight.back");
            } else {
                lastFlightDir.put(p.getUuid(), upMove > 0 ? 2 : -2);
                result = getAnim(upMove > 0 ? "super.flight.up" : "super.flight.down");
            }
            
            if (result != null) {
                result.extraData.put("type", "static");
                if (!result.extraData.containsKey("name")) {
                    result.extraData.put("name", result.getUuid().toString());
                }
                
                System.out.println("[FlightController] Selecting: " + result.extraData.get("name") + " (f=" + forwardMove + ", u=" + upMove + ")");
            }
            return result;
        });
    }

    private static KeyframeAnimation getAnim(String name) {
        KeyframeAnimation anim = PlayerAnimationRegistry.getAnimation(new Identifier("testmod", name));
        if (anim == null) {
            System.err.println("[FlightController] Animation not found in registry: " + name);
        }
        return anim;
    }
}
