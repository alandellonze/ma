package it.ade.ma.api.model.dto;

import it.ade.ma.api.model.Album;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AlbumDiff {

    public enum DiffType {
        EQUAL,
        PLUS,
        MINUS,
        CHANGE
    }

    DiffType type;
    List<Album> original;
    List<Album> revised;

}
