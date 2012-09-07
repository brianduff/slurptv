package org.dubh.slurptv;

import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class TaskExecutorModule extends AbstractModule {
	@Override
  protected void configure() {
		Multibinder<Service> serviceBinder = Multibinder.newSetBinder(binder(), Service.class);
		serviceBinder.addBinding().to(TaskExecutor.class);
  }
}
