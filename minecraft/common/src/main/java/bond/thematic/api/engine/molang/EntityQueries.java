package bond.thematic.api.engine.molang;

import net.minecraft.entity.LivingEntity;

public class EntityQueries {
    private LivingEntity entity;

    public void setEntity(LivingEntity entity) {
        this.entity = entity;
    }

    public void applyTo(MolangEnvironment env) {
        env.registerQuery("is_on_ground", () -> bool(entity != null && entity.isOnGround()));
        env.registerQuery("is_sneaking", () -> bool(entity != null && entity.isSneaking()));
        env.registerQuery("is_sprinting", () -> bool(entity != null && entity.isSprinting()));
        env.registerQuery("is_flying", () -> bool(entity != null && !entity.isOnGround()));
        env.registerQuery("is_alive", () -> bool(entity != null && entity.isAlive()));
        env.registerQuery("is_in_water", () -> bool(entity != null && entity.isTouchingWater()));
        env.registerQuery("is_in_lava", () -> bool(entity != null && entity.isInLava()));
        env.registerQuery("health", () -> entity != null ? entity.getHealth() : 0.0);
        env.registerQuery("max_health", () -> entity != null ? entity.getMaxHealth() : 0.0);
        env.registerQuery("horizontal_speed", () -> {
            if (entity == null) return 0.0;
            double dx = entity.getX() - entity.prevX;
            double dz = entity.getZ() - entity.prevZ;
            return Math.sqrt(dx * dx + dz * dz) * 20.0;
        });
        env.registerQuery("vertical_speed", () -> {
            if (entity == null) return 0.0;
            return (entity.getY() - entity.prevY) * 20.0;
        });
        env.registerQuery("speed", () -> {
            if (entity == null) return 0.0;
            double dx = entity.getX() - entity.prevX;
            double dy = entity.getY() - entity.prevY;
            double dz = entity.getZ() - entity.prevZ;
            return Math.sqrt(dx * dx + dy * dy + dz * dz) * 20.0;
        });
        env.registerQuery("ground_speed", () -> {
            if (entity == null) return 0.0;
            if (!entity.isOnGround()) return 0.0;
            double dx = entity.getX() - entity.prevX;
            double dz = entity.getZ() - entity.prevZ;
            return Math.sqrt(dx * dx + dz * dz) * 20.0;
        });
        env.registerQuery("yaw_delta", () -> {
            if (entity == null) return 0.0;
            double delta = entity.getYaw() - entity.prevYaw;
            return delta < -180 ? delta + 360 : delta > 180 ? delta - 360 : delta;
        });
        env.registerQuery("body_yaw", () -> entity != null ? entity.bodyYaw : 0.0);
        env.registerQuery("pitch", () -> entity != null ? entity.getPitch() : 0.0);
        env.registerQuery("yaw", () -> entity != null ? entity.getYaw() : 0.0);
        env.registerQuery("age", () -> entity != null ? entity.age : 0.0);
        env.registerQuery("swing_progress", () -> entity != null ? entity.getHandSwingProgress(1.0f) : 0.0);
        env.registerQuery("forward_speed", () -> {
            if (entity == null) return 0.0;
            double dx = entity.getX() - entity.prevX;
            double dz = entity.getZ() - entity.prevZ;
            float yawRad = entity.getYaw() * (float)Math.PI / 180.0f;
            return (-Math.sin(yawRad) * dx + Math.cos(yawRad) * dz) * 20.0;
        });
        env.registerQuery("sideways_speed", () -> {
            if (entity == null) return 0.0;
            double dx = entity.getX() - entity.prevX;
            double dz = entity.getZ() - entity.prevZ;
            float yawRad = entity.getYaw() * (float)Math.PI / 180.0f;
            return (Math.cos(yawRad) * dx + Math.sin(yawRad) * dz) * 20.0;
        });
        env.registerQuery("upward_speed", () -> {
            if (entity == null) return 0.0;
            return (entity.getY() - entity.prevY) * 20.0;
        });
    }

    public void reset() {
        this.entity = null;
    }

    private static double bool(boolean value) {
        return value ? 1.0 : 0.0;
    }
}
