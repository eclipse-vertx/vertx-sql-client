/*
 *
 *  Copyright (c) 2021 Vladimir Vishnevsky
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Eclipse Public License 2.0 which is available at
 *  http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 *  which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package io.vertx.clickhousenativeclient.alltypes;

import io.vertx.clickhouse.clickhousenative.impl.codec.columns.DateTimeColumnReader;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.junit.runner.RunWith;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

@RunWith(VertxUnitRunner.class)
public class DateTime64Test extends AllTypesBase<OffsetDateTime> {
  public DateTime64Test() {
    super("datetime64", new MyColumnChecker<>(OffsetDateTime.class, Tuple::getOffsetDateTime, Row::getOffsetDateTime, Tuple::getArrayOfOffsetDateTimes, Row::getArrayOfOffsetDateTimes), false);
  }

  @Override
  public List<Tuple> createBatch() {
    ZoneId zoneId = ZoneId.of("Europe/Oslo");
    OffsetDateTime v1 = Instant.ofEpochSecond(1617120094L, 300).atZone(zoneId).toOffsetDateTime();
    OffsetDateTime v2 = Instant.ofEpochSecond(1617120094L + 10L, 400).atZone(zoneId).toOffsetDateTime();
    OffsetDateTime v3 = Instant.ofEpochSecond(1617120094L + 20L, 500).atZone(zoneId).toOffsetDateTime();
    OffsetDateTime v4 = Instant.ofEpochSecond(1617120094L - 10L, 600).atZone(zoneId).toOffsetDateTime();
    OffsetDateTime v5 = Instant.ofEpochSecond(1617120094L - 20L, 700).atZone(zoneId).toOffsetDateTime();
    OffsetDateTime v6 = Instant.ofEpochSecond(1617120094L - 200L, 800).atZone(zoneId).toOffsetDateTime();
    OffsetDateTime nv = Instant.ofEpochSecond(0, 0).atZone(zoneId).toOffsetDateTime();
    OffsetDateTime mn = Instant.ofEpochSecond(0, 0).atZone(zoneId).toOffsetDateTime();
    OffsetDateTime mx = Instant.ofEpochSecond(DateTimeColumnReader.MAX_EPOCH_SECOND, 999).atZone(zoneId).toOffsetDateTime();

    return Arrays.asList(
      //            id    simple_t    nullable_t   array_t                                                   array3_t                                                                                                                        nullable_array_t                                                nullable_array3_t
      Tuple.of((byte)1,        mn,      mn,        new OffsetDateTime[]{mn, mn},                             new OffsetDateTime[][][]{{{mn, mn}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new OffsetDateTime[]{mn, mn},                                   new OffsetDateTime[][][]{{{mn, null, mn}, {mn, mn, null}, {null}}, {{mn, mn}, {mn, mn}, {}}, {{}, {null}, {}}}                   ),
      Tuple.of((byte)2,        mn,      mn,        new OffsetDateTime[]{mn, mn},                             new OffsetDateTime[][][]{{{mn, mn}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new OffsetDateTime[]{mn, mn},                                   new OffsetDateTime[][][]{{{mn, null, mn, null}, {mn, mn, null}, {null}}, {{mn, mn}, {mn, mn}, {}}, {{}, {null}, {}}}             ),
      Tuple.of((byte)3,        mn,      mn,        new OffsetDateTime[]{mn, mn},                             new OffsetDateTime[][][]{{{mn, mn}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new OffsetDateTime[]{mn, null, mn},                             new OffsetDateTime[][][]{{{mn, mn, null}, {mn, mn, null}, {null}}, {{mn, mn}, {mn, mn}, {}}, {{}, {null}, {}}}                   ),
      Tuple.of((byte)4,        mn,      mn,        new OffsetDateTime[]{mn, mn},                             new OffsetDateTime[][][]{{{mn, mn}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new OffsetDateTime[]{mn, null, mn},                             new OffsetDateTime[][][]{{{mn, null, mn}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {null}, {}}}                             ),
      Tuple.of((byte)5,        mx,      mx,        new OffsetDateTime[]{mx, mx},                             new OffsetDateTime[][][]{{{mx, mx}, {mx, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new OffsetDateTime[]{mx, mx},                                   new OffsetDateTime[][][]{{{mx, null, mx}, {mx, mx, null}, {}}, {{mn, mn}, {mn, null, mn}, {}}, {{}, {null}, {}}}                 ),
      Tuple.of((byte)6,        mn,      mn,        new OffsetDateTime[]{mx, mx},                             new OffsetDateTime[][][]{{{mx, mx}, {mx, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new OffsetDateTime[]{mx, mx},                                   new OffsetDateTime[][][]{{{mx, mx}, {mx, null, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{null}, {}, {}}}                             ),
      Tuple.of((byte)7,        mx,      mx,        new OffsetDateTime[]{mx, mx},                             new OffsetDateTime[][][]{{{mx, mx}, {mx, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new OffsetDateTime[]{mx, null, mx},                             new OffsetDateTime[][][]{{{mx, mx}, {mx, null, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}                                 ),
      Tuple.of((byte)8,        mn,      mn,        new OffsetDateTime[]{mx, mx},                             new OffsetDateTime[][][]{{{mx, mx}, {mx, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new OffsetDateTime[]{mx, null, mx},                             new OffsetDateTime[][][]{{{mx, mx}, {mx, null, mx, null}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {null}, {}}}                       ),
      Tuple.of((byte)9,        mx,      mx,        new OffsetDateTime[]{mn, mx},                             new OffsetDateTime[][][]{{{mn, mx}, {mn, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new OffsetDateTime[]{mn, null, mx},                             new OffsetDateTime[][][]{{{mn, mx, null}, {mn, null, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}                           ),
      Tuple.of((byte)10,       mn,      mn,        new OffsetDateTime[]{mn, mx},                             new OffsetDateTime[][][]{{{mn, mx}, {mn, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new OffsetDateTime[]{mn, null, mx},                             new OffsetDateTime[][][]{{{mn, mx, null}, {mn, null, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}                           ),
      Tuple.of((byte)11,       v2,      v3,        new OffsetDateTime[]{},                                   new OffsetDateTime[][][]{{{}, {}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                                 new OffsetDateTime[]{},                                         new OffsetDateTime[][][]{{{}, {}, {}}, {{}, {}, {}}, {{}, {}, {}}}                                                               ),
      Tuple.of((byte)12,       v2,      v3,        new OffsetDateTime[]{},                                   new OffsetDateTime[][][]{{{}, {}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                                 new OffsetDateTime[]{},                                         new OffsetDateTime[][][]{{{}, {}, {}}, {{}, {}, {}}, {{}, {}, {}}}                                                               ),
      Tuple.of((byte)13,       v2,      v3,        new OffsetDateTime[]{},                                   new OffsetDateTime[][][]{{{}}},                                                                                                 new OffsetDateTime[]{},                                         new OffsetDateTime[][][]{{{}}}                                                                                                   ),
      Tuple.of((byte)14,       v2,      v3,        new OffsetDateTime[]{},                                   new OffsetDateTime[][][]{{{}}},                                                                                                 new OffsetDateTime[]{},                                         new OffsetDateTime[][][]{{{}}}                                                                                                   ),
      Tuple.of((byte)15,       v2,      v3,        new OffsetDateTime[]{},                                   new OffsetDateTime[][][]{{{}}},                                                                                                 new OffsetDateTime[]{null},                                     new OffsetDateTime[][][]{{{null}}}                                                                                               ),
      Tuple.of((byte)16,       v2,      v3,        new OffsetDateTime[]{},                                   new OffsetDateTime[][][]{{{}}},                                                                                                 new OffsetDateTime[]{null},                                     new OffsetDateTime[][][]{{{null}}}                                                                                               ),
      Tuple.of((byte)17,       v2,      v3,        new OffsetDateTime[]{nv},                                 new OffsetDateTime[][][]{{{nv, nv}, {nv, nv}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new OffsetDateTime[]{nv},                                       new OffsetDateTime[][][]{{{nv, nv}, {nv, nv}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}                                       ),
      Tuple.of((byte)18,       v2,      v3,        new OffsetDateTime[]{nv},                                 new OffsetDateTime[][][]{{{nv, nv}, {nv, nv}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new OffsetDateTime[]{nv},                                       new OffsetDateTime[][][]{{{nv, nv}, {nv, nv}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}                                       ),
      Tuple.of((byte)19,       v2,      v3,        new OffsetDateTime[]{nv, mn, mx},                         new OffsetDateTime[][][]{{{v2, nv, mn}, {v3, mn, nv, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                         new OffsetDateTime[]{nv, mn, null, mx},                         new OffsetDateTime[][][]{{{v2, nv, mn, null}, {v3, mn, nv, mx}, {}}, {{mn, mn, null}, {mn, mn}, {}}, {{}, {}, {}}}               ),
      Tuple.of((byte)20,       v2,      v3,        new OffsetDateTime[]{nv, mn, mx},                         new OffsetDateTime[][][]{{{v2, nv, mn}, {v3, mn, nv, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                         new OffsetDateTime[]{nv, mn, null, mx},                         new OffsetDateTime[][][]{{{v2, nv, mn, null}, {v3, mn, nv, mx}, {}}, {{mn, mn, null}, {mn, mn}, {}}, {{}, {}, {}}}               ),
      Tuple.of((byte)21,       v2,      v3,        new OffsetDateTime[]{nv, mn, mx},                         new OffsetDateTime[][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},             new OffsetDateTime[]{nv, mn, null, mx},                         new OffsetDateTime[][][]{{{nv, mn, mx, v1, v2, null, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}         ),
      Tuple.of((byte)22,       v2,      v3,        new OffsetDateTime[]{nv, mn, mx},                         new OffsetDateTime[][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},             new OffsetDateTime[]{nv, mn, null, mx},                         new OffsetDateTime[][][]{{{nv, mn, mx, v1, v2, v3, null, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}         ),
      Tuple.of((byte)23, nv,    null,        new OffsetDateTime[]{v3, v1, nv, mx, v4},                 new OffsetDateTime[][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},             new OffsetDateTime[]{v3, v1, null, nv, mx, v3},                 new OffsetDateTime[][][]{{{nv, mn, mx, v1, v2, v3, v4, v5, null}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}         ),
      Tuple.of((byte)24, nv,    null,        new OffsetDateTime[]{v3, v1, nv, mx, v4},                 new OffsetDateTime[][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},             new OffsetDateTime[]{v3, v1, null, nv, mx, v3},                 new OffsetDateTime[][][]{{{nv, mn, mx, null, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}         ),
      Tuple.of((byte)25,       v1,      v1,        new OffsetDateTime[]{v1, nv, nv},                         new OffsetDateTime[][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},             new OffsetDateTime[]{v3, nv, null},                             new OffsetDateTime[][][]{{{nv, mn, null, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}         ),
      Tuple.of((byte)26,       nv,      nv,        new OffsetDateTime[]{nv, nv, nv},                         new OffsetDateTime[][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},             new OffsetDateTime[]{nv, null, v4},                             new OffsetDateTime[][][]{{{nv, null, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}         ),
      Tuple.of((byte)27,       v6,      v5,        new OffsetDateTime[]{v4, nv, nv},                         new OffsetDateTime[][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},             new OffsetDateTime[]{v3, nv, null},                             new OffsetDateTime[][][]{{{nv, mn, null, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}         ),
      Tuple.of((byte)28,       v6,      v5,        new OffsetDateTime[]{v1, nv, mn, mx, v2, v3, v4, v5, v6}, new OffsetDateTime[][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},             new OffsetDateTime[]{v1, nv, mn, mx, v2, v3, v4, v5, v6, null}, new OffsetDateTime[][][]{{{nv, mn, mx, v1, null, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}         ),
      Tuple.of((byte)29,       v6,      v5,        new OffsetDateTime[]{v1, nv, mn, mx, v2, v3, v4, v5, v6}, new OffsetDateTime[][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},             new OffsetDateTime[]{v1, nv, mn, mx, v2, v3, v4, v5, v6, null}, new OffsetDateTime[][][]{{{nv, mn, mx, v1, v2, null, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}         )
    );
  }
}
