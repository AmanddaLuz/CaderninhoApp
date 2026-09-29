package com.caderninho.app.ui.screens.venda;

import androidx.lifecycle.SavedStateHandle;
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
public final class ClienteDetalheViewModel_Factory implements Factory<ClienteDetalheViewModel> {
  private final Provider<CaderninhoRepository> repositoryProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  public ClienteDetalheViewModel_Factory(Provider<CaderninhoRepository> repositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.repositoryProvider = repositoryProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public ClienteDetalheViewModel get() {
    return newInstance(repositoryProvider.get(), savedStateHandleProvider.get());
  }

  public static ClienteDetalheViewModel_Factory create(
      Provider<CaderninhoRepository> repositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new ClienteDetalheViewModel_Factory(repositoryProvider, savedStateHandleProvider);
  }

  public static ClienteDetalheViewModel newInstance(CaderninhoRepository repository,
      SavedStateHandle savedStateHandle) {
    return new ClienteDetalheViewModel(repository, savedStateHandle);
  }
}
