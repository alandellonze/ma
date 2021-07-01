package it.ade.ma.api.sevice.diff.engine;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;
import com.google.common.collect.Lists;
import it.ade.ma.api.sevice.diff.engine.model.DiffResult;
import it.ade.ma.api.sevice.diff.engine.model.DiffRow;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.BiPredicate;

import static it.ade.ma.api.sevice.diff.engine.model.DiffRow.DiffType.*;

@Slf4j
public abstract class AbstractDiffService<T> {

    private final BiPredicate<T, T> DEFAULT_EQUALIZER = Object::equals;

    protected BiPredicate<T, T> getEqualizer() {
        return DEFAULT_EQUALIZER;
    }

    @SneakyThrows
    public DiffResult<T> execute(List<T> original, List<T> revised) {
        log.info("execute(\n original {}: {}\n revised  {}: {}\n)", original.size(), original, revised.size(), revised);

        DiffResult<T> diffResult = new DiffResult<>();

        // if there are no data from revisited
        if (revised.isEmpty()) {
            equalAction(diffResult, original);
        }

        // otherwise, compare the lists
        else {
            Patch<T> patch = DiffUtils.diff(original, revised, getEqualizer());
            List<AbstractDelta<T>> deltas = patch.getDeltas();

            // if there are no differences
            if (deltas.isEmpty()) {
                equalAction(diffResult, original);
            }

            // otherwise, if there are differences
            else {
                // add the first not diff items
                AbstractDelta<T> currentDelta = deltas.remove(0);
                int currentPosition = currentDelta.getSource().getPosition();
                if (currentPosition > 0) {
                    equalAction(diffResult, original.subList(0, currentPosition));
                }
                getDeltaTextCustom(diffResult, currentDelta);

                // add the diff items
                for (AbstractDelta<T> nextDelta : deltas) {
                    int intermediateStart = currentPosition + currentDelta.getSource().getLines().size();
                    equalAction(diffResult, original.subList(intermediateStart, nextDelta.getSource().getPosition()));
                    getDeltaTextCustom(diffResult, nextDelta);

                    currentDelta = nextDelta;
                    currentPosition = nextDelta.getSource().getPosition();
                }

                // add the last not diff items
                int lastStart = currentPosition + currentDelta.getSource().getLines().size();
                if (lastStart < original.size()) {
                    equalAction(diffResult, original.subList(lastStart, original.size()));
                }
            }
        }

        log.debug("changes found: {}", diffResult.getChanges());
        return diffResult;
    }

    protected void getDeltaTextCustom(DiffResult<T> diffResult, AbstractDelta<T> delta) {
        // plus
        if (delta.getSource().getLines().size() == 0 && delta.getTarget().getLines().size() > 0) {
            plusAction(diffResult, delta.getTarget().getLines());
        }

        // minus
        else if (delta.getSource().getLines().size() > 0 && delta.getTarget().getLines().size() == 0) {
            minusAction(diffResult, delta.getSource().getLines());
        }

        // change
        else if (delta.getSource().getLines().size() > 0 && delta.getTarget().getLines().size() > 0) {
            changeAction(diffResult, delta.getSource().getLines(), delta.getTarget().getLines());
        }
    }

    protected void equalAction(DiffResult<T> diffResult, List<T> original) {
        List<DiffRow<T>> diffs = diffResult.getDiffs();
        for (T o : original) {
            log.debug("  {}", o);
            diffs.add(new DiffRow<T>(EQUAL, Lists.newArrayList(o), null));
        }
    }

    protected void plusAction(DiffResult<T> diffResult, List<T> revised) {
        List<DiffRow<T>> diffs = diffResult.getDiffs();
        for (T r : revised) {
            log.debug("+ {}", r);
            diffs.add(new DiffRow<>(PLUS, null, Lists.newArrayList(r)));
            incrementCount(diffResult, 1);
        }
    }

    protected void minusAction(DiffResult<T> diffResult, List<T> original) {
        List<DiffRow<T>> diffs = diffResult.getDiffs();
        for (T o : original) {
            log.debug("- {}", o);
            diffs.add(new DiffRow<>(MINUS, Lists.newArrayList(o), null));
            incrementCount(diffResult, 1);
        }
    }

    protected void changeAction(DiffResult<T> diffResult, List<T> original, List<T> revised) {
        List<DiffRow<T>> diffs = diffResult.getDiffs();
        for (T o : original) {
            log.debug("> {}", o);
        }
        for (T r : revised) {
            log.debug("< {}", r);
        }
        diffs.add(new DiffRow<>(CHANGE, original, revised));
        incrementCount(diffResult, Math.max(original.size(), revised.size()));
    }

    protected void incrementCount(DiffResult<T> diffResult, Integer value) {
        diffResult.setChanges(diffResult.getChanges() + value);
    }

}
