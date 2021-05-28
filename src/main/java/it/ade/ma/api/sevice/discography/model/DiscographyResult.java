package it.ade.ma.api.sevice.discography.model;

import com.google.common.collect.Lists;
import it.ade.ma.api.sevice.db.model.Band;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class DiscographyResult {

    Band band;
    Integer changes = 0;
    List<AlbumDiff> albumDiffs = Lists.newArrayList();

}
