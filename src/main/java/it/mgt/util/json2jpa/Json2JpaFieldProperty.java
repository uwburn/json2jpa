package it.mgt.util.json2jpa;

import com.fasterxml.jackson.databind.JsonNode;

class Json2JpaFieldProperty {

    Json2JpaProperty property;
    JsonNode jsonNode;
    int index;

    public Json2JpaFieldProperty(Json2JpaProperty property, JsonNode jsonNode, int index) {
        this.property = property;
        this.jsonNode = jsonNode;
        this.index = index;
    }
}
