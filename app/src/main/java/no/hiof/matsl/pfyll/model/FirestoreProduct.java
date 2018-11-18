package no.hiof.matsl.pfyll.model;

import com.google.firebase.firestore.DocumentSnapshot;

public class FirestoreProduct extends Product {
    private DocumentSnapshot documentSnapshot;

    public FirestoreProduct(DocumentSnapshot documentSnapshot, Product product) {
        super(product);
        this.documentSnapshot = documentSnapshot;
    }

    public FirestoreProduct(DocumentSnapshot documentSnapshot) {
        this.documentSnapshot = documentSnapshot;
    }

    public DocumentSnapshot getDocumentSnapshot() {
        return documentSnapshot;
    }

    public static FirestoreProduct documentToProduct(DocumentSnapshot doc) {
        return new FirestoreProduct(doc, Product.documentToProduct(doc));
    }
}
