package io.streamzi.eventflow.runtime;

import eu.infomas.annotation.AnnotationDetector;
import io.streamzi.eventflow.annotations.CloudEventComponent;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class that starts everything going
 * @author hhiden
 */
public class ProcessorRunner implements Runnable {
    private static final Logger logger = Logger.getLogger(ProcessorRunner.class.getName());
    private List<ProcessorContainer> containers = new ArrayList<>();
    private volatile boolean stopFlag = false;
    
    public ProcessorRunner() {
    }
    
    
    public void stop(){
        stopFlag = true;
    }
    
    @Override
    public void run(){
        scanForComponents();
        startComponents();
        while(!stopFlag){
          try {
              Thread.sleep(100);
          } catch (Exception e){
              
          }
        }
        stopComponents();
    }
    
    private void scanForComponents(){
        try {
            AnnotationDetector.TypeReporter reporter = new AnnotationDetector.TypeReporter() {
                @Override
                public void reportTypeAnnotation(Class<? extends Annotation> annotation, String className) {
                    logger.info("Found processor class: " + className);
                    try {
                        final Class processorClass = Class.forName(className);
                        ProcessorContainer container = new ProcessorContainer(processorClass);
                        containers.add(container);
                    } catch (Exception e){
                        logger.log(Level.SEVERE, "Error creating processor class; " + e.getMessage(), e.getMessage());
                    }
                }

                @Override
                public Class<? extends Annotation>[] annotations() {
                    return new Class[]{CloudEventComponent.class};
                }
            };
            AnnotationDetector detector = new AnnotationDetector(reporter);
            detector.detect();            
        } catch (Exception e){
            logger.log(Level.SEVERE, "Exception", e);
        }
        
    }
    
    private void startComponents(){
        for(ProcessorContainer container : containers){
            container.startProcessor();
        }
    }
    
    private void stopComponents(){
        for(ProcessorContainer container : containers){
            container.stopProcessor();
        }
    }
    
    public static void main(String[] args){
        //Print all of the environment variable
        final Map<String, String> env = System.getenv();
        for(String key : env.keySet()){
            logger.info(key + "=" + env.get(key));
        }
        ProcessorRunner runner = new ProcessorRunner();
        new Thread(runner).start();
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.warning("SHUTDOWN");
            System.exit(0);
        }));                  
    }
}