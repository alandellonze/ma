package it.ade.ma.api.sevice.diff;

import it.ade.ma.api.sevice.diff.engine.AbstractDiffService;
import it.ade.ma.api.sevice.diff.engine.model.DiffResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class StringDiffService extends AbstractDiffService<String> {

    @Override
    protected void emptyAction(DiffResult<String> diffResult, List<String> original) {
        minusAction(diffResult, original);
    }

}
