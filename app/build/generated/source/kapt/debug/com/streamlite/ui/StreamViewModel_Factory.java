package com.streamlite.ui;

import com.streamlite.settings.SettingsRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class StreamViewModel_Factory implements Factory<StreamViewModel> {
  private final Provider<SettingsRepository> settingsProvider;

  private StreamViewModel_Factory(Provider<SettingsRepository> settingsProvider) {
    this.settingsProvider = settingsProvider;
  }

  @Override
  public StreamViewModel get() {
    return newInstance(settingsProvider.get());
  }

  public static StreamViewModel_Factory create(Provider<SettingsRepository> settingsProvider) {
    return new StreamViewModel_Factory(settingsProvider);
  }

  public static StreamViewModel newInstance(SettingsRepository settings) {
    return new StreamViewModel(settings);
  }
}
