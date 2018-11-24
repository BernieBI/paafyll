package no.hiof.matsl.pfyll.model;

import java.util.ArrayList;

public class UserList {

    private String name;
    private ArrayList<String> products;
    private String id;
    public UserList(){ }

    public UserList(String name, ArrayList<String> products ) {
        this.name = name;
        this.products = products;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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


