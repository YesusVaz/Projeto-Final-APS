package br.ufpb.aps.granja.repository;

import java.nio.file.Path;

/** Tira uma copia consistente do banco inteiro num arquivo (UC28). */
public interface CopiadorDoBanco {

    void copiarPara(Path destino);
}
