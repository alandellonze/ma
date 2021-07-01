package it.ade.ma.api.sevice.diff;

import com.google.common.collect.Lists;
import it.ade.ma.api.sevice.db.model.dto.AlbumDTO;
import it.ade.ma.api.sevice.diff.engine.AbstractDiffService;
import it.ade.ma.api.sevice.diff.engine.model.DiffResult;
import it.ade.ma.api.sevice.diff.engine.model.DiffRow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static it.ade.ma.api.sevice.diff.engine.model.DiffRow.DiffType.EQUAL;
import static it.ade.ma.api.sevice.diff.engine.model.DiffRow.DiffType.MINUS;

@Slf4j
@Component
public class AlbumDTODiffService extends AbstractDiffService<AlbumDTO> {

    @Override
    protected void minusAction(DiffResult<AlbumDTO> diffResult, List<AlbumDTO> original) {
        List<DiffRow<AlbumDTO>> diffs = diffResult.getDiffs();
        for (AlbumDTO o : original) {
            if (o.isFullyCustom()) {
                log.debug("  {}", o);
                diffs.add(new DiffRow<>(EQUAL, Lists.newArrayList(o), null));
            } else {
                log.debug("- {}", o);
                diffs.add(new DiffRow<>(MINUS, Lists.newArrayList(o), null));
                incrementCount(diffResult, 1);
            }
        }
    }

}
