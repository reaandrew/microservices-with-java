package uk.co.andrewrea.claim.registration;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import org.apache.commons.cli.*;
import uk.co.andrewrea.claim.registration.config.ClaimRegistrationConfiguration;
import uk.co.andrewrea.claim.registration.services.ClaimRegistrationHttpService;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Main {
    public static void main(String[] args) {

        Options options = new Options();
        options.addOption("c", "config", true, "the yaml configuration file for the service");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
            if (cmd.hasOption("c")) {
                String configurationFilePath = cmd.getOptionValue("c");
                YamlReader reader = new YamlReader(new FileReader(configurationFilePath));
                ClaimRegistrationConfiguration config = reader.read(ClaimRegistrationConfiguration.class);
                ClaimRegistrationHttpService claimRegistrationHttpService = new ClaimRegistrationHttpService(config);
                claimRegistrationHttpService.start();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (YamlException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("GOT IT");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
