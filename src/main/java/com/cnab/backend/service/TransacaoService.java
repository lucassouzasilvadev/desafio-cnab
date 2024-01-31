package com.cnab.backend.service;

import com.cnab.backend.entity.TipoTransacao;
import com.cnab.backend.entity.Transacao;
import com.cnab.backend.entity.TransacaoReport;
import com.cnab.backend.repository.TransacaoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Service
public class TransacaoService {
    private final TransacaoRepository repository;

    public TransacaoService(TransacaoRepository repository){
        this.repository = repository;
    }

    public List<TransacaoReport> totalTransacoesPorNomeDaLoja(){
        var transacoes = repository.findAllByOrderByNomeDaLojaAscIdDesc();

        var reportMap = new LinkedHashMap<String, TransacaoReport>();
        transacoes.forEach(transacao -> {
            String nomeDaLoja = transacao.nomeDaLoja();
            BigDecimal valor = transacao.valor();

            reportMap.compute(nomeDaLoja, (key, existingReport) -> {
                var report = (existingReport != null) ? existingReport : new TransacaoReport(key, BigDecimal.ZERO, new ArrayList<>());
                return  report.addTotal(valor).addTransacao(transacao);
            });
        });
        return new ArrayList<>(reportMap.values());
    }
}
