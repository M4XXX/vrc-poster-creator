package dev.vrcposter;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationControllerDeleteTest {

    @Test
    void deleteDjMethodExistsWithCorrectParameters() throws Exception {
        Method m = ApplicationController.class.getMethod(
                "deleteDj", String.class, String.class, Runnable.class, Runnable.class);
        assertEquals(void.class, m.getReturnType());
    }
}
