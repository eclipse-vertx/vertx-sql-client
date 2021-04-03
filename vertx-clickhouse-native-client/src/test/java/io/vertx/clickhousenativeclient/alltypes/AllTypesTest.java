package io.vertx.clickhousenativeclient.alltypes;

import io.vertx.clickhouse.clickhousenative.ClickhouseNativeConnectOptions;
import io.vertx.clickhouse.clickhousenative.ClickhouseNativeConnection;
import io.vertx.clickhouse.clickhousenative.impl.codec.columns.DateColumnReader;
import io.vertx.clickhouse.clickhousenative.impl.codec.columns.DateTimeColumnReader;
import io.vertx.clickhousenativeclient.ClickhouseResource;
import io.vertx.clickhousenativeclient.Sleep;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.ColumnChecker;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.junit.*;
import org.junit.runner.RunWith;

import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@RunWith(VertxUnitRunner.class)
public class AllTypesTest {
  private static final Logger LOG = LoggerFactory.getLogger(AllTypesTest.class);

  public static final String TABLE_PREFIX = "vertx_test_";

  @ClassRule
  public static ClickhouseResource rule = new ClickhouseResource();

  private ClickhouseNativeConnectOptions options;
  private Vertx vertx;

  @Before
  public void setup(TestContext ctx) {
    options = rule.options();
    vertx = Vertx.vertx();
  }

  @After
  public void teardDown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  private List<String> columnsList(boolean hasLowCardinality) {
    List<String> columns = new ArrayList<>(Arrays.asList("id", "simple_t", "nullable_t", "array_t", "array3_t", "nullable_array_t", "nullable_array3_t"));
    if (hasLowCardinality) {
      columns.addAll(Arrays.asList("simple_lc_t", "nullable_lc_t", "array_lc_t", "array3_lc_t", "nullable_array_lc_t", "nullable_array3_lc_t"));
    }
    return columns;
  }

  @Test
  public void testEmptyBlob(TestContext ctx) {
    doTest(ctx, "string", true, new MyColumnChecker<>(byte[].class, null, null, null, null), Collections.emptyList());
  }

  @Test
  public void testBlob(TestContext ctx) {
    byte[] v1 = b("val1");
    byte[] v2 = b("val2");
    byte[] v3 = b("val3");
    byte[] v4 = b("val4");
    byte[] v5 = b("value5");
    byte[] v6 = b("value_value_6");
    byte[] nv = b("");
    byte[] mn = b("");
    byte[] mx = b("not so looooooooooooooooooooooooooooooooooooooong value");

    List<Tuple> batch = Arrays.asList(
      //            id    simple_t    nullable_t   array_t                                           array3_t                                                                                                                nullable_array_t                                        nullable_array3_t                                                                                        simple_lc_t  nullable_lc_t   array_lc_t                                        array3_lc_t                                                                                                      nullable_array_lc_t
      Tuple.of((byte)1,        mn,      mn,        new byte[][]{mn, mn},                             new byte[][][][]{{{mn, mn}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new byte[][]{mn, mn},                                   new byte[][][][]{{{mn, null, mn}, {mn, mn, null}, {null}}, {{mn, mn}, {mn, mn}, {}}, {{}, {null}, {}}},           mn,        mn,      new byte[][]{mn, mn},                             new byte[][][][]{{{mn, mn}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                              new byte[][]{mn, mn},                                    new byte[][][][]{{{mn, null, mn}, {mn, mn, null}, {null}}, {{mn, mn}, {mn, mn}, {}}, {{}, {null}, {}}}            ),
      Tuple.of((byte)2,        mn,      mn,        new byte[][]{mn, mn},                             new byte[][][][]{{{mn, mn}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new byte[][]{mn, mn},                                   new byte[][][][]{{{mn, null, mn, null}, {mn, mn, null}, {null}}, {{mn, mn}, {mn, mn}, {}}, {{}, {null}, {}}},     mn,        mn,      new byte[][]{mn, mn},                             new byte[][][][]{{{mn, mn}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                              new byte[][]{mn, mn},                                    new byte[][][][]{{{mn, null, mn, null}, {mn, mn, null}, {null}}, {{mn, mn}, {mn, mn}, {}}, {{}, {null}, {}}}      ),
      Tuple.of((byte)3,        mn,      mn,        new byte[][]{mn, mn},                             new byte[][][][]{{{mn, mn}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new byte[][]{mn, null, mn},                             new byte[][][][]{{{mn, mn, null}, {mn, mn, null}, {null}}, {{mn, mn}, {mn, mn}, {}}, {{}, {null}, {}}},           mn,        mn,      new byte[][]{mn, mn},                             new byte[][][][]{{{mn, mn}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                              new byte[][]{mn, null, mn},                              new byte[][][][]{{{mn, mn, null}, {mn, mn, null}, {null}}, {{mn, mn}, {mn, mn}, {}}, {{}, {null}, {}}}            ),
      Tuple.of((byte)4,        mn,      mn,        new byte[][]{mn, mn},                             new byte[][][][]{{{mn, mn}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new byte[][]{mn, null, mn},                             new byte[][][][]{{{mn, null, mn}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {null}, {}}},                     mn,        mn,      new byte[][]{mn, mn},                             new byte[][][][]{{{mn, mn}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                              new byte[][]{mn, null, mn},                              new byte[][][][]{{{mn, null, mn}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {null}, {}}}                      ),
      Tuple.of((byte)5,        mx,      mx,        new byte[][]{mx, mx},                             new byte[][][][]{{{mx, mx}, {mx, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new byte[][]{mx, mx},                                   new byte[][][][]{{{mx, null, mx}, {mx, mx, null}, {}}, {{mn, mn}, {mn, null, mn}, {}}, {{}, {null}, {}}},         mx,        mx,      new byte[][]{mx, mx},                             new byte[][][][]{{{mx, mx}, {mx, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                              new byte[][]{mx, null, mx},                              new byte[][][][]{{{mx, null, mx}, {mx, mx, null}, {}}, {{mn, mn}, {mn, null, mn}, {}}, {{}, {null}, {}}}          ),
      Tuple.of((byte)6,        mn,      mn,        new byte[][]{mx, mx},                             new byte[][][][]{{{mx, mx}, {mx, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new byte[][]{mx, mx},                                   new byte[][][][]{{{mx, mx}, {mx, null, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{null}, {}, {}}},                     mn,        mn,      new byte[][]{mx, mx},                             new byte[][][][]{{{mx, mx}, {mx, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                              new byte[][]{mx, null, mx},                              new byte[][][][]{{{mx, mx}, {mx, null, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{null}, {}, {}}}                      ),
      Tuple.of((byte)7,        mx,      mx,        new byte[][]{mx, mx},                             new byte[][][][]{{{mx, mx}, {mx, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new byte[][]{mx, null, mx},                             new byte[][][][]{{{mx, mx}, {mx, null, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                         mx,        mx,      new byte[][]{mx, mx},                             new byte[][][][]{{{mx, mx}, {mx, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                              new byte[][]{mx, null, mx},                              new byte[][][][]{{{mx, mx}, {mx, null, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}                          ),
      Tuple.of((byte)8,        mn,      mn,        new byte[][]{mx, mx},                             new byte[][][][]{{{mx, mx}, {mx, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new byte[][]{mx, null, mx},                             new byte[][][][]{{{mx, mx}, {mx, null, mx, null}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {null}, {}}},               mn,        mn,      new byte[][]{mx, mx},                             new byte[][][][]{{{mx, mx}, {mx, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                              new byte[][]{mx, null, mx},                              new byte[][][][]{{{mx, mx}, {mx, null, mx, null}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {null}, {}}}                ),
      Tuple.of((byte)9,        mx,      mx,        new byte[][]{mn, mx},                             new byte[][][][]{{{mn, mx}, {mn, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new byte[][]{mn, null, mx},                             new byte[][][][]{{{mn, mx, null}, {mn, null, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                   mx,        mx,      new byte[][]{mn, mx},                             new byte[][][][]{{{mn, mx}, {mn, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                              new byte[][]{mn, null, mx},                              new byte[][][][]{{{mn, mx, null}, {mn, null, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}                    ),
      Tuple.of((byte)10,       mn,      mn,        new byte[][]{mn, mx},                             new byte[][][][]{{{mn, mx}, {mn, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new byte[][]{mn, null, mx},                             new byte[][][][]{{{mn, mx, null}, {mn, null, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                   mn,        mn,      new byte[][]{mn, mx},                             new byte[][][][]{{{mn, mx}, {mn, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                              new byte[][]{mn, null, mx},                              new byte[][][][]{{{mn, mx, null}, {mn, null, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}                    ),
      Tuple.of((byte)11,       v2,      v3,        new byte[][]{},                                   new byte[][][][]{{{}, {}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                                 new byte[][]{},                                         new byte[][][][]{{{}, {}, {}}, {{}, {}, {}}, {{}, {}, {}}},                                                       v2,        v3,      new byte[][]{},                                   new byte[][][][]{{{}, {}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                          new byte[][]{},                                          new byte[][][][]{{{}, {}, {}}, {{}, {}, {}}, {{}, {}, {}}}                                                        ),
      Tuple.of((byte)12,       v2,      v3,        new byte[][]{},                                   new byte[][][][]{{{}, {}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                                 new byte[][]{},                                         new byte[][][][]{{{}, {}, {}}, {{}, {}, {}}, {{}, {}, {}}},                                                       v2,        v3,      new byte[][]{},                                   new byte[][][][]{{{}, {}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                          new byte[][]{},                                          new byte[][][][]{{{}, {}, {}}, {{}, {}, {}}, {{}, {}, {}}}                                                        ),
      Tuple.of((byte)13,       v2,      v3,        new byte[][]{nv},                                 new byte[][][][]{{{nv, nv}, {nv, nv}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new byte[][]{nv},                                       new byte[][][][]{{{nv, nv}, {nv, nv}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                               v2,        v3,      new byte[][]{nv},                                 new byte[][][][]{{{nv, nv}, {nv, nv}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                              new byte[][]{nv},                                        new byte[][][][]{{{nv, nv}, {nv, nv}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}                                ),
      Tuple.of((byte)14,       v2,      v3,        new byte[][]{nv},                                 new byte[][][][]{{{nv, nv}, {nv, nv}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                                     new byte[][]{nv},                                       new byte[][][][]{{{nv, nv}, {nv, nv}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                               v2,        v3,      new byte[][]{nv},                                 new byte[][][][]{{{nv, nv}, {nv, nv}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                              new byte[][]{nv},                                        new byte[][][][]{{{nv, nv}, {nv, nv}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}                                ),
      Tuple.of((byte)15,       v2,      v3,        new byte[][]{nv, mn, mx},                         new byte[][][][]{{{v2, nv, mn}, {v3, mn, nv, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                         new byte[][]{nv, mn, null, mx},                         new byte[][][][]{{{v2, nv, mn, null}, {v3, mn, nv, mx}, {}}, {{mn, mn, null}, {mn, mn}, {}}, {{}, {}, {}}},       v2,        v3,      new byte[][]{nv, mn, mx},                         new byte[][][][]{{{v2, nv, mn}, {v3, mn, nv, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                  new byte[][]{nv},                                        new byte[][][][]{{{v2, nv, mn, null}, {v3, mn, nv, mx}, {}}, {{mn, mn, null}, {mn, mn}, {}}, {{}, {}, {}}}        ),
      Tuple.of((byte)16,       v2,      v3,        new byte[][]{nv, mn, mx},                         new byte[][][][]{{{v2, nv, mn}, {v3, mn, nv, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                         new byte[][]{nv, mn, null, mx},                         new byte[][][][]{{{v2, nv, mn, null}, {v3, mn, nv, mx}, {}}, {{mn, mn, null}, {mn, mn}, {}}, {{}, {}, {}}},       v2,        v3,      new byte[][]{nv, mn, mx},                         new byte[][][][]{{{v2, nv, mn}, {v3, mn, nv, mx}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},                  new byte[][]{nv},                                        new byte[][][][]{{{v2, nv, mn, null}, {v3, mn, nv, mx}, {}}, {{mn, mn, null}, {mn, mn}, {}}, {{}, {}, {}}}        ),
      Tuple.of((byte)17,       v2,      v3,        new byte[][]{nv, mn, mx},                         new byte[][][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},             new byte[][]{nv, mn, null, mx},                         new byte[][][][]{{{nv, mn, mx, v1, v2, null, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}, v2,        v3,      new byte[][]{nv, mn, mx},                         new byte[][][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},      new byte[][]{nv, mn, null, mx},                          new byte[][][][]{{{nv, mn, mx, v1, v2, null, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}  ),
      Tuple.of((byte)18,       v2,      v3,        new byte[][]{nv, mn, mx},                         new byte[][][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},             new byte[][]{nv, mn, null, mx},                         new byte[][][][]{{{nv, mn, mx, v1, v2, v3, null, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}, v2,        v3,      new byte[][]{nv, mn, mx},                         new byte[][][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},      new byte[][]{nv, mn, null, mx},                          new byte[][][][]{{{nv, mn, mx, v1, v2, v3, null, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}  ),
      Tuple.of((byte)19, nv,    null,        new byte[][]{v3, v1, nv, mx, v4},                 new byte[][][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},             new byte[][]{v3, v1, null, nv, mx, v3},                 new byte[][][][]{{{nv, mn, mx, v1, v2, v3, v4, v5, null}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}, nv,      null,      new byte[][]{v3, v1, nv, mx, v4},                 new byte[][][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},      new byte[][]{nv, v2, null, v3, v2},                       new byte[][][][]{{{nv, mn, mx, v1, v2, v3, v4, v5, null}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}} ),
      Tuple.of((byte)20, nv,    null,        new byte[][]{v3, v1, nv, mx, v4},                 new byte[][][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},             new byte[][]{v3, v1, null, nv, mx, v3},                 new byte[][][][]{{{nv, mn, mx, null, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}, nv,      null,      new byte[][]{v3, v1, nv, mx, v4},                 new byte[][][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},      new byte[][]{nv, v2, null, v3, v2},                       new byte[][][][]{{{nv, mn, mx, null, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}} ),
      Tuple.of((byte)21,       v1,      v1,        new byte[][]{v1, nv, nv},                         new byte[][][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},             new byte[][]{v3, nv, null},                             new byte[][][][]{{{nv, mn, null, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}, v1,        v1,      new byte[][]{v1, nv, nv},                         new byte[][][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},      new byte[][]{v2, nv, null},                              new byte[][][][]{{{nv, mn, null, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}  ),
      Tuple.of((byte)22,       nv,      nv,        new byte[][]{nv, nv, nv},                         new byte[][][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},             new byte[][]{nv, null, v4},                             new byte[][][][]{{{nv, null, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}, nv,        nv,      new byte[][]{nv, nv, nv},                         new byte[][][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},      new byte[][]{nv, null, nv},                              new byte[][][][]{{{nv, null, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}  ),
      Tuple.of((byte)23,       v6,      v5,        new byte[][]{v4, nv, nv},                         new byte[][][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},             new byte[][]{v3, nv, null},                             new byte[][][][]{{{nv, mn, null, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}, v6,        v5,      new byte[][]{v4, nv, nv},                         new byte[][][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},      new byte[][]{v2, nv, null},                              new byte[][][][]{{{nv, mn, null, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}  ),
      Tuple.of((byte)24,       v6,      v5,        new byte[][]{v1, nv, mn, mx, v2, v3, v4, v5, v6}, new byte[][][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},             new byte[][]{v1, nv, mn, mx, v2, v3, v4, v5, v6, null}, new byte[][][][]{{{nv, mn, mx, v1, null, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}, v6,        v5,      new byte[][]{v1, nv, mn, mx, v2, v3, v4, v5, v6}, new byte[][][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},      new byte[][]{v1, nv, mn, mx, v2, v3, v4, v5, v6, null},  new byte[][][][]{{{nv, mn, mx, v1, null, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}  ),
      Tuple.of((byte)25,       v6,      v5,        new byte[][]{v1, nv, mn, mx, v2, v3, v4, v5, v6}, new byte[][][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},             new byte[][]{v1, nv, mn, mx, v2, v3, v4, v5, v6, null}, new byte[][][][]{{{nv, mn, mx, v1, v2, null, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}, v6,        v5,      new byte[][]{v1, nv, mn, mx, v2, v3, v4, v5, v6}, new byte[][][][]{{{nv, mn, mx, v1, v2, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}},      new byte[][]{v1, nv, mn, mx, v2, v3, v4, v5, v6, null},  new byte[][][][]{{{nv, mn, mx, v1, v2, null, v3, v4, v5}, {mn, mn}, {}}, {{mn, mn}, {mn, mn}, {}}, {{}, {}, {}}}  )
    );
    doTest(ctx, "string", true, new MyColumnChecker<>(byte[].class, null, null, null, null), batch);
  }

  //@Test
  public void testUInt8(TestContext ctx) {
    List<Tuple> batch = Arrays.asList(
      Tuple.of((byte)1, (short)0,       (short)0,   new Short[]{},                new Short[]{},                   (short)0,   (short)0,   new Short[]{},                new Short[]{}   ),
      Tuple.of((byte)2, (short)0,  null,      new Short[]{0, 2, 0, 3, 255}, new Short[]{0, 2, null, 3, 255},  (short)0,   null,      new Short[]{0, 2, 0, 3, 255}, new Short[]{0, 2, null, 3, 255}      ),
      Tuple.of((byte)3, (short)255,     (short)255, new Short[]{255, 0, 0},       new Short[]{255, 0, null},       (short)255, (short)255, new Short[]{255, 0, 0},       new Short[]{255, 0, null} ),
      Tuple.of((byte)4, (short)0,       (short)0,   new Short[]{0, 0, 0},         new Short[]{0, null, 0},         (short)0  , (short)0,   new Short[]{0, 0, 0},         new Short[]{0, null, 0}   )
    );
    doTest(ctx, "uint8",  true, new MyColumnChecker<>(Short.class, Tuple::getShort, Row::getShort, Tuple::getArrayOfShorts, Row::getArrayOfShorts), batch);
  }

  //@Test
  public void testInt8(TestContext ctx) {
    List<Tuple> batch = Arrays.asList(
      Tuple.of((byte)1, (byte)-128,   (byte)-128, new Byte[]{},                   new Byte[]{},                      (byte)0,   (byte)0,   new Byte[]{},                new Byte[]{}   ),
      Tuple.of((byte)2, (byte)0, null,      new Byte[]{-128, 2, 0, 3, 127}, new Byte[]{-128, 2, null, 3, 127}, (byte)0,   null,      new Byte[]{0, 2, 0, 3, 127}, new Byte[]{0, 2, null, 3, -128}      ),
      Tuple.of((byte)3, (byte)127,    (byte)127,  new Byte[]{127, 0, 0},          new Byte[]{127, 0, null},          (byte)255, (byte)255, new Byte[]{-128, 0, 0},      new Byte[]{127, 0, null} ),
      Tuple.of((byte)4, (byte)0,      (byte)0,    new Byte[]{0, 0, 0},            new Byte[]{0, null, 0},            (byte)0,   (byte)0,   new Byte[]{0, 0, 0},         new Byte[]{0, null, 0}   )
    );
    doTest(ctx, "int8", true, new MyColumnChecker<>(Byte.class, null, null, null, null), batch);
  }

  //@Test
  public void testString(TestContext ctx) {
    String v2 = "val2";
    String v_1 = "val_1";
    String v4 = "val_4";
    String v5 = "val5";
    String v3 = "val3";
    String v1 = "val1";
    List<Tuple> batch = Arrays.asList(
      Tuple.of((byte)1, v2,  v3,   new String[]{},                    new String[]{},                          "",   "",   new String[]{},                   new String[]{} ),
      Tuple.of((byte)2, "",  null, new String[]{v3, v1, "", "z", v5}, new String[]{v3, v1, null, "", "z", v3}, "", null,   new String[]{"", v1, "", v2, v2}, new String[]{"", v2, null, v3, v2} ),
      Tuple.of((byte)3, v_1, v4,   new String[]{v5, "", ""},          new String[]{v3, "", null},              v5,   v5,   new String[]{v1, "", ""},         new String[]{v2, "", null} ),
      Tuple.of((byte)4, "",  "",   new String[]{"", "", ""},          new String[]{"", null, v5},              "",   "",   new String[]{"", "", ""},         new String[]{"", null, ""} ),
      Tuple.of((byte)5, v_1, v4,   new String[]{v5, "", ""},          new String[]{v3, "", null},              v5,   v5,   new String[]{v1, "", ""},         new String[]{v2, "", null} )
    );
    doTest(ctx, "string", true, new MyColumnChecker<>(String.class, Tuple::getString, Row::getString, Tuple::getArrayOfStrings, Row::getArrayOfStrings), batch);
  }

  //@Test
  public void testDate(TestContext ctx) {
    LocalDate dt = LocalDate.of(2020, 3, 29);
    LocalDate d2 = dt.plusDays(2);
    LocalDate d3 = dt.plusDays(3);
    LocalDate mn = DateColumnReader.MIN_VALUE;
    LocalDate mx = DateColumnReader.MAX_VALUE;
    List<Tuple> batch = Arrays.asList(
      Tuple.of((byte)1,       d2,   d2, new LocalDate[]{},                   new LocalDate[]{},                         mn, mn,   new LocalDate[]{},                   new LocalDate[]{} ),
      Tuple.of((byte)2, mn, null, new LocalDate[]{d2, mn, mn, mx, d3}, new LocalDate[]{d2, d3, null, mn, mn, d3}, mn, null, new LocalDate[]{mn, d2, mn, d3, d3}, new LocalDate[]{mn, d2, null, d3, d2} ),
      Tuple.of((byte)3,       dt,   dt, new LocalDate[]{d2, mn, mn},         new LocalDate[]{d3, mn, null},             d2, d3,   new LocalDate[]{d2, mn, mn},         new LocalDate[]{d2, mn, null} ),
      Tuple.of((byte)4,       dt,   dt, new LocalDate[]{mn, mn, mn},         new LocalDate[]{mn, null, d3},             mn, mn,   new LocalDate[]{mn, mn, mn},         new LocalDate[]{mn, null, mn} ),
      Tuple.of((byte)5,       mn,   mn, new LocalDate[]{d2, mn, mn},         new LocalDate[]{d3, mn, null},             d2, d3,   new LocalDate[]{d2, mn, mn},         new LocalDate[]{d2, mn, null} ),
      Tuple.of((byte)6,       mx,   mx, new LocalDate[]{d2, mn, mn},         new LocalDate[]{d3, mn, null},             d2, d3,   new LocalDate[]{d2, mn, mn},         new LocalDate[]{d2, mn, null} ),
      Tuple.of((byte)6,       mx,   mx, new LocalDate[]{d2, mn, mn},         new LocalDate[]{d3, mn, null},             d2, d3,   new LocalDate[]{d2, mn, mn},         new LocalDate[]{d2, mn, null} )
    );
    doTest(ctx, "date", true, new MyColumnChecker<>(LocalDate.class, Tuple::getLocalDate, Row::getLocalDate, Tuple::getArrayOfLocalDates, Row::getArrayOfLocalDates), batch);
  }

  //@Test
  public void testDateTime(TestContext ctx) {
    ZoneId zoneId = ZoneId.of("Europe/Oslo");
    OffsetDateTime dt = Instant.ofEpochSecond(1617120094L).atZone(zoneId).toOffsetDateTime();
    OffsetDateTime d2 = Instant.ofEpochSecond(1617120094L).atZone(zoneId).toOffsetDateTime();
    OffsetDateTime d3 = Instant.ofEpochSecond(1617120094L).atZone(zoneId).toOffsetDateTime();
    OffsetDateTime mn = Instant.ofEpochSecond(0).atZone(zoneId).toOffsetDateTime();
    OffsetDateTime mx = Instant.ofEpochSecond(DateTimeColumnReader.MAX_EPOCH_SECOND).atZone(zoneId).toOffsetDateTime();

    List<Tuple> batch = Arrays.asList(
      Tuple.of((byte)1,       d2,   d2, new OffsetDateTime[]{},                   new OffsetDateTime[]{},                         mn, mn,   new OffsetDateTime[]{},                   new OffsetDateTime[]{} ),
      Tuple.of((byte)2, mn, null, new OffsetDateTime[]{d2, mn, mn, mx, d3}, new OffsetDateTime[]{d2, d3, null, mn, mn, d3}, mn, null, new OffsetDateTime[]{mn, d2, mn, d3, d3}, new OffsetDateTime[]{mn, d2, null, d3, d2} ),
      Tuple.of((byte)3,       dt,   dt, new OffsetDateTime[]{d2, mn, mn},         new OffsetDateTime[]{d3, mn, null},             d2, d3,   new OffsetDateTime[]{d2, mn, mn},         new OffsetDateTime[]{d2, mn, null} ),
      Tuple.of((byte)4,       dt,   dt, new OffsetDateTime[]{mn, mn, mn},         new OffsetDateTime[]{mn, null, d3},             mn, mn,   new OffsetDateTime[]{mn, mn, mn},         new OffsetDateTime[]{mn, null, mn} ),
      Tuple.of((byte)5,       mn,   mn, new OffsetDateTime[]{d2, mn, mn},         new OffsetDateTime[]{d3, mn, null},             d2, d3,   new OffsetDateTime[]{d2, mn, mn},         new OffsetDateTime[]{d2, mn, null} ),
      Tuple.of((byte)6,       mx,   mx, new OffsetDateTime[]{d2, mn, mn},         new OffsetDateTime[]{d3, mn, null},             d2, d3,   new OffsetDateTime[]{d2, mn, mn},         new OffsetDateTime[]{d2, mn, null} ),
      Tuple.of((byte)6,       mx,   mx, new OffsetDateTime[]{d2, mn, mn},         new OffsetDateTime[]{d3, mn, null},             d2, d3,   new OffsetDateTime[]{d2, mn, mn},         new OffsetDateTime[]{d2, mn, null} )
    );
    doTest(ctx, "datetime", true, new MyColumnChecker<>(OffsetDateTime.class, Tuple::getOffsetDateTime, Row::getOffsetDateTime, Tuple::getArrayOfOffsetDateTimes, Row::getArrayOfOffsetDateTimes), batch);
  }

  private static byte[] b(String s) {
    return s.getBytes(StandardCharsets.UTF_8);
  }

  private <R> void doTest(TestContext ctx, String tableSuffix, boolean hasLowCardinality,
                      MyColumnChecker<R> columnChecker, List<Tuple> batch) {
    String tableName = TABLE_PREFIX + tableSuffix;
    ClickhouseNativeConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("TRUNCATE TABLE " + tableName).execute(
        ctx.asyncAssertSuccess(res1 -> {
          Sleep.sleepOrThrow();
          List<String> columnsList = columnsList(hasLowCardinality);
          String columnsStr = String.join(", ", columnsList);
          String query = "INSERT INTO " + tableName + " (" + columnsStr + ") VALUES";
          conn.preparedQuery(query)
            .executeBatch(batch, ctx.asyncAssertSuccess(
              res2 -> {
                Sleep.sleepOrThrow();
                conn.query("SELECT " + columnsStr + " FROM " + tableName + " ORDER BY id").execute(ctx.asyncAssertSuccess(
                  res3 -> {
                    ctx.assertEquals(res3.size(), batch.size(), "row count mismatch");
                    int batchIdx = 0;
                    for (Row row : res3) {
                      Number id = row.get(Number.class, "id");
                      Tuple expectedRow = batch.get(batchIdx);
                      LOG.info("checking row " + tableSuffix + ":" + id);
                      for (int colIdx = 0; colIdx < expectedRow.size(); ++colIdx) {
                        String colName = columnsList.get(colIdx);
                        Object expectedColumnValue = expectedRow.getValue(colIdx);
                        columnChecker.checkColumn(row, colIdx, colName, (R) expectedColumnValue);
                      }
                      ++batchIdx;
                    }
                  }));
              }));
        }));
    }));
  }
}

class MyColumnChecker<R> {
  private final Class<R> componentType;
  private final ColumnChecker.SerializableBiFunction<Tuple, Integer, R> byIndexGetter;
  private final ColumnChecker.SerializableBiFunction<Row, String, R> byNameGetter;
  private final ColumnChecker.SerializableBiFunction<Tuple, Integer, Object> arrayByIndexGetter;
  private final ColumnChecker.SerializableBiFunction<Row, String, Object> arrayByNameGetter;

  public MyColumnChecker(Class<R> componentType,
                         ColumnChecker.SerializableBiFunction<Tuple, Integer, R> byIndexGetter,
                         ColumnChecker.SerializableBiFunction<Row, String, R> byNameGetter,
                         ColumnChecker.SerializableBiFunction<Tuple, Integer, Object> arrayByIndexGetter,
                         ColumnChecker.SerializableBiFunction<Row, String, Object> arrayByNameGetter) {
    this.componentType = componentType;
    this.byIndexGetter = byIndexGetter;
    this.byNameGetter = byNameGetter;
    this.arrayByNameGetter = arrayByNameGetter;
    this.arrayByIndexGetter = arrayByIndexGetter;
  }

  public void checkColumn(Row row, int index, String name, R expected) {
    ColumnChecker checker = ColumnChecker.checkColumn(index, name);
    if ("id".equals(name)) {
      checker.returns((Class<R>)expected.getClass(), expected)
        .forRow(row);
      return;
    }
    if (componentType == byte[].class && (expected == null || expected.getClass() == byte[].class)) {
      //ask driver to turn off String encoding
      checker = checker
        .returns((tuple, idx) -> tuple.get(byte[].class, idx),
                 (ColumnChecker.SerializableBiFunction<Row, String, byte[]>) (r, colName) -> r.get(byte[].class, colName),
                 (Consumer<byte[]>) actual -> Assert.assertArrayEquals((byte[])actual, (byte[])expected));
    } else {
      //arrays are non-nullable
      if (expected != null && expected.getClass().isArray()) {
        boolean multidimensional = expected.getClass().getComponentType().isArray() && expected.getClass().getComponentType() != byte[].class;
        if (componentType == byte[].class) {
          //ask driver to turn off String encoding
          checker = checker.returns((tuple, idx) -> tuple.get(expected.getClass(), idx), (r, colName) -> r.get(expected.getClass(), colName), (Object[]) expected);
        } else {
          checker = checker.returns(Tuple::getValue, Row::getValue, (Object[]) expected);
        }
        if (!multidimensional && arrayByIndexGetter != null) {
          //API does not provided dedicated methods to get multi-dimensional arrays
          checker = checker.returns(arrayByIndexGetter, arrayByNameGetter, (Object[]) expected);
        }
      } else {
        //regular non-array elements
        checker = checker.returns(Tuple::getValue, Row::getValue, expected);
        if (byIndexGetter != null) {
          checker = checker.returns(byIndexGetter, byNameGetter, expected);
        }
      }
    }
    checker.forRow(row);
  }
}
