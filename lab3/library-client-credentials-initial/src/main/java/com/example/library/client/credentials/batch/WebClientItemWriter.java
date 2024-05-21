package com.example.library.client.credentials.batch;

import com.example.library.client.credentials.web.BookResource;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.support.AbstractItemStreamItemWriter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

public class WebClientItemWriter<T> extends AbstractItemStreamItemWriter<T> {

  private final WebClient webClient;
  private final String targetUrl;

  public WebClientItemWriter(WebClient webClient, String targetUrl) {
    this.webClient = webClient;
    this.targetUrl = targetUrl;
  }

  @Override
  public void write(Chunk<? extends T> items) {
    items.forEach(
      item -> webClient.post().uri(targetUrl + "/books").bodyValue(item)
              .retrieve()
              .onStatus(
                      s -> s.value() == HttpStatus.UNAUTHORIZED.value(),
                      cr -> Mono.error(new BadCredentialsException("Not authenticated")))
              .onStatus(
                      s -> s.value() == HttpStatus.BAD_REQUEST.value(),
                      cr -> Mono.error(new IllegalArgumentException(HttpStatus.valueOf(cr.statusCode().value()).getReasonPhrase())))
              .onStatus(
                      HttpStatusCode::is5xxServerError,
                      cr -> Mono.error(new RuntimeException(HttpStatus.valueOf(cr.statusCode().value()).getReasonPhrase())))
              .bodyToMono(BookResource.class).log().block()
    );
  }
}
