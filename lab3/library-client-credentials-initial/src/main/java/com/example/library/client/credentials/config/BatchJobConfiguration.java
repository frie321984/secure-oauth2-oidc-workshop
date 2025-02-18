package com.example.library.client.credentials.config;

import com.example.library.client.credentials.batch.BookFieldSetMapper;
import com.example.library.client.credentials.batch.BookLineMapper;
import com.example.library.client.credentials.batch.WebClientItemWriter;
import com.example.library.client.credentials.web.BookResource;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class BatchJobConfiguration {

  private final WebClient webClient;

  @Value("${library.server}")
  private String libraryServer;

  public BatchJobConfiguration(WebClient webClient) {
    this.webClient = webClient;
  }

  @Bean
  public Job importBooksJob(PlatformTransactionManager transactionManager,  JobRepository jobRepository) {
    return new JobBuilder("importBooksJob", jobRepository).preventRestart()
                  .start(importStep(transactionManager, jobRepository))
                  .build();
    /*return this.jobBuilderFactory.get("importBooksJob").preventRestart()
                  .start(importStep(transactionManager))
                  .build();*/
  }

  @Bean
  public Step importStep(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
    return new StepBuilder("importStep", jobRepository)
            .<BookResource, BookResource>chunk(10, transactionManager)
            .reader(itemReader())
            .writer(itemWriter())
            .startLimit(1)
            .build();
  }

  @Bean
  public ItemWriter<BookResource> itemWriter() {
    return new WebClientItemWriter<>(this.webClient, libraryServer);
  }

  @Bean
  public ItemReader<BookResource> itemReader() {
    BookLineMapper bookLineMapper = new BookLineMapper();
    DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer(";");
    lineTokenizer.setNames("isbn", "title", "description", "authors");
    bookLineMapper.setLineTokenizer(lineTokenizer);
    bookLineMapper.setFieldSetMapper(new BookFieldSetMapper());

    FlatFileItemReader<BookResource> fileItemReader = new FlatFileItemReader<>();
    fileItemReader.setLineMapper(bookLineMapper);
    fileItemReader.setResource(new ClassPathResource("books.csv"));
    return fileItemReader;
  }

}
