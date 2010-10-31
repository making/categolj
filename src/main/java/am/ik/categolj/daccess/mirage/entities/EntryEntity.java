package am.ik.categolj.daccess.mirage.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import jp.sf.amateras.mirage.annotation.PrimaryKey;
import jp.sf.amateras.mirage.annotation.Table;

@SuppressWarnings("serial")
@Table(name = "Entity")
public class EntryEntity implements Serializable {
    @PrimaryKey
    public Long id;
    public String title;
    public String content;
    public Date createdAt;
    public Date updatedAt;
    public Set<CategoryEntity> categories;

    // TODO category

    public EntryEntity() {
    }

    public EntryEntity(Long id, String title, String content, Date createdAt,
            Date updatedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Set<CategoryEntity> getCategories() {
        return categories;
    }

    public void setCategories(Set<CategoryEntity> categories) {
        this.categories = categories;
    }

}
