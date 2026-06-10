package com.example.chess.api;

public class endPoints {

    private String URL = "http://192.168.31.229:8000";
    //authorisation
    private String LOGIN = "/mobile_api/app_functional/login";
    private String REGISTER = "/mobile_api/app_functional/register";
    //friends
    private String GET_FRIENDS = "/mobile_api/app_functional/friends/get/";
    private String ADD_FRIEND = "/mobile_api/app_functional/friends/add/";
    private String DELETE_FRIEND = "/mobile_api/app_functional/friends/delete/";
    private String SEND_IVITE = "/mobile_api/app_functional/friends/send_invite/";
    //requests
    private String GET_REQUESTS = "/mobile_api/app_functional/requests/get/";
    private String CANCEL_REQUEST = "/mobile_api/app_functional/requests/cancel/";
    private String APROOVE_REQUEST = "/mobile_api/app_functional/requests/aproove/";

    //url
    public void setURL(String url){
        this.URL = url;
    }
    public String getURL(){return this.URL;}
    //authorisation
    public String getLOGINPath(){return this.LOGIN;}
    public String getREGISTERPath(){return this.REGISTER;}
    //friends
    public String getGET_FRIENDS(){return this.GET_FRIENDS;}
    public String getADD_FRIEND(){return this.ADD_FRIEND;}
    public String getDELETE_FRIEND(){return this.DELETE_FRIEND;}
    public String getSEND_IVITE(){return this.SEND_IVITE;}
    //requests
    public String getGET_REQUESTS(){return this.GET_REQUESTS;}
    public String getCANCEL_REQUEST(){return this.CANCEL_REQUEST;}
    public String getAPROOVE_REQUEST(){return this.APROOVE_REQUEST;}
}
