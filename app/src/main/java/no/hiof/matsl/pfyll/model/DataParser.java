package no.hiof.matsl.pfyll.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataParser {

    private HashMap<String, String> getPlace(JSONObject googlePlaceJson){
        HashMap<String, String> googlePlacesMap = new HashMap<>();
        String placeName = "-NA-";
        String vicinity = "-Na-";
        String latitude = "";
        String longditude = "";
        String reference = "";
        try {
            if (!googlePlaceJson.isNull("name")) {
                placeName = googlePlaceJson.getString("name");
            }
            if(!googlePlaceJson.isNull("vicinity")){
                vicinity = googlePlaceJson.getString("vicinity");
            }
            latitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longditude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lng");

            googlePlacesMap.put("place_name", placeName);
            googlePlacesMap.put("vicinity", vicinity);
            googlePlacesMap.put("lat", latitude);
            googlePlacesMap.put("lng", longditude);
            googlePlacesMap.put("reference", reference);

            reference = googlePlaceJson.getString("reference");
        }catch (JSONException e) {
            e.printStackTrace();
        }
        return googlePlacesMap;

    }

    private List<HashMap<String,String>> getPlaces(JSONArray jsonArray){
        int count = jsonArray.length();
        List<HashMap<String,String>> placesList = new ArrayList<>();
        HashMap<String, String> placeMap = null;

        for(int i = 0; i<count; i++){
            try {
                placeMap = getPlace((JSONObject) jsonArray.get(i));
                placesList.add(placeMap);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return placesList;
    }

    public List<HashMap<String,String>> parse(String jsonData){
        JSONArray jsonArray = null;
        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(jsonData);
            jsonArray = jsonObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return getPlaces(jsonArray);
    }
}