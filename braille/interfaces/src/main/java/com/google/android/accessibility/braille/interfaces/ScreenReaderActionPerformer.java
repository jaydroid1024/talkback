/*
 * Copyright (C) 2023 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.android.accessibility.braille.interfaces;

/**
 * An interface that defines screen reader executable actions and provides a method for performing
 * the actions.
 */
public interface ScreenReaderActionPerformer {

  /** Actions forwarded to screen reader to perform. */
  enum ScreenReaderAction {
    NEXT_ITEM,
    PREVIOUS_ITEM,
    NEXT_LINE,
    PREVIOUS_LINE,
    NEXT_WINDOW,
    PREVIOUS_WINDOW,
    SCROLL_FORWARD,
    SCROLL_BACKWARD,
    NAVIGATE_TO_TOP,
    NAVIGATE_TO_BOTTOM,
    NAVIGATE_BY_READING_GRANULARITY_OR_ADJUST_READING_CONTROL_BACKWARD,
    NAVIGATE_BY_READING_GRANULARITY_OR_ADJUST_READING_CONTROL_FORWARD,
    CLICK_CURRENT,
    CLICK_NODE,
    LONG_CLICK_CURRENT,
    LONG_CLICK_NODE,
    PREVIOUS_READING_CONTROL,
    NEXT_READING_CONTROL,
    SCREEN_SEARCH,
    OPEN_TALKBACK_MENU,
    TOGGLE_VOICE_FEEDBACK,
    GLOBAL_HOME,
    GLOBAL_BACK,
    GLOBAL_RECENTS,
    GLOBAL_NOTIFICATIONS,
    GLOBAL_QUICK_SETTINGS,
    GLOBAL_ALL_APPS,
    WEB_NEXT_HEADING,
    WEB_PREVIOUS_HEADING,
    WEB_NEXT_CONTROL,
    WEB_PREVIOUS_CONTROL,
    WEB_NEXT_LINK,
    WEB_PREVIOUS_LINK,
    NEXT_HEADING,
    PREVIOUS_HEADING,
    NEXT_CONTROL,
    PREVIOUS_CONTROL,
    NEXT_LINK,
    PREVIOUS_LINK,
    ACCESSIBILITY_FOCUS,
    FOCUS_NEXT_CHARACTER,
    FOCUS_PREVIOUS_CHARACTER,
    FOCUS_NEXT_WORD,
    FOCUS_PREVIOUS_WORD,
    FOCUS_NEXT_LINE,
    FOCUS_PREVIOUS_LINE,
    STOP_READING,
    CUT,
    COPY,
    PASTE,
    SELECT_PREVIOUS_CHARACTER,
    SELECT_NEXT_CHARACTER,
    SELECT_PREVIOUS_WORD,
    SELECT_NEXT_WORD,
    SELECT_PREVIOUS_LINE,
    SELECT_NEXT_LINE,
    SELECT_CURRENT_TO_START,
    SELECT_CURRENT_TO_END,
    TYPO_CORRECT,
    PLAY_PAUSE_MEDIA
  }

  /** Performs the action associated with Talkback. */
  boolean performAction(ScreenReaderAction action, int inputMode, Object... args);
}
