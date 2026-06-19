package com.example.chess.main_fragments.objects;

public class RequestOb {
    private Integer id;
    private String name;
    private String type;
    private String data;
    public RequestOb(Integer id, String name, String type, String data){
        this.id = id;
        this.name = name;
        this.type = type;
        this.data = data;
    }

    public int getId(){return this.id;}

    public String getName() {return name;}

    public String getType(){return type;}
    public String getData(){return type;}

    public void setId(Integer id) {
        this.id = id;
    }
    public void setName(String name){
        this.name = name;
    }
    public void setType(String type){
        this.type = type;
    }
    public void setData(String data){
        this.data = data;
    }
}
