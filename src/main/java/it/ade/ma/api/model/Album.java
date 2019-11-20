package it.ade.ma.api.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@NoArgsConstructor
public class Album {

    public enum AlbumStatus {
        NONE,
        MISSED,
        PRESENT,
        PRESENT_WITH_COVER;
    }

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

    @Enumerated
    private AlbumStatus status = AlbumStatus.NONE;

    private String maType;
    private Integer maTypeCount;
    private String maName;

    public Album(Integer position, String type, Integer typeCount, String name, Integer year) {
        this.position = position;
        this.type = type;
        this.typeCount = typeCount;
        this.name = name;
        this.year = year;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Album album = (Album) o;

        if (type != null ? !type.equals(album.type) : album.type != null) return false;
        if (typeCount != null ? !typeCount.equals(album.typeCount) : album.typeCount != null) return false;
        if (name != null ? !name.equals(album.name) : album.name != null) return false;
        return year.equals(album.year);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (typeCount != null ? typeCount.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + year.hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder toString = new StringBuilder(String.format("%03d", position)).append(" ");

        if (maType != null) {
            toString.append(maType).append("*");
        } else if (type != null) {
            toString.append(type);
        }

        if (maTypeCount != null) {
            toString.append(String.format("%02d", maTypeCount)).append("*");
        } else if (typeCount != null) {
            toString.append(String.format("%02d", typeCount));
        }

        toString.append(" - ");

        if (maName != null) {
            toString.append(maName).append("*");
        } else if (name != null) {
            toString.append(name);
        }

        toString.append(" (").append(year).append(")");

        return toString.toString();
    }

}
