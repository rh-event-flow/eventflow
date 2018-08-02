package io.streamzi.openshift;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Start the Flow Watcher
 */
public class Executor {

    private static Logger logger;

    private static final String FLOW_PREDICATE = "streamzi.io/kind=flow";

    public Executor() {
    }

    //Setup the logger nicely
    static {
        InputStream stream = Executor.class.getClassLoader().
                getResourceAsStream("logging.properties");
        try {
            LogManager.getLogManager().readConfiguration(stream);
            logger = Logger.getLogger(Executor.class.getName());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        logger.info("\uD83C\uDF0A Starting Flow Controller \uD83C\uDF0A");

        final FlowWatcher fw = new FlowWatcher(new FlowController(), FLOW_PREDICATE);

        final ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.submit(fw);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down");
            executor.shutdown();
            try {
                executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ie) {
                logger.severe("Error on close: " + ie.getMessage());
            }
        }));


    }

}
