package it.ade.ma.api.sevice.diff.model;

import it.ade.ma.api.sevice.db.model.dto.AlbumDTO;
import it.ade.ma.api.sevice.db.model.dto.BandDTO;
import it.ade.ma.api.sevice.diff.engine.model.DiffResult;
import lombok.Data;

@Data
public class DiffResponse {

    private BandDTO bandDTO;
    private DiffResult<AlbumDTO> albumDiff;
    private DiffResult<String> coversDiff;
    private DiffResult<String> mp3Diff;
    private DiffResult<String> scansDiff;

}
