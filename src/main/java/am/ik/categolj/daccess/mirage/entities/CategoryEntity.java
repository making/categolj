package am.ik.categolj.daccess.mirage.entities;

import java.io.Serializable;

import clojure.lang.Named;

import jp.sf.amateras.mirage.annotation.PrimaryKey;
import jp.sf.amateras.mirage.annotation.Table;


@SuppressWarnings("serial")
@Table(name = "Category")
public class CategoryEntity implements Serializable, Named{
    @PrimaryKey
    public Long id;
    public String name;
    public Long index;

    public CategoryEntity() {
    }

    public CategoryEntity(Long id, String name, Long index) {
        super();
        this.id = id;
        this.name = name;
        this.index = index;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    @Override
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Long getIndex() {
        return index;
    }
    public void setIndex(Long index) {
        this.index = index;
    }

    @Override
    public String getNamespace() {
        return "am.ik.categolj.daccess.mirage.entities";
    }
}
