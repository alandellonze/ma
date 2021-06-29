package it.ade.ma.api.sevice.discography.model;

import it.ade.ma.api.sevice.db.model.Band;
import it.ade.ma.api.sevice.db.model.dto.AlbumDTO;
import it.ade.ma.api.sevice.diff.model.DiffRow;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Deprecated
@Data
@AllArgsConstructor
public class DiscographyResult {

    Band band;
    Integer changes;
    List<DiffRow<AlbumDTO>> albumDiffs;

}
