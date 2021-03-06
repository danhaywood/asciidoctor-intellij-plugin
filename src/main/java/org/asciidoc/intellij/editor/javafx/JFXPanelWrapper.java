package org.asciidoc.intellij.editor.javafx;

import com.intellij.ui.JreHiDpiUtil;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.util.FieldAccessor;
import com.sun.javafx.embed.EmbeddedSceneInterface;
import com.sun.javafx.tk.TKScene;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;

import java.awt.*;

public class JFXPanelWrapper extends JFXPanel {
  private static final FieldAccessor<JFXPanel, Integer> MY_SCALE_FACTOR_ACCESSOR = new FieldAccessor<>(JFXPanel.class, "scaleFactor");

  public JFXPanelWrapper() {
    Platform.setImplicitExit(false);
  }

  /**
   * This override fixes the situation of using multiple JFXPanels
   * with jbtabs/splitters when some of them are not showing.
   * On getMinimumSize there is no layout manager nor peer so
   * the result could be #size() which is incorrect.
   *
   * @return zero size
   */
  @Override
  public Dimension getMinimumSize() {
    return new Dimension(0, 0);
  }

  @Override
  public void addNotify() {
    super.addNotify();
    if (JreHiDpiUtil.isJreHiDPIEnabled()) {
      // JFXPanel is scaled asynchronously after first repaint, what may lead
      // to showing unscaled content. To work it around, set "scaleFactor" ahead.
      int scale = Math.round(JBUIScale.sysScale(this));
      MY_SCALE_FACTOR_ACCESSOR.set(this, scale);
      Scene scene = getScene();
      // If scene is null then it will be set later and super.setEmbeddedScene(..) will init its scale properly,
      // otherwise explicitly set scene scale to match JFXPanel.scaleFactor.
      if (scene != null) {
        try {
          // this will no longer work with JDK 11
          TKScene tks = scene.impl_getPeer();
          if (tks instanceof EmbeddedSceneInterface) {
            ((EmbeddedSceneInterface) tks).setPixelScaleFactor(scale);
          }
        } catch (NoSuchMethodError e) {
          // ignore
        }
      }
    }
  }
}
