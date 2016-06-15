package uk.co.andrewrea.claim.portal;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import org.apache.commons.cli.*;
import uk.co.andrewrea.claim.portal.config.ClaimPortalServiceConfiguration;
import uk.co.andrewrea.claim.portal.services.ClaimPortalHttpService;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Main {


    //TODO: Generate some test data which have the national insurance numbers but we dont ask for it in the UI
    public static void main(String[] args) {

        Options options = new Options();
        options.addOption("c", "config", true, "the yaml configuration file for the service");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            ClaimPortalServiceConfiguration config = new ClaimPortalServiceConfiguration();
            cmd = parser.parse(options, args);
            if (cmd.hasOption("c")) {
                String configurationFilePath = cmd.getOptionValue("c");
                YamlReader reader = new YamlReader(new FileReader(configurationFilePath));
                config = reader.read(ClaimPortalServiceConfiguration.class);
            }
            ClaimPortalHttpService claimPortalHttpService = new ClaimPortalHttpService(config);
            claimPortalHttpService.start();


        } catch (ParseException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (YamlException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
}
