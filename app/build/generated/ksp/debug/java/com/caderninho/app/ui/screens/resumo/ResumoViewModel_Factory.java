package com.caderninho.app.ui.screens.resumo;

import com.caderninho.app.data.repository.CaderninhoRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class ResumoViewModel_Factory implements Factory<ResumoViewModel> {
  private final Provider<CaderninhoRepository> repositoryProvider;

  public ResumoViewModel_Factory(Provider<CaderninhoRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public ResumoViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static ResumoViewModel_Factory create(Provider<CaderninhoRepository> repositoryProvider) {
    return new ResumoViewModel_Factory(repositoryProvider);
  }

  public static ResumoViewModel newInstance(CaderninhoRepository repository) {
    return new ResumoViewModel(repository);
  }
}
