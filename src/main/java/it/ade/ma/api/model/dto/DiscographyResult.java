package it.ade.ma.api.model.dto;

import it.ade.ma.api.model.Band;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DiscographyResult {

    Band band;
    List<AlbumDiff> albumDiffs;

}
