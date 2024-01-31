package com.cnab.backend.web;

import com.cnab.backend.entity.Transacao;
import com.cnab.backend.entity.TransacaoReport;
import com.cnab.backend.service.TransacaoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("transacoes")
public class TransacaoController {

    private final TransacaoService service;

    public TransacaoController(TransacaoService service){
        this.service = service;
    }

    @GetMapping
    List<TransacaoReport> listAll(){
        return service.totalTransacoesPorNomeDaLoja();
    }
}
