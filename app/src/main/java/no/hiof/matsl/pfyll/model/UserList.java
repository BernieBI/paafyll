package no.hiof.matsl.pfyll.model;

import java.util.ArrayList;

public class UserList {

    private String navn;
    private ArrayList<String> products;
    private String id;
    public UserList(){ }

    public UserList(String navn, String id, ArrayList<String> products ) {
        this.navn = navn;
        this.id = id;
        this.products = products;
    }

    public String getNavn() {
        return navn;
    }

    public void setNavn(String navn) {
        this.navn = navn;
    }

    public ArrayList<String> getProducts() {
        return products;
    }

    public void setProducts(ArrayList<String> products) {
        this.products = products;
    }

    public void addProduct(String product) {
        products.add(product);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}


