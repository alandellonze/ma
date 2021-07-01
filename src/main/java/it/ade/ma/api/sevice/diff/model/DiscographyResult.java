package it.ade.ma.api.sevice.diff.model;

import it.ade.ma.api.sevice.db.model.dto.AlbumDTO;
import it.ade.ma.api.sevice.db.model.dto.BandDTO;
import it.ade.ma.api.sevice.diff.engine.model.DiffRow;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Deprecated
@Data
@AllArgsConstructor
public class DiscographyResult {

    private BandDTO bandDTO;
    private int changes;
    private List<DiffRow<AlbumDTO>> albumDiffs;

}
