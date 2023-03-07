package com.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Class that wraps functionality related to Weather Service.
 * <p>This class is initiated with two parameters: double latitude and double longitude.
 * The class creates an XML file with the given coordinates and then sends the file to
 * an external weather service using Curl.
 * <p>The class expects to receive an XML containing the temperature for the given coordinates.
 * <p>The class returns the temperature for the given coordinates with the method getTemperature()
 * <p>This class was created with help of ChatGPT.
 */
public class WeatherService {
    private double latitude;
    private double longitude;
    private String filename;
    private String weatherResponse;
    private int temperature;

    /**
     * Constructor for WeatherService class.
     * <p>Creates an XML file containing the given coordinates.
     * @param latitude double containing latitude coordinates
     * @param longitude double containins longitude coordinates
     */
    public WeatherService(double latitude, double longitude) {
        System.out.println("Status: Initializing WeatherService");
        this.latitude = latitude;
        this.longitude = longitude;
        this.temperature = -999;
        createWeatherCoordinatesFile();
    }

    /**
     * This method handles the sequence of events required to communicate with the
     * Weather Service API in order to get the temperature information.
     * <p>Deletes the created XML coordinate file when the sequence is over.
     */
    public void callWeatherAPI() {
        this.weatherResponse = sendWeatherCoordinates();
        if (weatherResponse != null) {
            if (weatherResponse.contains("weather")) {
                this.temperature = parseWeatherTemperature(weatherResponse);
                System.out.println("Status: Temperature: " + this.temperature + " Celcius");
            } else {
                System.out.println("Error: The received XML does not contain weather information");
            }
        } else {
            System.out.println("Status: Did not parse weather data since there was a problem with the connection to the weather service");
        }

        /* Delete the created XML since it is not needed any longer */
        deleteXMLCoordinateFile();
    }

    /**
     * Basic getter for int temperature class variable.
     * @return The class variable int temperature, 
     * which is the final output of the WeatherService object.
     * The value is -999 if the temperature operation was not successful
     */
    public int getTemperature() {
        return this.temperature;
    }

    public String getTemperatureString() {
        return this.temperature + " Celsius";
    }

    /**
     * Basic getter for the String filename class variable.
     * @return The filename of the file containing the XML
     * coordinates the class received as parameter.
     * Note that this file will be deleted after the method
     * callWatherAPI() has been called.
     */
    public String getFilename() {
        return this.filename;
    }

    /**
     * Basic getter that return the class variable latitude
     * @return double latitude
     */
    public double getLatitude() {
        return this.latitude;
    }

    /**
     * Basic getter that return the class variable longitude
     * @return double longitude
     */
    public double getLongitude() {
        return this.longitude;
    }

    /**
     * Method that creates an XML file containing the coordinates
     * the Class received as parameter.
     * <p>
     * The XML structure will be as follows:
     * <pre>{@code
     *<coordinates>
     *    <latitude>##.###</latitude>
     *    <longitude>##.###</longitude>
     *</coordinates>
     * }</pre>
     */
    private void createWeatherCoordinatesFile() {
        System.out.println("Status: Creating a XML file containing coordinates for weather service");
        try {
            /* Generate unique filename */
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmssSSS");
            this.filename = "weathercoordinates_" + dateFormat.format(new Date()) + ".xml";

            /* Create XML file */
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            /* Create root element "coordinates" */
            Element root = document.createElement("coordinates");
            document.appendChild(root);

            /* Create element "latitude" */
            Element latitude = document.createElement("latitude");
            latitude.appendChild(document.createTextNode(Double.toString(this.latitude)));
            root.appendChild(latitude);

            /* Create element "longitude" */
            Element longitude = document.createElement("longitude");
            longitude.appendChild(document.createTextNode(Double.toString(this.longitude)));
            root.appendChild(longitude);

            /* Write the content into XML file */
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(this.filename));
            transformer.transform(source, result);
            System.out.println("Success: File " + this.filename + " created");
            
        } catch (ParserConfigurationException | TransformerException e) {
            System.out.println("Error: XML file for weather coordinates could not be created");
            System.out.println(e.getMessage());
        }
    }

    /**
     * Method that forms a curl command with the filename of the XML
     * file that contains the coordinates.
     * <p>Uses that curl command to send the file to the Weather Service.
     * @return String that contains the response from the Weather Service.
     */
    private String sendWeatherCoordinates() {
        System.out.println("Status: Sending coordinates from file " + this.filename + " to the weather server");

        try {
            /* Build the command for curl */
            String curlCommand = "curl -k -d @./" + this.filename + " http://localhost:4001/weather -k -H Content-Type:application/xml -v";

            /* Execute the curl command as a separate process */
            Process process = Runtime.getRuntime().exec(curlCommand);

            /* Read the output of the process */
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }
            reader.close();

            /* Wait for the process to finish */
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Success: Weather coordinates sent to server");
            } else {
                System.out.println("Error: Failed to send weather coordinates to server");
                System.out.println("^^^ Error occured while waiting for the runtime process to end");
                return null;
            }

            /* Return the received response as a String */
            return responseBuilder.toString();
            
        } catch (IOException | InterruptedException | NullPointerException e) {
            System.out.println("Error: Failed to send weather coordinates to server");
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            System.out.println("Error: Failed to send weather coordinates to server");
            return null;
        }
    }

    /**
     * Method that takes the response String received from the 
     * Weather Service, and parses it to extract the temperature information.
     * @param response String received earlier from the Weather Service with curl.
     * @return Temperature as int, will be -999 if the operation failed.
     */
    private int parseWeatherTemperature(String response) {
        System.out.println("Status: Parsing weather temperature from response");
    
        try {
            /* Extract the temperature information */
            int startIndex = response.indexOf("<temperature>") + "<temperature>".length();
            int endIndex = response.indexOf("</temperature>");
            String temperatureStr = response.substring(startIndex, endIndex);
            int temperature = Integer.parseInt(temperatureStr);
            return temperature;
        } catch (Exception e) {
            System.out.println("Error: Failed to parse weather temperature from response");
            e.printStackTrace();
            return -999;
        }
    }

    /**
     * Method that deletes the created XML file.
     */
    private void deleteXMLCoordinateFile() {
        File file = new File(this.filename);
        if (file.delete()) {
            System.out.println("Status: The XML file containing coordinates was deleted, was no more required");
        } else {
            System.out.println("Error: Failed to delete the XML file containing coordinates");
        }
    }
}
