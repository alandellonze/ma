package it.ade.ma.api.sevice.discography.model;

import it.ade.ma.api.sevice.db.model.dto.AlbumDTO;
import it.ade.ma.api.sevice.diff.model.DiffRow;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DiscographyItem {

    long bandId;
    DiffRow<AlbumDTO> diff;

}
