/*
 *
 *  * Copyright (c) 2021 Vladimir Vishnevsky
 *  *
 *  * This program and the accompanying materials are made available under the
 *  * terms of the Eclipse Public License 2.0 which is available at
 *  * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 *  * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *  *
 *  * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package io.vertx.clickhousenativeclient.alltypes;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

@RunWith(VertxUnitRunner.class)
public class FixedStringTest extends AllTypesBase<String> {
  public FixedStringTest() {
    super("fixedstring", new MyColumnChecker<>(String.class, Tuple::getString, Row::getString, Tuple::getArrayOfStrings, Row::getArrayOfStrings));
  }

  @Test
  public void testArrayDeduplication(TestContext ctx) {
    new StringArrayDeduplicationTester(tableName(), vertx, options).test(ctx);
  }

  @Override
  public List<Tuple> createBatch() {
    String v1 = "val1";
    String v2 = "val2";
    String v3 = "val3";
    String v4 = "val4";
    String v5 = "value5";
    String v6 = "v6";
    String nv = "";
    String mn = "";
    String mx = "123456789123";

    return Arrays.asList(
      //            id    simple_t    nullable_t   array_t                                           array3_t                                                                                                                nullable_array_t                                        nullable_array3_t                                                                                        simple_lc_t  nullable_lc_t   array_lc_t                                        array3_lc_t                                                                                                      nullable_array_lc_t                                      nullable_array3_lc_t
      Tuple.of((byte)1,        mn,      mn,        new String[]{mn, mn},                             new String[][][]{{{mn, mn}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new String[]{mn, mn},                                   new String[][][]{{{mn, null, mn}, {mn, mn, null}, {null}}, {{mn, mn}, {mn, mn}, {}}, {{}, {null}, {}}},           mn,        mn,      new String[]{mn, mn},                             new String[][][]{{{mn, mn}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                              new String[]{mn, mn},                                    new String[][][]{{{mn, null, mn}, {mn, mn, null}, {null}}, {{mn, mn}, {mn, mn}, {}}, {{}, {null}, {}}}            ),
      Tuple.of((byte)2,        mn,      mn,        new String[]{mn, mn},                             new String[][][]{{{mn, mn}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new String[]{mn, mn},                                   new String[][][]{{{mn, null, mn, null}, {mn, mn, null}, {null}}, {{mn, mn}, {mn, mn}, {}}, {{}, {null}, {}}},     mn,        mn,      new String[]{mn, mn},                             new String[][][]{{{mn, mn}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                              new String[]{mn, mn},                                    new String[][][]{{{mn, null, mn, null}, {mn, mn, null}, {null}}, {{mn, mn}, {mn, mn}, {}}, {{}, {null}, {}}}      ),
      Tuple.of((byte)3,        mn,      mn,        new String[]{mn, mn},                             new String[][][]{{{mn, mn}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new String[]{mn, null, mn},                             new String[][][]{{{mn, mn, null}, {mn, mn, null}, {null}}, {{mn, mn}, {mn, mn}, {}}, {{}, {null}, {}}},           mn,        mn,      new String[]{mn, mn},                             new String[][][]{{{mn, mn}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                              new String[]{mn, null, mn},                              new String[][][]{{{mn, mn, null}, {mn, mn, null}, {null}}, {{mn, mn}, {mn, mn}, {}}, {{}, {null}, {}}}            ),
      Tuple.of((byte)4,        mn,      mn,        new String[]{mn, mn},                             new String[][][]{{{mn, mn}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new String[]{mn, null, mn},                             new String[][][]{{{mn, null, mn}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {null}, {}}},                     mn,        mn,      new String[]{mn, mn},                             new String[][][]{{{mn, mn}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                              new String[]{mn, null, mn},                              new String[][][]{{{mn, null, mn}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {null}, {}}}                      ),
      Tuple.of((byte)5,        mx,      mx,        new String[]{mx, mx},                             new String[][][]{{{mx, mx}, {mx, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new String[]{mx, mx},                                   new String[][][]{{{mx, null, mx}, {mx, mx, null}, {}}, {{mn, mn}, {mn, null, mn}, {}}, {{}, {null}, {}}},         mx,        mx,      new String[]{mx, mx},                             new String[][][]{{{mx, mx}, {mx, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                              new String[]{mx, null, mx},                              new String[][][]{{{mx, null, mx}, {mx, mx, null}, {}}, {{mn, mn}, {mn, null, mn}, {}}, {{}, {null}, {}}}          ),
      Tuple.of((byte)6,        mn,      mn,        new String[]{mx, mx},                             new String[][][]{{{mx, mx}, {mx, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new String[]{mx, mx},                                   new String[][][]{{{mx, mx}, {mx, null, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{null}, {}, {}}},                     mn,        mn,      new String[]{mx, mx},                             new String[][][]{{{mx, mx}, {mx, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                              new String[]{mx, null, mx},                              new String[][][]{{{mx, mx}, {mx, null, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{null}, {}, {}}}                      ),
      Tuple.of((byte)7,        mx,      mx,        new String[]{mx, mx},                             new String[][][]{{{mx, mx}, {mx, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new String[]{mx, null, mx},                             new String[][][]{{{mx, mx}, {mx, null, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                         mx,        mx,      new String[]{mx, mx},                             new String[][][]{{{mx, mx}, {mx, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                              new String[]{mx, null, mx},                              new String[][][]{{{mx, mx}, {mx, null, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}                          ),
      Tuple.of((byte)8,        mn,      mn,        new String[]{mx, mx},                             new String[][][]{{{mx, mx}, {mx, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new String[]{mx, null, mx},                             new String[][][]{{{mx, mx}, {mx, null, mx, null}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {null}, {}}},               mn,        mn,      new String[]{mx, mx},                             new String[][][]{{{mx, mx}, {mx, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                              new String[]{mx, null, mx},                              new String[][][]{{{mx, mx}, {mx, null, mx, null}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {null}, {}}}                ),
      Tuple.of((byte)9,        mx,      mx,        new String[]{mn, mx},                             new String[][][]{{{mn, mx}, {mn, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new String[]{mn, null, mx},                             new String[][][]{{{mn, mx, null}, {mn, null, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                   mx,        mx,      new String[]{mn, mx},                             new String[][][]{{{mn, mx}, {mn, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                              new String[]{mn, null, mx},                              new String[][][]{{{mn, mx, null}, {mn, null, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}                    ),
      Tuple.of((byte)10,       mn,      mn,        new String[]{mn, mx},                             new String[][][]{{{mn, mx}, {mn, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new String[]{mn, null, mx},                             new String[][][]{{{mn, mx, null}, {mn, null, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                   mn,        mn,      new String[]{mn, mx},                             new String[][][]{{{mn, mx}, {mn, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                              new String[]{mn, null, mx},                              new String[][][]{{{mn, mx, null}, {mn, null, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}                    ),
      Tuple.of((byte)11,       v2,      v3,        new String[]{},                                   new String[][][]{{{}, {}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                                 new String[]{},                                         new String[][][]{{{}, {}, {}}, {{}, {}, {}}, {{}, {}, {}}},                                                       v2,        v3,      new String[]{},                                   new String[][][]{{{}, {}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                          new String[]{},                                          new String[][][]{{{}, {}, {}}, {{}, {}, {}}, {{}, {}, {}}}                                                        ),
      Tuple.of((byte)12,       v2,      v3,        new String[]{},                                   new String[][][]{{{}, {}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                                 new String[]{},                                         new String[][][]{{{}, {}, {}}, {{}, {}, {}}, {{}, {}, {}}},                                                       v2,        v3,      new String[]{},                                   new String[][][]{{{}, {}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                          new String[]{},                                          new String[][][]{{{}, {}, {}}, {{}, {}, {}}, {{}, {}, {}}}                                                        ),
      Tuple.of((byte)13,       v2,      v3,        new String[]{},                                   new String[][][]{{{}}},                                                                                                 new String[]{},                                         new String[][][]{{{}}},                                                                                           v2,        v3,      new String[]{},                                   new String[][][]{{{}}},                                                                                          new String[]{},                                          new String[][][]{{{}}}                                                                                            ),
      Tuple.of((byte)14,       v2,      v3,        new String[]{},                                   new String[][][]{{{}}},                                                                                                 new String[]{},                                         new String[][][]{{{}}},                                                                                           v2,        v3,      new String[]{},                                   new String[][][]{{{}}},                                                                                          new String[]{},                                          new String[][][]{{{}}}                                                                                            ),
      Tuple.of((byte)15,       v2,      v3,        new String[]{},                                   new String[][][]{{{}}},                                                                                                 new String[]{null},                                     new String[][][]{{{null}}},                                                                                       v2,        v3,      new String[]{},                                   new String[][][]{{{}}},                                                                                          new String[]{null},                                      new String[][][]{{{null}}}                                                                                            ),
      Tuple.of((byte)16,       v2,      v3,        new String[]{},                                   new String[][][]{{{}}},                                                                                                 new String[]{null},                                     new String[][][]{{{null}}},                                                                                       v2,        v3,      new String[]{},                                   new String[][][]{{{}}},                                                                                          new String[]{null},                                      new String[][][]{{{null}}}                                                                                            ),
      Tuple.of((byte)17,       v2,      v3,        new String[]{nv},                                 new String[][][]{{{nv, nv}, {nv, nv}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new String[]{nv},                                       new String[][][]{{{nv, nv}, {nv, nv}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                               v2,        v3,      new String[]{nv},                                 new String[][][]{{{nv, nv}, {nv, nv}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                              new String[]{nv},                                        new String[][][]{{{nv, nv}, {nv, nv}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}                                ),
      Tuple.of((byte)18,       v2,      v3,        new String[]{nv},                                 new String[][][]{{{nv, nv}, {nv, nv}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new String[]{nv},                                       new String[][][]{{{nv, nv}, {nv, nv}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                               v2,        v3,      new String[]{nv},                                 new String[][][]{{{nv, nv}, {nv, nv}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                              new String[]{nv},                                        new String[][][]{{{nv, nv}, {nv, nv}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}                                ),
      Tuple.of((byte)19,       v2,      v3,        new String[]{nv, mn, mx},                         new String[][][]{{{v2, nv, mn}, {v3, mn, nv, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                         new String[]{nv, mn, null, mx},                         new String[][][]{{{v2, nv, mn, null}, {v3, mn, nv, mx}, {}}, {{mn, mn, null}, {mn, mn}, {}}, {{}, {}, {}}},       v2,        v3,      new String[]{nv, mn, mx},                         new String[][][]{{{v2, nv, mn}, {v3, mn, nv, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                  new String[]{nv},                                        new String[][][]{{{v2, nv, mn, null}, {v3, mn, nv, mx}, {}}, {{mn, mn, null}, {mn, mn}, {}}, {{}, {}, {}}}        ),
      Tuple.of((byte)20,       v2,      v3,        new String[]{nv, mn, mx},                         new String[][][]{{{v2, nv, mn}, {v3, mn, nv, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                         new String[]{nv, mn, null, mx},                         new String[][][]{{{v2, nv, mn, null}, {v3, mn, nv, mx}, {}}, {{mn, mn, null}, {mn, mn}, {}}, {{}, {}, {}}},       v2,        v3,      new String[]{nv, mn, mx},                         new String[][][]{{{v2, nv, mn}, {v3, mn, nv, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                  new String[]{nv},                                        new String[][][]{{{v2, nv, mn, null}, {v3, mn, nv, mx}, {}}, {{mn, mn, null}, {mn, mn}, {}}, {{}, {}, {}}}        ),
      Tuple.of((byte)21,       v2,      v3,        new String[]{nv, mn, mx},                         new String[][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},             new String[]{nv, mn, null, mx},                         new String[][][]{{{nv, mn, mx, v1, v2, null, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}, v2,        v3,      new String[]{nv, mn, mx},                         new String[][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},      new String[]{nv, mn, null, mx},                          new String[][][]{{{nv, mn, mx, v1, v2, null, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}  ),
      Tuple.of((byte)22,       v2,      v3,        new String[]{nv, mn, mx},                         new String[][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},             new String[]{nv, mn, null, mx},                         new String[][][]{{{nv, mn, mx, v1, v2, v3, null, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}, v2,        v3,      new String[]{nv, mn, mx},                         new String[][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},      new String[]{nv, mn, null, mx},                          new String[][][]{{{nv, mn, mx, v1, v2, v3, null, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}  ),
      Tuple.of((byte)23, nv,    null,        new String[]{v3, v1, nv, mx, v4},                 new String[][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},             new String[]{v3, v1, null, nv, mx, v3},                 new String[][][]{{{nv, mn, mx, v1, v2, v3, v4, v5, null}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}, nv,      null,      new String[]{v3, v1, nv, mx, v4},                 new String[][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},      new String[]{nv, v2, null, v3, v2},                       new String[][][]{{{nv, mn, mx, v1, v2, v3, v4, v5, null}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}} ),
      Tuple.of((byte)24, nv,    null,        new String[]{v3, v1, nv, mx, v4},                 new String[][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},             new String[]{v3, v1, null, nv, mx, v3},                 new String[][][]{{{nv, mn, mx, null, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}, nv,      null,      new String[]{v3, v1, nv, mx, v4},                 new String[][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},      new String[]{nv, v2, null, v3, v2},                       new String[][][]{{{nv, mn, mx, null, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}} ),
      Tuple.of((byte)25,       v1,      v1,        new String[]{v1, nv, nv},                         new String[][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},             new String[]{v3, nv, null},                             new String[][][]{{{nv, mn, null, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}, v1,        v1,      new String[]{v1, nv, nv},                         new String[][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},      new String[]{v2, nv, null},                              new String[][][]{{{nv, mn, null, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}  ),
      Tuple.of((byte)26,       nv,      nv,        new String[]{nv, nv, nv},                         new String[][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},             new String[]{nv, null, v4},                             new String[][][]{{{nv, null, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}, nv,        nv,      new String[]{nv, nv, nv},                         new String[][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},      new String[]{nv, null, nv},                              new String[][][]{{{nv, null, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}  ),
      Tuple.of((byte)27,       v6,      v5,        new String[]{v4, nv, nv},                         new String[][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},             new String[]{v3, nv, null},                             new String[][][]{{{nv, mn, null, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}, v6,        v5,      new String[]{v4, nv, nv},                         new String[][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},      new String[]{v2, nv, null},                              new String[][][]{{{nv, mn, null, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}  ),
      Tuple.of((byte)28,       v6,      v5,        new String[]{v1, nv, mn, mx, v2, v3, v4, v5, v6}, new String[][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},             new String[]{v1, nv, mn, mx, v2, v3, v4, v5, v6, null}, new String[][][]{{{nv, mn, mx, v1, null, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}, v6,        v5,      new String[]{v1, nv, mn, mx, v2, v3, v4, v5, v6}, new String[][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},      new String[]{v1, nv, mn, mx, v2, v3, v4, v5, v6, null},  new String[][][]{{{nv, mn, mx, v1, null, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}  ),
      Tuple.of((byte)29,       v6,      v5,        new String[]{v1, nv, mn, mx, v2, v3, v4, v5, v6}, new String[][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},             new String[]{v1, nv, mn, mx, v2, v3, v4, v5, v6, null}, new String[][][]{{{nv, mn, mx, v1, v2, null, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}, v6,        v5,      new String[]{v1, nv, mn, mx, v2, v3, v4, v5, v6}, new String[][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},      new String[]{v1, nv, mn, mx, v2, v3, v4, v5, v6, null},  new String[][][]{{{nv, mn, mx, v1, v2, null, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}  )
    );
  }
}
