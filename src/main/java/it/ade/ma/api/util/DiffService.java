package it.ade.ma.api.util;

import com.github.difflib.DiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;
import com.google.common.collect.Lists;
import it.ade.ma.api.model.Album;
import it.ade.ma.api.model.dto.AlbumDiff;
import it.ade.ma.api.model.dto.AlbumDiff.DiffType;
import it.ade.ma.api.model.dto.DiscographyResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DiffService {

    public DiscographyResult execute(List<Album> original, List<Album> revised) throws DiffException {
        DiscographyResult discographyResult = new DiscographyResult();

        Patch<Album> patch = DiffUtils.diff(original, revised);
        List<AbstractDelta<Album>> deltas = patch.getDeltas();

        // if there are no differences
        if (deltas.size() == 0) {
            equalAction(discographyResult, original);
        }

        // otherwise, if there are differences
        else {
            // add the first not diff items
            AbstractDelta currentDelta = deltas.remove(0);
            Integer currentPosition = currentDelta.getSource().getPosition();
            if (currentPosition > 0) {
                equalAction(discographyResult, original.subList(0, currentPosition));
                getDeltaTextCustom(discographyResult, currentDelta);
            }

            // add the diff items
            for (AbstractDelta nextDelta : deltas) {
                int intermediateStart = currentPosition + currentDelta.getSource().getLines().size();
                equalAction(discographyResult, original.subList(intermediateStart, nextDelta.getSource().getPosition()));
                getDeltaTextCustom(discographyResult, nextDelta);

                currentDelta = nextDelta;
                currentPosition = nextDelta.getSource().getPosition();
            }

            // add the last not diff items
            int lastStart = currentPosition + currentDelta.getSource().getLines().size();
            if (lastStart < original.size()) {
                equalAction(discographyResult, original.subList(lastStart, original.size()));
            }
        }

        System.out.println("Changes found: " + discographyResult.getChanges());
        return discographyResult;
    }

    private void getDeltaTextCustom(DiscographyResult discographyResult, AbstractDelta delta) {
        // plus
        if (delta.getSource().getLines().size() == 0 && delta.getTarget().getLines().size() > 0) {
            plusAction(discographyResult, delta.getTarget().getLines());
        }

        // minus
        else if (delta.getSource().getLines().size() > 0 && delta.getTarget().getLines().size() == 0) {
            minusAction(discographyResult, delta.getSource().getLines());
        }

        // change
        else if (delta.getSource().getLines().size() > 0 && delta.getTarget().getLines().size() > 0) {
            changeAction(discographyResult, delta.getSource().getLines(), delta.getTarget().getLines());
        }
    }

    private void equalAction(DiscographyResult discographyResult, List<Album> original) {
        List<AlbumDiff> albumDiffs = discographyResult.getAlbumDiffs();
        for (Album album : original) {
            System.out.println("  " + album);
            albumDiffs.add(new AlbumDiff(DiffType.EQUAL, Lists.newArrayList(album), null));
        }
    }

    private void plusAction(DiscographyResult discographyResult, List<Album> revised) {
        List<AlbumDiff> albumDiffs = discographyResult.getAlbumDiffs();
        for (Album album : revised) {
            System.out.println("+ " + album);
            albumDiffs.add(new AlbumDiff(DiffType.PLUS, null, Lists.newArrayList(album)));
            incrementCount(discographyResult, 1);
        }
    }

    private void minusAction(DiscographyResult discographyResult, List<Album> original) {
        List<AlbumDiff> albumDiffs = discographyResult.getAlbumDiffs();
        for (Album album : original) {
            if (album.isFullyCustom()) {
                System.out.println("  " + album);
                albumDiffs.add(new AlbumDiff(DiffType.EQUAL, Lists.newArrayList(album), null));
            } else {
                System.out.println("- " + album);
                albumDiffs.add(new AlbumDiff(DiffType.MINUS, Lists.newArrayList(album), null));
                incrementCount(discographyResult, 1);
            }
        }
    }

    private void changeAction(DiscographyResult discographyResult, List<Album> original, List<Album> revised) {
        List<AlbumDiff> albumDiffs = discographyResult.getAlbumDiffs();
        for (Album line : original) {
            System.out.println("> " + line);
        }
        for (Album line : revised) {
            System.out.println("< " + line);
        }
        albumDiffs.add(new AlbumDiff(DiffType.CHANGE, original, revised));
        incrementCount(discographyResult, Math.max(original.size(), revised.size()));
    }

    private void incrementCount(DiscographyResult discographyResult, Integer value) {
        discographyResult.setChanges(discographyResult.getChanges() + value);
    }

}
