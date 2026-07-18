package com.regyinventory.utils.mapper;

import java.util.List;

public interface IGenericConverter {

    <S, D> D convert(S source, Class<D> destinationType);

    <S, D> List<D> convertList(
            List<S> source,
            Class<D> destinationType
    );

    <S, D> void map(
            S source,
            D destination
    );
}