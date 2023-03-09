package com.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WeatherServiceTest {

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

    @Test
    public void testGetTemperature() {
        double latitude = 65.02718262105043;
        double longitude = 25.460310013354846;

        WeatherService weatherService = new WeatherService(latitude, longitude);
        weatherService.callWeatherAPI();
        int temperature = weatherService.getTemperature();

        /* Should pass. Check whether the temperature is in range */
        System.out.println("Test result: The temperature is: " + weatherService.getTemperatureString());
        assertTrue("Temperature is outside valid range", temperature >= -40 && temperature <= 40);
    }
}