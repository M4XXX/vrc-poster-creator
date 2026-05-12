package dev.vrcposter;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApplicationControllerExportImportTest {

    @Test
    void exportRosterMethodExistsWithCorrectParameters() throws Exception {
        Method m = ApplicationController.class.getMethod(
                "exportRoster", Path.class, Runnable.class, Runnable.class);
        assertEquals(void.class, m.getReturnType());
    }

    @Test
    void importRosterMethodExistsWithCorrectParameters() throws Exception {
        Method m = ApplicationController.class.getMethod(
                "importRoster", Path.class, Consumer.class, Runnable.class);
        assertEquals(void.class, m.getReturnType());
    }
}
