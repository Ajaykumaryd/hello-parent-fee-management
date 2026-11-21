package com.helloparent.fees.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/** Stable, compact pagination envelope (Spring's Page JSON is verbose and not guaranteed stable). */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements,
                              int totalPages, boolean last) {

    public static <E, T> PageResponse<T> of(Page<E> page, Function<E, T> mapper) {
        return new PageResponse<>(page.getContent().stream().map(mapper).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages(), page.isLast());
    }
}
