package it.ade.ma.api.model.dto;

import it.ade.ma.api.model.enums.AlbumStatus;
import it.ade.ma.api.model.enums.CoverStatus;
import it.ade.ma.api.model.enums.MP3Status;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AlbumDTO {

    Long id;

    Long bandId;
    String bandName;

    Integer position;
    String type;
    Integer typeCount;
    String name;
    Integer year;
    AlbumStatus status = AlbumStatus.NONE;

    String maType;
    Integer maTypeCount;
    String maName;

    MP3Status mp3Status = MP3Status.NOT_PRESENT;
    CoverStatus coverStatus = CoverStatus.NOT_PRESENT;

    public AlbumDTO(Integer position, String type, Integer typeCount, String name, Integer year) {
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

        AlbumDTO albumDTO = (AlbumDTO) o;

        if (type != null ? !type.equals(albumDTO.type) : albumDTO.type != null) return false;
        if (typeCount != null ? !typeCount.equals(albumDTO.typeCount) : albumDTO.typeCount != null) return false;
        if (name != null ? !name.equals(albumDTO.name) : albumDTO.name != null) return false;
        return year.equals(albumDTO.year);
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

    public boolean isFullyCustom() {
        return type == null && typeCount == null && name == null;
    }

}
