/*
 * Copyright © 2025 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.cdap.wrangler.api.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.cdap.wrangler.api.annotations.PublicEvolving;

/**
 * TimeDuration token to parse and store values like "150ms", "2s", "1min", etc.
 * It converts them into canonical milliseconds and implements the Token interface.
 */
@PublicEvolving
public class TimeDuration implements Token {
  private final String value;
  private final long milliseconds;

  public TimeDuration(String value) {
    this.value = value;
    this.milliseconds = parseMilliseconds(value);
  }

  private long parseMilliseconds(String val) {
    val = val.trim().toLowerCase();
    if (val.endsWith("ms")) {
      return (long) Double.parseDouble(val.replace("ms", ""));
    } else if (val.endsWith("s")) {
      return (long) (Double.parseDouble(val.replace("s", "")) * 1000);
    } else if (val.endsWith("min")) {
      return (long) (Double.parseDouble(val.replace("min", "")) * 60 * 1000);
    } else if (val.endsWith("h")) {
      return (long) (Double.parseDouble(val.replace("h", "")) * 3600 * 1000);
    } else {
      throw new IllegalArgumentException("Invalid TimeDuration format: " + val);
    }
  }

  /**
   * Returns the value in milliseconds.
   */
  public long getMilliseconds() {
    return milliseconds;
  }

  /**
   * Returns the original value string (e.g., "150ms").
   */
  @Override
  public Object value() {
    return value;
  }

  /**
   * Returns the TokenType for this class: TIME_DURATION.
   */
  @Override
  public TokenType type() {
    return TokenType.TIME_DURATION;
  }

  /**
   * Converts the token into a JSON representation.
   */
  @Override
  public JsonElement toJson() {
    JsonObject object = new JsonObject();
    object.addProperty("type", TokenType.TIME_DURATION.name());
    object.addProperty("value", value);
    return object;
  }
}
