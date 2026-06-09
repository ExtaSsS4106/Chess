package com.example.chess.api;

public class endPoints {

    private String URL = "";
    private String LOGIN = "/mobile_api/app_functional/login";
    private String REGISTER = "/mobile_api/app_functional/register";


    public void setURL(String url){
        this.URL = url;
    }

    public String getURL(){return this.URL;}
    public String getLOGINPath(){return this.LOGIN;}
    public String getREGISTERPath(){return this.REGISTER;}

}
