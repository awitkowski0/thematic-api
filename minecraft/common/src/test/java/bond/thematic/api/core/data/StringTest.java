package bond.thematic.api.core.data;

import bond.thematic.api.core.data.gson.GeckoLibSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class StringTest {

    @Test
    public void camelTest() {
        String str = "camel_case_string";
        String converted = "camelCaseString";
        Assertions.assertEquals(converted, GeckoLibSerializer.snake2Camel(str));
    }
}
