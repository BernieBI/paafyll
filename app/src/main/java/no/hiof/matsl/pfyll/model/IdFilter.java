package no.hiof.matsl.pfyll.model;

import java.util.List;

public class IdFilter {
    private List<String> ids;

    public IdFilter(List<String> ids) {
        this.ids = ids;
    }

    public List<String> getIds() {
        return ids;
    }
}
