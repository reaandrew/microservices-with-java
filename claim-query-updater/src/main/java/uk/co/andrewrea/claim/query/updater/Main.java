package uk.co.andrewrea.claim.query.updater;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.cli.*;
import uk.co.andrewrea.claim.query.infrastructure.mongo.MongoClaimQueryService;
import uk.co.andrewrea.claim.query.updater.config.ClaimQueryUpdaterServiceConfiguration;
import uk.co.andrewrea.claim.query.updater.services.ClaimQueryUpdaterHttpService;

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
            ClaimQueryUpdaterServiceConfiguration config = new ClaimQueryUpdaterServiceConfiguration();
            if (cmd.hasOption("c")) {
                String configurationFilePath = cmd.getOptionValue("c");
                YamlReader reader = new YamlReader(new FileReader(configurationFilePath));
                config = reader.read(ClaimQueryUpdaterServiceConfiguration.class);
            }

            MongoClient mongoClient = new MongoClient(config.mongoDbHost,config.mongoDbPort);
            MongoDatabase db = mongoClient.getDatabase(config.mongoDatabaseName);
            MongoClaimQueryService claimService = new MongoClaimQueryService(db, config.mongoClaimQueryCollectionName);

            ClaimQueryUpdaterHttpService claimRegistrationHttpService = new ClaimQueryUpdaterHttpService(config, claimService);
            claimRegistrationHttpService.start();

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
