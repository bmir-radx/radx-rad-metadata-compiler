package edu.stanford.bmir.radx.rad.metadata.compiler;

import java.net.URI;
import java.util.Optional;

public enum TemplateVersion {
  FIRST_VERSION(URI.create("https://repo.metadatacenter.org/templates/c691629c-1183-4425-9a12-26201eab1a10"));

  private URI id;

  TemplateVersion(URI id) {
    this.id = id;
  }

  public URI getId() {
    return id;
  }

  public static boolean isFirstVersion(Optional<URI> id){
    return id.isPresent() && id.get().equals(FIRST_VERSION.getId());
  }
}
