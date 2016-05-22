package uk.co.andrewrea.claim.award;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import org.apache.commons.cli.*;
import uk.co.andrewrea.claim.award.config.ClaimAwardServiceConfiguration;
import uk.co.andrewrea.claim.award.services.ClaimAwardedHttpService;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Main {
    public static void main(String[] args)  {

        Options options = new Options();
        options.addOption("c", "config", true, "the yaml configuration file for the service");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse( options, args);
            if(cmd.hasOption("c")){
                String configurationFilePath = cmd.getOptionValue("c");
                YamlReader reader = new YamlReader(new FileReader(configurationFilePath));
                ClaimAwardServiceConfiguration config = reader.read(ClaimAwardServiceConfiguration.class);
                ClaimAwardedHttpService claimAwardedHttpService = new ClaimAwardedHttpService(config);
                claimAwardedHttpService.start();
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
            e.printStackTrace();
        }
    }
}
