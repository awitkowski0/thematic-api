package bond.thematic.api.core.data;

import bond.thematic.api.layered.AnimationStack;
import bond.thematic.api.layered.IAnimation;
import bond.thematic.api.layered.ModifierLayer;
import bond.thematic.api.core.util.Pair;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;

public class LayerTest {

    @Test
    public void testLayers() throws NoSuchFieldException, IllegalAccessException {
        AnimationStack stack = new AnimationStack();
        Random random = new Random();
        for (int i = 0; i < 128; i++) {
            stack.addAnimLayer(random.nextInt()%10000, new ModifierLayer<>());
        }

        //This should not be accessible while using it as an API, but for the test, it is completely reasonable
        Field layersRef = AnimationStack.class.getDeclaredField("layers");
        layersRef.setAccessible(true);
        List<Pair<Integer, IAnimation>> layers = (List<Pair<Integer, IAnimation>>)layersRef.get(stack);

        int i = Integer.MIN_VALUE;
        for (Pair<Integer, IAnimation> layer : layers) {
            int n = layer.getLeft();
            if (n < i) {
                System.out.println(layers);
                throw new AssertionError("Layers are not in order");
            }
            i = n;
        }
    }
}
