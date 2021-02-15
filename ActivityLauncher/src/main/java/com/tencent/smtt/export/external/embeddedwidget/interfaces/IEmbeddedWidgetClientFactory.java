package com.tencent.smtt.export.external.embeddedwidget.interfaces;

import java.util.Map;

public interface IEmbeddedWidgetClientFactory {
   IEmbeddedWidgetClient createWidgetClient(String var1, Map<String, String> var2, IEmbeddedWidget var3);
}
