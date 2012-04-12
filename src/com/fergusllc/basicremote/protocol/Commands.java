/*
 * Copyright (C) 2010 Google Inc.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fergusllc.basicremote.protocol;

import com.google.anymote.Key.Code;

/**
 * Utility class for building command wrappers.
 *
 */
public class Commands {
  private Commands() {
    // prevent instantiation
    throw new IllegalStateException();
  }

  /**
   * Builds key press command.
   *
   * @param key the pressed key
   */
  public static Command buildKeyPressCommand(final Code key) {
    return new Command() {
      public void execute(ICommandSender sender) {
        sender.keyPress(key);
      }
    };
  }
}
