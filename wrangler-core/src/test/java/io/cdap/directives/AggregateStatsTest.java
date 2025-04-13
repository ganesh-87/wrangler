/*
 * Copyright © 2025 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

 package io.cdap.directives;

 import io.cdap.wrangler.TestingRig;
 import io.cdap.wrangler.api.Row;
 import org.junit.Assert;
 import org.junit.Test;

 import java.util.Arrays;
 import java.util.List;
 
 /**
  * Unit tests for {@link AggregateStats}
  */
 public class AggregateStatsTest {
 
   @Test
   public void testAggregateStats() throws Exception {
     List<Row> rows = Arrays.asList(
       new Row("data_transfer_size", "1024B").add("response_time", "1s"),
       new Row("data_transfer_size", "1TB").add("response_time", "1000min"),
       new Row("data_transfer_size", "0.5GB").add("response_time", "0.5s")
     );
 
     String[] recipe = new String[] {
       "aggregate-stats :data_transfer_size :response_time :total_size_mb :total_time_sec"
     };
 
     List<Row> results = TestingRig.execute(recipe, rows);
 
     Assert.assertEquals(rows.size(), results.size());
 
    double actualMB = 0;
    double actualSec = 0;

    for (Row result : results) {
      
      actualMB += (double) result.getValue("total_size_mb");
      actualSec += (double) result.getValue("total_time_sec");
    }
 
     // Expected conversions:
     // 10MB = 10 * 1024 * 1024 bytes
     // 5KB = 5 * 1024 bytes
     // 1.5GB = 1.5 * 1024 * 1024 * 1024 bytes
     // Convert all to MB
     double totalBytes = 1024 + 1 * 1024L * 1024L * 1024L * 1024L + 0.5 * 1024L * 1024L * 1024L;
     double expectedMB = totalBytes / (1024.0 * 1024.0);
 
     double expectedSec = 1.0 + 1000 * 60 + 0.5;
 
     Assert.assertEquals(expectedMB, actualMB, 0.001);
     Assert.assertEquals(expectedSec, actualSec, 0.001);
   }
}
