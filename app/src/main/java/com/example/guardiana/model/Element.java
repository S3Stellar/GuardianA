package com.example.guardiana.model;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

public class Element {

    private String id;
    private String type;
    private String icon;
    private String name;
    private Boolean active;
    private Date createdTimestamp;
    private ElementCreator createdBy;
    private Location location;
    private Map<String, Object> elementAttribute;

    public Element() {
        // TODO Auto-generated constructor stub
    }

    // There are two elements which got created using the database which are: id and
    // created date
    public Element(String type, String icon, String name, Boolean active, ElementCreator createdBy,
                   Location location, Map<String, Object> elementAttribute) {
        super();
        this.type = type;
        this.icon = icon;
        this.name = name;
        this.active = active;
        this.createdBy = createdBy;
        this.location = location;
        this.elementAttribute = elementAttribute;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Date getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Date createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public ElementCreator getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(ElementCreator createdBy) {
        this.createdBy = createdBy;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Map<String, Object> getElementAttribute() {
        return elementAttribute;
    }

    public void setElementAttribute(Map<String, Object> elementAttribute) {
        this.elementAttribute = elementAttribute;
    }

    @Override
    public String toString() {
        return "Element{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", icon='" + icon + '\'' +
                ", name='" + name + '\'' +
                ", active=" + active +
                ", createdTimestamp=" + createdTimestamp +
                ", createdBy=" + createdBy +
                ", location=" + location +
                ", elementAttribute=" + elementAttribute +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Element)) return false;
        Element element = (Element) o;
        return Objects.equals(getId(), element.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
