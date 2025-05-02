package com.group25.photos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Photo implements Serializable {
    private static final long serialVersionUID = 1L;
    private String uri;
    private List<Tag> tags;

    public Photo(String uri) {
        this.uri = uri;
        this.tags = new ArrayList<>();
    }

    public Photo(Photo other) {
        this.uri = other.uri;
        this.tags = new ArrayList<>();
        for (Tag tag : other.tags) {
            this.tags.add(new Tag(tag.getType(), tag.getValue()));
        }
    }

    public String getUri() {
        return uri;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void addTag(Tag tag) {
        if (!this.tags.contains(tag)) {
            tags.add(tag);
        }
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Photo photo = (Photo) o;
        return Objects.equals(uri, photo.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri);
    }

    public static class Tag implements Serializable {
        private static final long serialVersionUID = 1L;
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
            return Objects.hash(type, value.toLowerCase());
        }
    }
} 