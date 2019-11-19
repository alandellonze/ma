package it.ade.ma.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "band_id", nullable = false)
    private Band band;

    private Integer position;
    private String type;
    private Integer typeCount;
    private String name;
    private Integer year;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Album album = (Album) o;

        if (!type.equals(album.type)) return false;
        if (!typeCount.equals(album.typeCount)) return false;
        if (!name.equals(album.name)) return false;
        return year.equals(album.year);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + typeCount.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + year.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return new StringBuilder(String.format("%03d", position)).append(" ")
                .append(type).append(String.format("%02d", typeCount)).append(" - ")
                .append(name)
                .append(" (").append(year).append(")")
                .toString();
    }

}
