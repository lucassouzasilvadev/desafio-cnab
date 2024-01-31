package com.cnab.backend.repository;

import com.cnab.backend.entity.Transacao;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TransacaoRepository extends CrudRepository<Transacao, Long> {
    List<Transacao> findAllByOrderByNomeDaLojaAscIdDesc();
}
