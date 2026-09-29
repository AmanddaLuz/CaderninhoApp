package com.caderninho.app.ui.screens.clientes;

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
public final class ClientesViewModel_Factory implements Factory<ClientesViewModel> {
  private final Provider<CaderninhoRepository> repositoryProvider;

  public ClientesViewModel_Factory(Provider<CaderninhoRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public ClientesViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static ClientesViewModel_Factory create(
      Provider<CaderninhoRepository> repositoryProvider) {
    return new ClientesViewModel_Factory(repositoryProvider);
  }

  public static ClientesViewModel newInstance(CaderninhoRepository repository) {
    return new ClientesViewModel(repository);
  }
}
