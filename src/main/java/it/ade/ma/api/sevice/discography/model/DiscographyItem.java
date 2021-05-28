package it.ade.ma.api.sevice.discography.model;

import it.ade.ma.api.sevice.db.model.Band;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DiscographyItem {

    Band band;
    AlbumDiff albumDiff;

}
