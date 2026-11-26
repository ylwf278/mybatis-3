/*
 *    Copyright 2009-2026 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.type;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

/**
 * Utility methods for eagerly materializing JDBC LOB values.
 */
final class JdbcLobUtils {

  private JdbcLobUtils() {
  }

  static <T> T readAndFree(SqlSupplier<T> reader, SqlRunnable free) throws SQLException {
    T value;
    try {
      value = reader.get();
    } catch (SQLException | RuntimeException | Error e) {
      try {
        free.run();
      } catch (Throwable cleanupException) {
        if (cleanupException != e) {
          e.addSuppressed(cleanupException);
        }
      }
      throw e;
    }
    try {
      free.run();
    } catch (SQLFeatureNotSupportedException ignored) {
      // Some JDBC drivers do not support freeing LOBs explicitly.
    }
    return value;
  }

  @FunctionalInterface
  interface SqlSupplier<T> {
    T get() throws SQLException;
  }

  @FunctionalInterface
  interface SqlRunnable {
    void run() throws SQLException;
  }

}
