package it.ade.ma.api.sevice.diff.model;

import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

@Data
public class DiffResult<T> {

    Integer changes = 0;
    List<DiffRow<T>> diffs = Lists.newArrayList();

}
