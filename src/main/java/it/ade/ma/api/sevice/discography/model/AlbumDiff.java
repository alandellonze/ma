package it.ade.ma.api.sevice.discography.model;

import it.ade.ma.api.sevice.db.model.dto.AlbumDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlbumDiff {

    public enum DiffType {
        EQUAL,
        PLUS,
        MINUS,
        CHANGE
    }

    DiffType type;
    List<AlbumDTO> original;
    List<AlbumDTO> revised;

}
