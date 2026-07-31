package com.streamlite.core;

import com.streamlite.settings.SettingsRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class AppModule_ProvideSettingsRepositoryFactory implements Factory<SettingsRepository> {
  private final Provider<SettingsRepository> repositoryProvider;

  private AppModule_ProvideSettingsRepositoryFactory(
      Provider<SettingsRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public SettingsRepository get() {
    return provideSettingsRepository(repositoryProvider.get());
  }

  public static AppModule_ProvideSettingsRepositoryFactory create(
      Provider<SettingsRepository> repositoryProvider) {
    return new AppModule_ProvideSettingsRepositoryFactory(repositoryProvider);
  }

  public static SettingsRepository provideSettingsRepository(SettingsRepository repository) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideSettingsRepository(repository));
  }
}
