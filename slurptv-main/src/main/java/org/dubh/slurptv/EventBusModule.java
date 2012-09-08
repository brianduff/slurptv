package org.dubh.slurptv;

import java.util.concurrent.Executors;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;

class EventBusModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(EventBus.class).toInstance(new AsyncEventBus(Executors.newSingleThreadExecutor()));
  }
}
