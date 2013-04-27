package org.pvv.bcd.Util;

import java.awt.Component;
import java.awt.Rectangle;

public interface WindowOpener
{
   public void openWindow(
      Component parent,
      Component panel,
      String title,
      boolean modal,
      Rectangle bounds,
      boolean resizable,
      boolean closable,
      boolean maximizable,
      boolean iconifiable);

   public void openWindow(
      Component parent,
      Component panel,
      String title,
      boolean modal);

}
