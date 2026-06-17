package com.example.chess.api;

public class endPoints {

    private String PORT = "8000";
    private String IP = "192.168.31.229";
    private String URL = "http://"+this.IP+":"+this.PORT;
    private String WS_URL = "ws://"+this.IP+":"+this.PORT;
    //authorisation
    private String LOGIN = "/mobile_api/app_functional/login";
    private String REGISTER = "/mobile_api/app_functional/register";
    //friends
    private String GET_FRIENDS = "/mobile_api/app_functional/friends/get";
    private String ADD_FRIEND = "/mobile_api/app_functional/friends/add";
    private String DELETE_FRIEND = "/mobile_api/app_functional/friends/delete";
    private String SEND_IVITE = "/mobile_api/app_functional/friends/send_invite";
    //requests
    private String GET_REQUESTS = "/mobile_api/app_functional/requests/get";
    private String CANCEL_REQUEST = "/mobile_api/app_functional/requests/cancel";
    private String APROOVE_REQUEST = "/mobile_api/app_functional/requests/aproove";
    //search game
    private String SEARCH_ROOM = "/mobile_api/search/game_start/";
    private String GAME_SESSION = "/mobile_api/session/";
    private String ACTIVE_GAME = "/mobile_api/app_functional/active_game";

    //url
    public void setPORT(String port){
        this.PORT = port;
    }
    public void setIP(String ip){
        this.IP = ip;
    }
    public String getURL(){return this.URL;}
    public String getWS_URL(){return this.WS_URL;}
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
    public String getSEARCH_ROOM(){return this.SEARCH_ROOM;}
    public String getGAME_SESSION(){return this.GAME_SESSION;}
    public String getACTIVE_GAME(){return this.ACTIVE_GAME;}
}
