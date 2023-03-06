package com.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class WeatherServiceTest {

    /**
     * Unit test for the constructor of WeatherService class.
     * <p>Verifies that the constructor initializes the class
     * variables properly and creates a non-null filename. 
     */
    @Test
    public void testConstructor() {
        double latitude = 56.789;
        double longitude = 67.789;
        /* Delta is the maximum range of difference allowed in the result */
        double delta = 0.001;
        WeatherService weatherService = new WeatherService(latitude, longitude);
        assertNotNull(weatherService);
        assertEquals(latitude, weatherService.getLatitude(), delta);
        assertEquals(longitude, weatherService.getLongitude(), delta);
        assertNotNull(weatherService.getFilename());
    }
}