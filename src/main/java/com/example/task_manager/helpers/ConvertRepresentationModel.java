package com.example.task_manager.helpers;

import java.util.List;
import java.util.function.Function;

import lombok.NonNull;
import org.springframework.hateoas.RepresentationModel;

public final class ConvertRepresentationModel {

    public static <M, R extends RepresentationModel<R>> @NonNull List<R> toList(
        @NonNull final List<M> models,
        @NonNull final Function<M, R> representationFactory
    ) {
        return models.stream()
            .map(representationFactory)
            .toList();
    }
}
