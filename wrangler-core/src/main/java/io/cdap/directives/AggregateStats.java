/*
 * Copyright © 2025 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

 package io.cdap.directives;

 import io.cdap.cdap.api.annotation.Description;
 import io.cdap.cdap.api.annotation.Name;
 import io.cdap.cdap.api.annotation.Plugin;
 import io.cdap.wrangler.api.Arguments;
 import io.cdap.wrangler.api.Directive;
 import io.cdap.wrangler.api.DirectiveExecutionException;
 import io.cdap.wrangler.api.DirectiveParseException;
 import io.cdap.wrangler.api.ExecutorContext;
 import io.cdap.wrangler.api.Row;
 import io.cdap.wrangler.api.annotations.Categories;
 import io.cdap.wrangler.api.lineage.Lineage;
 import io.cdap.wrangler.api.lineage.Many;
 import io.cdap.wrangler.api.lineage.Mutation;
 import io.cdap.wrangler.api.parser.ByteSize;
 import io.cdap.wrangler.api.parser.ColumnName;
 import io.cdap.wrangler.api.parser.TimeDuration;
 import io.cdap.wrangler.api.parser.TokenType;
 import io.cdap.wrangler.api.parser.UsageDefinition;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * A directive to compute total byte size and time duration aggregates.
  * ByteSize and TimeDuration columns are aggregated into MB and seconds respectively.
  */
 @Plugin(type = Directive.TYPE)
 @Name("aggregate-stats")
 @Categories(categories = {"aggregate", "stats"})
 @Description("Aggregate total byte size and time duration values. " +
   "Supports BYTE_SIZE and TIME_DURATION parsing.")
 public class AggregateStats implements Directive, Lineage {
 
   public static final String NAME = "aggregate-stats";
 
   private String sourceSizeCol;
   private String sourceTimeCol;
   private String targetSizeCol;
   private String targetTimeCol;
 
   private long totalBytes = 0;
   private long totalMillis = 0;

   @Override
   public UsageDefinition define() {
     UsageDefinition.Builder builder = UsageDefinition.builder(NAME);
     builder.define("sourceSize", TokenType.COLUMN_NAME);
     builder.define("sourceTime", TokenType.COLUMN_NAME);
     builder.define("targetSize", TokenType.COLUMN_NAME);
     builder.define("targetTime", TokenType.COLUMN_NAME);
     return builder.build();
   }
 
   @Override
   public void initialize(Arguments args) throws DirectiveParseException {
     this.sourceSizeCol = ((ColumnName) args.value("sourceSize")).value();
     this.sourceTimeCol = ((ColumnName) args.value("sourceTime")).value();
     this.targetSizeCol = ((ColumnName) args.value("targetSize")).value();
     this.targetTimeCol = ((ColumnName) args.value("targetTime")).value();
   }
 
   @Override
   public void destroy() {
     // no-op
   }
 
   @Override
   public List<Row> execute(List<Row> rows, ExecutorContext context) throws DirectiveExecutionException {
    long totalBytes = 0;
    long totalMillis = 0;
     for (Row row : rows) {
       String sizeObj = row.getValue(sourceSizeCol).toString();
       String timeObj = row.getValue(sourceTimeCol).toString();
 
       ByteSize byteSize = new ByteSize(sizeObj);
       totalBytes += byteSize.getBytes();
 
       TimeDuration timeDuration = new TimeDuration(timeObj);
       totalMillis += timeDuration.getMilliseconds();
     }
 
     double totalSizeMB = totalBytes / (1024.0 * 1024.0);

     double totalSec = totalMillis / 1000.0;
     Row resultRow = new Row();
     resultRow.add(targetSizeCol, totalSizeMB);
     resultRow.add(targetTimeCol, totalSec);

     return List.of(resultRow);
   }
 
   @Override
   public Mutation lineage() {
     return Mutation.builder()
       .readable("Aggregated values from '%s' and '%s' into '%s' and '%s'",
         sourceSizeCol, sourceTimeCol, targetSizeCol, targetTimeCol)
       .relation(Many.columns(sourceSizeCol, sourceTimeCol), targetSizeCol)
       .relation(sourceSizeCol, sourceSizeCol)
       .relation(sourceTimeCol, sourceTimeCol)
       .build();
   }
}
