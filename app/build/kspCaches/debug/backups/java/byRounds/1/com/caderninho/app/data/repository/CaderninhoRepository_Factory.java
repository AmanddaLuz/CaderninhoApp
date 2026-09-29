package com.caderninho.app.data.repository;

import com.caderninho.app.data.local.dao.ClienteDao;
import com.caderninho.app.data.local.dao.VendaDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class CaderninhoRepository_Factory implements Factory<CaderninhoRepository> {
  private final Provider<ClienteDao> clienteDaoProvider;

  private final Provider<VendaDao> vendaDaoProvider;

  public CaderninhoRepository_Factory(Provider<ClienteDao> clienteDaoProvider,
      Provider<VendaDao> vendaDaoProvider) {
    this.clienteDaoProvider = clienteDaoProvider;
    this.vendaDaoProvider = vendaDaoProvider;
  }

  @Override
  public CaderninhoRepository get() {
    return newInstance(clienteDaoProvider.get(), vendaDaoProvider.get());
  }

  public static CaderninhoRepository_Factory create(Provider<ClienteDao> clienteDaoProvider,
      Provider<VendaDao> vendaDaoProvider) {
    return new CaderninhoRepository_Factory(clienteDaoProvider, vendaDaoProvider);
  }

  public static CaderninhoRepository newInstance(ClienteDao clienteDao, VendaDao vendaDao) {
    return new CaderninhoRepository(clienteDao, vendaDao);
  }
}
