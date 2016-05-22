package uk.co.andrewrea.registration.infrastructure.rabbitmq;

/**
 * Created by vagrant on 5/11/16.
 */
public class ConsumingServiceQueueName {

    private String targetService;
    private String consumingService;

    public ConsumingServiceQueueName(String targetService, String consumingService){
        this.targetService = targetService;
        this.consumingService = consumingService;
    }

    @Override
    public String toString(){
        return String.format("%s.%s", this.targetService, this.consumingService);
    }
}
