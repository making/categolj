package am.ik.categolj.daccess.mirage.entities;

import java.io.Serializable;

import clojure.lang.Named;

import jp.sf.amateras.mirage.annotation.PrimaryKey;
import jp.sf.amateras.mirage.annotation.Table;


@SuppressWarnings("serial")
@Table(name = "User")
public class UserEntity implements Serializable, Named{
    @PrimaryKey
    public Long id;
    public String name;
    public String password;

    public UserEntity() {
    }

    public UserEntity(Long id, String name, String password) {
        super();
        this.id = id;
        this.name = name;
        this.password = password;
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
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getNamespace() {
        return "am.ik.categolj.daccess.mirage.entities";
    }
}
