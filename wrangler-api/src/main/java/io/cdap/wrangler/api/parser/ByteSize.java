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
 * ByteSize token to parse and store string values like "10KB", "2MB", etc.
 * It converts them into canonical byte values and implements the Token interface.
 */
@PublicEvolving
public class ByteSize implements Token {
  private String value;
  private long bytes;

  public ByteSize(String value) {
    this.value = value;
    this.bytes = parseBytes(value);
  }

  private long parseBytes(String val) {
    val = val.trim().toUpperCase();
    if (val.endsWith("KB")) {
      return (long) (Double.parseDouble(val.replace("KB", "")) * 1024);
    } else if (val.endsWith("MB")) {
      return (long) (Double.parseDouble(val.replace("MB", "")) * 1024 * 1024);
    } else if (val.endsWith("GB")) {
      return (long) (Double.parseDouble(val.replace("GB", "")) * 1024 * 1024 * 1024);
    } else if (val.endsWith("TB")) {
      return (long) (Double.parseDouble(val.replace("TB", "")) * 1024L * 1024L * 1024L * 1024L);
    } else if (val.endsWith("B")) {
      return (long) (Double.parseDouble(val.replace("B", "")));
    }  else {
      throw new IllegalArgumentException("Invalid ByteSize format: " + val);
    }
  }

  public long getBytes() {
    return bytes;
  }

  @Override
  public Object value() {
    return value;
  }

  @Override
  public TokenType type() {
    return TokenType.BYTE_SIZE;
  }

  @Override
  public JsonElement toJson() {
    JsonObject object = new JsonObject();
    object.addProperty("type", TokenType.BYTE_SIZE.name());
    object.addProperty("value", value);
    return object;
  }
}
