package am.ik.categolj.daccess.mirage.entities;

import java.io.Serializable;

import jp.sf.amateras.mirage.annotation.PrimaryKey;
import jp.sf.amateras.mirage.annotation.Table;


@SuppressWarnings("serial")
@Table(name = "Category")
public class CategoryEntity implements Serializable{
    @PrimaryKey
    public Long id;
    public String name;
    public Integer index;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Integer getIndex() {
        return index;
    }
    public void setIndex(Integer index) {
        this.index = index;
    }
}
