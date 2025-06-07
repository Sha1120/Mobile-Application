
package entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table (name = "movie_category")
public class Category implements Serializable{

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "name", length = 45, nullable = false)
    private String name;
    @Column(name = "img_path",length = 100 ,nullable = false)
    private String img_path;
    
    public Category(){
    
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the img_path
     */
    public String getImg_path() {
        return img_path;
    }

    /**
     * @param img_path the img_path to set
     */
    public void setImg_path(String img_path) {
        this.img_path = img_path;
    }
    
    

    
}
