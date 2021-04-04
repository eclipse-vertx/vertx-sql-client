package io.vertx.clickhousenativeclient.alltypes;

import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Tuple;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

@RunWith(VertxUnitRunner.class)
public class Enum8Test extends AllTypesBase<Test8Enum> {
  public Enum8Test() {
    this(Test8Enum.class);
  }

  private Enum8Test(Class<Test8Enum> cls) {
    super("enum8", new MyColumnChecker<>(cls,
      (row, idx) -> row.get(cls, idx), (tp, name) -> tp.get(cls, name), null, null), false);
  }

  @Override
  public List<Tuple> createBatch() {
    Test8Enum v1 = Test8Enum.v0;
    Test8Enum v2 = Test8Enum.v1;
    Test8Enum v3 = Test8Enum.v2;
    Test8Enum v4 = Test8Enum.v3;
    Test8Enum v5 = Test8Enum.v4;
    Test8Enum v6 = Test8Enum.v5;
    Test8Enum nv = Test8Enum.v0;
    Test8Enum mn = Test8Enum.v3;
    Test8Enum mx = Test8Enum.v6;

    return Arrays.asList(
      //            id    simple_t    nullable_t   array_t                                              array3_t                                                                                                                   nullable_array_t                                           nullable_array3_t
      Tuple.of((byte)1,        mn,      mn,        new Test8Enum[]{mn, mn},                             new Test8Enum[][][]{{{mn, mn}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new Test8Enum[]{mn, mn},                                   new Test8Enum[][][]{{{mn, null, mn}, {mn, mn, null}, {null}}, {{mn, mn}, {mn, mn}, {}}, {{}, {null}, {}}}                    ),
      Tuple.of((byte)2,        mn,      mn,        new Test8Enum[]{mn, mn},                             new Test8Enum[][][]{{{mn, mn}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new Test8Enum[]{mn, mn},                                   new Test8Enum[][][]{{{mn, null, mn, null}, {mn, mn, null}, {null}}, {{mn, mn}, {mn, mn}, {}}, {{}, {null}, {}}}              ),
      Tuple.of((byte)3,        mn,      mn,        new Test8Enum[]{mn, mn},                             new Test8Enum[][][]{{{mn, mn}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new Test8Enum[]{mn, null, mn},                             new Test8Enum[][][]{{{mn, mn, null}, {mn, mn, null}, {null}}, {{mn, mn}, {mn, mn}, {}}, {{}, {null}, {}}}                    ),
      Tuple.of((byte)4,        mn,      mn,        new Test8Enum[]{mn, mn},                             new Test8Enum[][][]{{{mn, mn}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new Test8Enum[]{mn, null, mn},                             new Test8Enum[][][]{{{mn, null, mn}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {null}, {}}}                              ),
      Tuple.of((byte)5,        mx,      mx,        new Test8Enum[]{mx, mx},                             new Test8Enum[][][]{{{mx, mx}, {mx, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new Test8Enum[]{mx, mx},                                   new Test8Enum[][][]{{{mx, null, mx}, {mx, mx, null}, {}}, {{mn, mn}, {mn, null, mn}, {}}, {{}, {null}, {}}}                  ),
      Tuple.of((byte)6,        mn,      mn,        new Test8Enum[]{mx, mx},                             new Test8Enum[][][]{{{mx, mx}, {mx, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new Test8Enum[]{mx, mx},                                   new Test8Enum[][][]{{{mx, mx}, {mx, null, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{null}, {}, {}}}                              ),
      Tuple.of((byte)7,        mx,      mx,        new Test8Enum[]{mx, mx},                             new Test8Enum[][][]{{{mx, mx}, {mx, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new Test8Enum[]{mx, null, mx},                             new Test8Enum[][][]{{{mx, mx}, {mx, null, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}                                  ),
      Tuple.of((byte)8,        mn,      mn,        new Test8Enum[]{mx, mx},                             new Test8Enum[][][]{{{mx, mx}, {mx, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new Test8Enum[]{mx, null, mx},                             new Test8Enum[][][]{{{mx, mx}, {mx, null, mx, null}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {null}, {}}}                        ),
      Tuple.of((byte)9,        mx,      mx,        new Test8Enum[]{mn, mx},                             new Test8Enum[][][]{{{mn, mx}, {mn, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new Test8Enum[]{mn, null, mx},                             new Test8Enum[][][]{{{mn, mx, null}, {mn, null, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}                            ),
      Tuple.of((byte)10,       mn,      mn,        new Test8Enum[]{mn, mx},                             new Test8Enum[][][]{{{mn, mx}, {mn, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new Test8Enum[]{mn, null, mx},                             new Test8Enum[][][]{{{mn, mx, null}, {mn, null, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}                            ),
      Tuple.of((byte)11,       v2,      v3,        new Test8Enum[]{},                                   new Test8Enum[][][]{{{}, {}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                                 new Test8Enum[]{},                                         new Test8Enum[][][]{{{}, {}, {}}, {{}, {}, {}}, {{}, {}, {}}}                                                                ),
      Tuple.of((byte)12,       v2,      v3,        new Test8Enum[]{},                                   new Test8Enum[][][]{{{}, {}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                                 new Test8Enum[]{},                                         new Test8Enum[][][]{{{}, {}, {}}, {{}, {}, {}}, {{}, {}, {}}}                                                                ),
      Tuple.of((byte)13,       v2,      v3,        new Test8Enum[]{},                                   new Test8Enum[][][]{{{}}},                                                                                                 new Test8Enum[]{},                                         new Test8Enum[][][]{{{}}}                                                                                                    ),
      Tuple.of((byte)14,       v2,      v3,        new Test8Enum[]{},                                   new Test8Enum[][][]{{{}}},                                                                                                 new Test8Enum[]{},                                         new Test8Enum[][][]{{{}}}                                                                                                    ),
      Tuple.of((byte)15,       v2,      v3,        new Test8Enum[]{},                                   new Test8Enum[][][]{{{}}},                                                                                                 new Test8Enum[]{null},                                     new Test8Enum[][][]{{{null}}}                                                                                                ),
      Tuple.of((byte)16,       v2,      v3,        new Test8Enum[]{},                                   new Test8Enum[][][]{{{}}},                                                                                                 new Test8Enum[]{null},                                     new Test8Enum[][][]{{{null}}}                                                                                                ),
      Tuple.of((byte)17,       v2,      v3,        new Test8Enum[]{nv},                                 new Test8Enum[][][]{{{nv, nv}, {nv, nv}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new Test8Enum[]{nv},                                       new Test8Enum[][][]{{{nv, nv}, {nv, nv}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}                                        ),
      Tuple.of((byte)18,       v2,      v3,        new Test8Enum[]{nv},                                 new Test8Enum[][][]{{{nv, nv}, {nv, nv}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new Test8Enum[]{nv},                                       new Test8Enum[][][]{{{nv, nv}, {nv, nv}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}                                        ),
      Tuple.of((byte)19,       v2,      v3,        new Test8Enum[]{nv, mn, mx},                         new Test8Enum[][][]{{{v2, nv, mn}, {v3, mn, nv, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                         new Test8Enum[]{nv, mn, null, mx},                         new Test8Enum[][][]{{{v2, nv, mn, null}, {v3, mn, nv, mx}, {}}, {{mn, mn, null}, {mn, mn}, {}}, {{}, {}, {}}}                ),
      Tuple.of((byte)20,       v2,      v3,        new Test8Enum[]{nv, mn, mx},                         new Test8Enum[][][]{{{v2, nv, mn}, {v3, mn, nv, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                         new Test8Enum[]{nv, mn, null, mx},                         new Test8Enum[][][]{{{v2, nv, mn, null}, {v3, mn, nv, mx}, {}}, {{mn, mn, null}, {mn, mn}, {}}, {{}, {}, {}}}                ),
      Tuple.of((byte)21,       v2,      v3,        new Test8Enum[]{nv, mn, mx},                         new Test8Enum[][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},             new Test8Enum[]{nv, mn, null, mx},                         new Test8Enum[][][]{{{nv, mn, mx, v1, v2, null, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}          ),
      Tuple.of((byte)22,       v2,      v3,        new Test8Enum[]{nv, mn, mx},                         new Test8Enum[][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},             new Test8Enum[]{nv, mn, null, mx},                         new Test8Enum[][][]{{{nv, mn, mx, v1, v2, v3, null, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}          ),
      Tuple.of((byte)23, nv,    null,        new Test8Enum[]{v3, v1, nv, mx, v4},                 new Test8Enum[][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},             new Test8Enum[]{v3, v1, null, nv, mx, v3},                 new Test8Enum[][][]{{{nv, mn, mx, v1, v2, v3, v4, v5, null}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}          ),
      Tuple.of((byte)24, nv,    null,        new Test8Enum[]{v3, v1, nv, mx, v4},                 new Test8Enum[][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},             new Test8Enum[]{v3, v1, null, nv, mx, v3},                 new Test8Enum[][][]{{{nv, mn, mx, null, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}          ),
      Tuple.of((byte)25,       v1,      v1,        new Test8Enum[]{v1, nv, nv},                         new Test8Enum[][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},             new Test8Enum[]{v3, nv, null},                             new Test8Enum[][][]{{{nv, mn, null, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}          ),
      Tuple.of((byte)26,       nv,      nv,        new Test8Enum[]{nv, nv, nv},                         new Test8Enum[][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},             new Test8Enum[]{nv, null, v4},                             new Test8Enum[][][]{{{nv, null, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}          ),
      Tuple.of((byte)27,       v6,      v5,        new Test8Enum[]{v4, nv, nv},                         new Test8Enum[][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},             new Test8Enum[]{v3, nv, null},                             new Test8Enum[][][]{{{nv, mn, null, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}          ),
      Tuple.of((byte)28,       v6,      v5,        new Test8Enum[]{v1, nv, mn, mx, v2, v3, v4, v5, v6}, new Test8Enum[][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},             new Test8Enum[]{v1, nv, mn, mx, v2, v3, v4, v5, v6, null}, new Test8Enum[][][]{{{nv, mn, mx, v1, null, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}          ),
      Tuple.of((byte)29,       v6,      v5,        new Test8Enum[]{v1, nv, mn, mx, v2, v3, v4, v5, v6}, new Test8Enum[][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},             new Test8Enum[]{v1, nv, mn, mx, v2, v3, v4, v5, v6, null}, new Test8Enum[][][]{{{nv, mn, mx, v1, v2, null, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}          )
    );
  }
}

enum Test8Enum {
  v0, v1, v2, v3, v4, v5, v6
}
