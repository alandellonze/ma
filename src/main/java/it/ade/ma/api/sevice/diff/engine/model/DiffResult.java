package it.ade.ma.api.sevice.diff.engine.model;

import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

@Data
public class DiffResult<T> {

    private Integer changes = 0;
    private List<DiffRow<T>> diffs = Lists.newArrayList();

}
