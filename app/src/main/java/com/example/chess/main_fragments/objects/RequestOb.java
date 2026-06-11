package com.example.chess.main_fragments.objects;

public class RequestOb {
    private Integer id;
    private String name;
    private String type;
    public RequestOb(Integer id, String name, String type){
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public int getId(){return this.id;}

    public String getName() {return name;}

    public String getType(){return type;}

    public void setId(Integer id) {
        this.id = id;
    }
    public void setName(String name){
        this.name = name;
    }
    public void setType(String name){
        this.type = type;
    }
}
