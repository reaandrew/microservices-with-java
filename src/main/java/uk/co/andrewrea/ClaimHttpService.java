package uk.co.andrewrea;

import spark.Service;

/**
 * Created by vagrant on 5/6/16.
 */
public class ClaimHttpService {

    private int port;
    private Service service;

    public ClaimHttpService(int port){
        this.port = port;
    }

    public void start(){
        Service http = Service.ignite().port(this.port);
        http.post("/claims", (q, a) -> "{\"status\":\"received\"}");
        http.get("/info",(req,res) -> {
            res.status(200);
            return "";
        } );
        http.get("/health",(req,res) -> {
            res.status(200);
            return "";
        } );
        http.get("/metrics",(req,res) -> {
            res.status(200);
            return "";
        } );
        this.service = http;
    }

    public void stop(){
        this.service.stop();
    }
}
