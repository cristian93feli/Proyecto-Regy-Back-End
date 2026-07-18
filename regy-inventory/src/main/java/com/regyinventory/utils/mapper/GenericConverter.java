package com.regyinventory.utils.mapper;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GenericConverter implements IGenericConverter {

    private final ModelMapper modelMapper;

    @Override
    public <S, D> D convert(
            S source,
            Class<D> destinationType
    ) {
        if (source == null) {
            return null;
        }

        return modelMapper.map(source, destinationType);
    }

    @Override
    public <S, D> List<D> convertList(
            List<S> source,
            Class<D> destinationType
    ) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }

        return source.stream()
                .map(item -> modelMapper.map(item, destinationType))
                .toList();
    }

    @Override
    public <S, D> void map(
            S source,
            D destination
    ) {
        if (source == null || destination == null) {
            return;
        }

        modelMapper.map(source, destination);
    }
}