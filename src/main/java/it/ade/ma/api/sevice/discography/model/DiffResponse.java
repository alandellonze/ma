package it.ade.ma.api.sevice.discography.model;

import it.ade.ma.api.sevice.db.model.Band;
import it.ade.ma.api.sevice.db.model.dto.AlbumDTO;
import it.ade.ma.api.sevice.diff.model.DiffResult;
import lombok.Data;

@Data
public class DiffResponse {

    Band band;
    DiffResult<AlbumDTO> albumDiff;
    DiffResult<String> coversDiff;
    DiffResult<String> mp3Diff;
    DiffResult<String> scansDiff;

}
