package no.hiof.matsl.pfyll.model;

import java.util.ArrayList;

public class UserList {

    private String navn;
    private ArrayList<Product> products;
    private String id;
    public UserList(){ }

    public UserList(String navn, String id) {
        this.navn = navn;
        this.id = id;
    }

    public String getNavn() {
        return navn;
    }

    public void setNavn(String navn) {
        this.navn = navn;
    }

    public ArrayList<Product> getProducts() {
        return products;
    }

    public void setProducts(ArrayList<Product> products) {
        this.products = products;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}


