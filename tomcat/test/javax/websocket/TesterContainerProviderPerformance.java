package javax.websocket;

import java.util.function.IntConsumer;
import java.util.function.Supplier;

import org.junit.Test;

import org.apache.tomcat.unittest.TesterThreadedPerformance;

public class TesterContainerProviderPerformance {

    @Test
    public void testGetWebSocketContainer() throws Exception {
        for (int i = 1; i < 9; i++) {
            TesterThreadedPerformance test = new TesterThreadedPerformance(i, 250000, new TestInstanceSupplier());
            long duration = test.doTest();
            System.out.println(i + " threads completed in " + duration + "ns");
        }
    }


    private static class TestInstanceSupplier implements Supplier<IntConsumer> {
        @Override
        public IntConsumer get() {
            return new TestInstance();
        }
    }


    private static class TestInstance implements IntConsumer {
        @Override
        public void accept(int value) {
            ContainerProvider.getWebSocketContainer();
        }
    }
}