package com.cnab.backend;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.SQLOutput;

@Configuration
public class BatchConfig {
    private PlatformTransactionManager transactionManager;
    private JobRepository jobRepository;
    public BatchConfig(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        this.transactionManager = transactionManager;
        this.jobRepository = jobRepository;
    }

    @Bean
    Job job(Step step){
        return new JobBuilder("job", jobRepository)
                .start(step)
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    Step step(ItemReader<TransacaoCNAB> itemReader, ItemProcessor<TransacaoCNAB, Transacao> itemProcessor, ItemWriter<Transacao> itemWriter){
        return new StepBuilder("step", jobRepository)
                .<TransacaoCNAB, Transacao>chunk(1000, transactionManager)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();
    }

    @Bean
    FlatFileItemReader<TransacaoCNAB> reader(){
        return new FlatFileItemReaderBuilder<TransacaoCNAB>()
                .name("reader")
                .resource(new FileSystemResource("file\\CNAB.txt"))
                .fixedLength()
                .columns(
                    new Range(1, 1), new Range(2, 9),
                    new Range(10, 19), new Range(20, 30),
                    new Range(31, 42), new Range(43, 48),
                    new Range(49, 62), new Range(63, 80)
                )
                .names(
                     "tipo", "data", "valor", "cpf",
                    "cartao", "hora", "donoDaLoja", "nomeDaLoja"
                )
                .targetType(TransacaoCNAB.class)
                .build();
    }

    @Bean
    ItemProcessor<TransacaoCNAB, Transacao> processor(){
        return item -> {
          return new Transacao(null, item.tipo(), null, item.valor().divide(BigDecimal.valueOf(100)),
                                            item.cpf(), item.cartao(),null,
                                            item.donoDaLoja().trim(), item.nomeDaLoja().trim()
                  )
                  .withData(item.data())
                  .withHora(item.hora());
        };
    }

    @Bean
    JdbcBatchItemWriter<Transacao> writer(DataSource dataSource){
        return new JdbcBatchItemWriterBuilder<Transacao>()
                .dataSource(dataSource)
                .sql("""
                        INSERT INTO transacao(
                            tipo, data, valor, cpf, cartao, hora, dono_loja, nome_loja
                        ) VALUES (
                        :tipo, :data, :valor, :cpf, :cartao,
                        :hora, :donoDaLoja, :nomeDaLoja                 
                        )
                        """)
                .beanMapped().build();
    }
}

