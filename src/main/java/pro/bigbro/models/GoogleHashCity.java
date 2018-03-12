package pro.bigbro.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class GoogleHashCity {
    @Id
    @GeneratedValue
    private int id;
    private int cityId;
    private String googleHash;

    public GoogleHashCity() {
    }

    public GoogleHashCity(int cityId, String googleHash) {
        this.cityId = cityId;
        this.googleHash = googleHash;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getGoogleHash() {
        return googleHash;
    }

    public void setGoogleHash(String googleHash) {
        this.googleHash = googleHash;
    }
}
