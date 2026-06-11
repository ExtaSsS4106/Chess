package com.example.chess.main_fragments.objects;

public class Friend {
    private Integer id;
    private String name;

    public Friend(Integer id, String name){
        this.id = id;
        this.name = name;
    }

    public int getId(){return this.id;}

    public String getName() {return name;}

    public void setId(Integer id) {
        this.id = id;
    }
    public void setName(String name){
        this.name = name;
    }
}
