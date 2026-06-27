package bond.thematic.api.engine.keyframe;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EffectKeyframe {
    String id();
    String display() default "";
}
