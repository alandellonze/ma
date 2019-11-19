package it.ade.ma.api.util;

import com.github.difflib.DiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;
import com.google.common.collect.Lists;
import it.ade.ma.api.model.Album;
import it.ade.ma.api.model.dto.AlbumDiff;
import it.ade.ma.api.model.dto.AlbumDiff.DiffType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DiffService {

    public List<AlbumDiff> execute(List<Album> original, List<Album> revised) throws DiffException {
        List<AlbumDiff> albumDiffs = new ArrayList<>();

        Patch<Album> patch = DiffUtils.diff(original, revised);
        List<AbstractDelta<Album>> deltas = patch.getDeltas();

        // if there are no differences
        if (deltas.size() == 0) {
            equalAction(albumDiffs, original);
        }

        // otherwise, if there are differences
        else {
            // add the first not diff items
            AbstractDelta currentDelta = deltas.remove(0);
            Integer currentPosition = currentDelta.getSource().getPosition();
            if (currentPosition > 0) {
                equalAction(albumDiffs, original.subList(0, currentPosition));
                getDeltaTextCustom(albumDiffs, currentDelta);
            }

            // add the diff items
            for (AbstractDelta nextDelta : deltas) {
                int intermediateStart = currentPosition + currentDelta.getSource().getLines().size();
                equalAction(albumDiffs, original.subList(intermediateStart, nextDelta.getSource().getPosition()));
                getDeltaTextCustom(albumDiffs, nextDelta);

                currentDelta = nextDelta;
                currentPosition = nextDelta.getSource().getPosition();
            }

            // add the last not diff items
            int lastStart = currentPosition + currentDelta.getSource().getLines().size();
            if (lastStart < original.size()) {
                equalAction(albumDiffs, original.subList(lastStart, original.size()));
            }
        }

        return albumDiffs;
    }

    private void getDeltaTextCustom(List<AlbumDiff> albumDiffs, AbstractDelta delta) {
        // plus
        if (delta.getSource().getLines().size() == 0 && delta.getTarget().getLines().size() > 0) {
            plusAction(albumDiffs, delta.getTarget().getLines());
        }

        // minus
        else if (delta.getSource().getLines().size() > 0 && delta.getTarget().getLines().size() == 0) {
            minusAction(albumDiffs, delta.getSource().getLines());
        }

        // change
        else if (delta.getSource().getLines().size() > 0 && delta.getTarget().getLines().size() > 0) {
            changeAction(albumDiffs, delta.getSource().getLines(), delta.getTarget().getLines());
        }
    }

    private void equalAction(List<AlbumDiff> albumDiffs, List<Album> original) {
        for (Album album : original) {
            System.out.println("  " + album);
            albumDiffs.add(new AlbumDiff(DiffType.EQUAL, Lists.newArrayList(album), null));
        }
    }

    private void plusAction(List<AlbumDiff> albumDiffs, List<Album> revised) {
        for (Album album : revised) {
            System.out.println("+ " + album);
            albumDiffs.add(new AlbumDiff(DiffType.PLUS, null, Lists.newArrayList(album)));
        }
    }

    private void minusAction(List<AlbumDiff> albumDiffs, List<Album> original) {
        for (Album album : original) {
            System.out.println("- " + album);
            albumDiffs.add(new AlbumDiff(DiffType.MINUS, Lists.newArrayList(album), null));
        }
    }

    private void changeAction(List<AlbumDiff> albumDiffs, List<Album> original, List<Album> revised) {
        for (Album line : original) {
            System.out.println("> " + line);
        }
        for (Album line : revised) {
            System.out.println("< " + line);
        }
        albumDiffs.add(new AlbumDiff(DiffType.CHANGE, original, revised));
    }

}
