package bond.thematic.api.core.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Where is the emote from
 * use dev.kosmx.playerAnim.minecraftApi.codec, AnimationCodecs class for deserializing instead.
 * <p>
 * This package may be removed in the future
 */
@Deprecated
public enum AnimationFormat {
    JSON_EMOTECRAFT("json"),
    JSON_MC_ANIM("json"),
    QUARK("emote"),
    BINARY("emotecraft"),
    SERVER(null),
    UNKNOWN(null);

    private static final Map<String, AnimationFormat> FORMATS;

    static {
        AnimationFormat[] formatsValues = values();

        FORMATS = new HashMap<>(formatsValues.length);

        for (AnimationFormat format : formatsValues) {
            if (format.extension != null)
                FORMATS.putIfAbsent(format.extension, format);
        }
    }
    private final String extension;

    AnimationFormat(String extension) {
        this.extension = extension;
    }
}