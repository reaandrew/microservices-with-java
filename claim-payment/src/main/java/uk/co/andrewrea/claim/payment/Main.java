package uk.co.andrewrea.claim.payment;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import org.apache.commons.cli.*;
import uk.co.andrewrea.claim.payment.config.ClaimPaymentServiceConfiguration;
import uk.co.andrewrea.claim.payment.services.ClaimPaymentHttpService;

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
                ClaimPaymentServiceConfiguration config = reader.read(ClaimPaymentServiceConfiguration.class);
                ClaimPaymentHttpService claimFraudHttpService = new ClaimPaymentHttpService(config);
                claimFraudHttpService.start();
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
