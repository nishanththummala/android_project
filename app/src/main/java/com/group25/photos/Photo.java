package com.group25.photos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Photo implements Serializable {
    private String uri;
    private List<Tag> tags;

    public Photo(String uri) {
        this.uri = uri;
        this.tags = new ArrayList<>();
    }

    public String getUri() {
        return uri;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void addTag(Tag tag) {
        tags.add(tag);
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
    }

    public static class Tag implements Serializable {
        public enum Type {
            PERSON,
            LOCATION
        }

        private Type type;
        private String value;

        public Tag(Type type, String value) {
            this.type = type;
            this.value = value;
        }

        public Type getType() {
            return type;
        }

        public String getValue() {
            return value;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Tag tag = (Tag) obj;
            return type == tag.type && value.equalsIgnoreCase(tag.value);
        }

        @Override
        public int hashCode() {
            return 31 * type.hashCode() + value.toLowerCase().hashCode();
        }
    }
} 