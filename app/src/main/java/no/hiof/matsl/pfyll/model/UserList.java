package no.hiof.matsl.pfyll.model;

import java.util.ArrayList;

public class UserList {

    private String navn;
    private ArrayList<String> products;
    private String id;
    public UserList(){ }

    public UserList(String navn, ArrayList<String> products ) {
        this.navn = navn;
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

    public boolean addProduct(String product) {
        if (products.contains(product))
            return false;

        products.add(product);
        return true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}


