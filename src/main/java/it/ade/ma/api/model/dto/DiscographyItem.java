package it.ade.ma.api.model.dto;

import it.ade.ma.api.model.Band;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DiscographyItem {

    Band band;
    AlbumDiff albumDiff;

}
