package com.vlkan.log4j2.logstash.layout;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.nio.BufferOverflowException;


public class LogStashLayoutGeneratorStateTest {
    private static final Configuration CONFIGURATION = new DefaultConfiguration();
    /**
     * Eventually stack overflow error will be thrown due to jason generator state corruption after
     * any non {@link com.fasterxml.jackson.core.JsonGenerationException}
     * This occurs when  EmptyPropertyExclusion Enabled  and using thread locals
     */
    @Test
    public void testGeneratorStateCorruptedAfterUnhandledException() {
        LogstashLayout layout = LogstashLayout
                .newBuilder()
                .setConfiguration(CONFIGURATION)
                .setEventTemplateUri("classpath:LogstashTestLayout.json")
                .setStackTraceEnabled(true)
                .setLocationInfoEnabled(true)
                .setEmptyPropertyExclusionEnabled(true)
                .build();
        LogEvent hugeMessageLogEvent = LogEventFixture.createHugeMeessagLogEvent(512 * 1024);
        LogEvent fullEvent = LogEventFixture.createFullLogEvents(1).get(0);
        long counter = 0;
        encodeSafe(layout, fullEvent);
        Assertions.assertThatThrownBy(()->layout.toByteArray(hugeMessageLogEvent)).hasCauseInstanceOf(BufferOverflowException.class);
        long startTime = System.currentTimeMillis();
        while(true){
            encodeSafe(layout, fullEvent);
            long l = ++counter;
            if(System.currentTimeMillis()-startTime>=1000){
                System.out.println(l);
                startTime=System.currentTimeMillis();
            }
        }

    }

    private void encodeSafe(LogstashLayout layout, LogEvent fullEvent) {
        try {
            layout.toByteArray(fullEvent);
        }
        catch (Exception e){
            //do nothing
        }
    }
}
