/*
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * <nighteyes1993@gmail.com> wrote this file. As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return Poul-Henning Kamp
 * ----------------------------------------------------------------------------
 */

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.lang.reflect.Field;

/**
 * This depends on the external library https://github.com/jgilfelt/SystemBarTint
 * will make both the status bar and navigation bar translucent on KitKat and try
 * to automatically figure out the padding of the views.
 */
public final class TransluencyHelper {
  private TransluencyHelper() {
  }

  @TargetApi(Build.VERSION_CODES.KITKAT)
  public static void setupTransluency(final Activity activity, final View... views) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
      return;
    final Window win = activity.getWindow();
    final WindowManager.LayoutParams winParams = win.getAttributes();
    final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION|WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
    winParams.flags |= bits;
    win.setAttributes(winParams);
    final SystemBarTintManager tintManager = new SystemBarTintManager(activity);
    tintManager.setNavigationBarTintEnabled(true);
    tintManager.setStatusBarTintEnabled(true);

    final Resources.Theme theme = activity.getTheme();
    // We get the color of the actionbar here to set it as the statusbar tint resource
    TypedArray a = theme.obtainStyledAttributes(new int[]{android.R.attr.actionBarStyle});
    final int actionBarStyle = a.getResourceId(0, 0);
    a = theme.obtainStyledAttributes(actionBarStyle, new int[]{android.R.attr.background});
    tintManager.setStatusBarTintResource(a.getResourceId(0, 0));
    // We get the height of the tabview here
    a = theme.obtainStyledAttributes(new int[]{android.R.attr.actionBarTabBarStyle});
    final int actionBarTabStyle = a.getResourceId(0, 0);
    a = theme.obtainStyledAttributes(actionBarTabStyle, new int[]{android.R.attr.actionBarSize});
    int tabHeight = 0;
    if (activity.getActionBar().getNavigationMode() == ActionBar.NAVIGATION_MODE_TABS) {
      tabHeight = a.getDimensionPixelOffset(0, 0);
      try {
        Class<?> actionBarClass = activity.getActionBar().getClass();
        Field mHasEmbeddedTabs = actionBarClass.getDeclaredField("mHasEmbeddedTabs");
        mHasEmbeddedTabs.setAccessible(true);
        if (mHasEmbeddedTabs.getBoolean(activity.getActionBar()))
          tabHeight = 0;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    int bottom = 0;
    final View decor = win.getDecorView();
    final View actionBar = decor.findViewById(activity.getResources().getIdentifier("split_action_bar", "id", "android"));
    if (actionBar != null && actionBar.getVisibility() == View.VISIBLE && actionBar.willNotDraw() == false) {
      a = theme.obtainStyledAttributes(actionBarStyle, new int[]{android.R.attr.actionBarSize});
      bottom = a.getDimensionPixelOffset(0, 0);
    }

    final SystemBarTintManager.SystemBarConfig config = tintManager.getConfig();
    for (View view : views)
      view.setPadding(0, config.getStatusBarHeight() + config.getActionBarHeight() + tabHeight, config.getPixelInsetRight(), bottom);
  }
}
