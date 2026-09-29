package com.caderninho.app.di;

import com.caderninho.app.data.local.CaderninhoDatabase;
import com.caderninho.app.data.local.dao.VendaDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideVendaDaoFactory implements Factory<VendaDao> {
  private final Provider<CaderninhoDatabase> databaseProvider;

  public DatabaseModule_ProvideVendaDaoFactory(Provider<CaderninhoDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public VendaDao get() {
    return provideVendaDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideVendaDaoFactory create(
      Provider<CaderninhoDatabase> databaseProvider) {
    return new DatabaseModule_ProvideVendaDaoFactory(databaseProvider);
  }

  public static VendaDao provideVendaDao(CaderninhoDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideVendaDao(database));
  }
}
