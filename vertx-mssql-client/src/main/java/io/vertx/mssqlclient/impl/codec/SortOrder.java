/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mssqlclient.impl.codec;

import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;

public enum SortOrder {

  BIN_CP437(30, "SQL_Latin1_General_CP437_BIN", Encoding.CP437),
  DICTIONARY_437(31, "SQL_Latin1_General_CP437_CS_AS", Encoding.CP437),
  NOCASE_437(32, "SQL_Latin1_General_CP437_CI_AS", Encoding.CP437),
  NOCASEPREF_437(33, "SQL_Latin1_General_Pref_CP437_CI_AS", Encoding.CP437),
  NOACCENTS_437(34, "SQL_Latin1_General_CP437_CI_AI", Encoding.CP437),
  BIN2_CP437(35, "SQL_Latin1_General_CP437_BIN2", Encoding.CP437),

  BIN_CP850(40, "SQL_Latin1_General_CP850_BIN", Encoding.CP850),
  DICTIONARY_850(41, "SQL_Latin1_General_CP850_CS_AS", Encoding.CP850),
  NOCASE_850(42, "SQL_Latin1_General_CP850_CI_AS", Encoding.CP850),
  NOCASEPREF_850(43, "SQL_Latin1_General_Pref_CP850_CI_AS", Encoding.CP850),
  NOACCENTS_850(44, "SQL_Latin1_General_CP850_CI_AI", Encoding.CP850),
  BIN2_CP850(45, "SQL_Latin1_General_CP850_BIN2", Encoding.CP850),

  CASELESS_34(49, "SQL_1xCompat_CP850_CI_AS", Encoding.CP850),
  BIN_ISO_1(50, "bin_iso_1", Encoding.CP1252),
  DICTIONARY_ISO(51, "SQL_Latin1_General_CP1_CS_AS", Encoding.CP1252),
  NOCASE_ISO(52, "SQL_Latin1_General_CP1_CI_AS", Encoding.CP1252),
  NOCASEPREF_ISO(53, "SQL_Latin1_General_Pref_CP1_CI_AS", Encoding.CP1252),
  NOACCENTS_ISO(54, "SQL_Latin1_General_CP1_CI_AI", Encoding.CP1252),
  ALT_DICTIONARY(55, "SQL_AltDiction_CP850_CS_AS", Encoding.CP850),
  ALT_NOCASEPREF(56, "SQL_AltDiction_Pref_CP850_CI_AS", Encoding.CP850),
  ALT_NOACCENTS(57, "SQL_AltDiction_CP850_CI_AI", Encoding.CP850),
  SCAND_NOCASEPREF(58, "SQL_Scandinavian_Pref_CP850_CI_AS", Encoding.CP850),
  SCAND_DICTIONARY(59, "SQL_Scandinavian_CP850_CS_AS", Encoding.CP850),
  SCAND_NOCASE(60, "SQL_Scandinavian_CP850_CI_AS", Encoding.CP850),
  ALT_NOCASE(61, "SQL_AltDiction_CP850_CI_AS", Encoding.CP850),

  DICTIONARY_1252(71, "dictionary_1252", Encoding.CP1252),
  NOCASE_1252(72, "nocase_1252", Encoding.CP1252),
  DNK_NOR_DICTIONARY(73, "dnk_nor_dictionary", Encoding.CP1252),
  FIN_SWE_DICTIONARY(74, "fin_swe_dictionary", Encoding.CP1252),
  ISL_DICTIONARY(75, "isl_dictionary", Encoding.CP1252),

  BIN_CP1250(80, "bin_cp1250", Encoding.CP1250),
  DICTIONARY_1250(81, "SQL_Latin1_General_CP1250_CS_AS", Encoding.CP1250),
  NOCASE_1250(82, "SQL_Latin1_General_CP1250_CI_AS", Encoding.CP1250),
  CSYDIC(83, "SQL_Czech_CP1250_CS_AS", Encoding.CP1250),
  CSYNC(84, "SQL_Czech_CP1250_CI_AS", Encoding.CP1250),
  HUNDIC(85, "SQL_Hungarian_CP1250_CS_AS", Encoding.CP1250),
  HUNNC(86, "SQL_Hungarian_CP1250_CI_AS", Encoding.CP1250),
  PLKDIC(87, "SQL_Polish_CP1250_CS_AS", Encoding.CP1250),
  PLKNC(88, "SQL_Polish_CP1250_CI_AS", Encoding.CP1250),
  ROMDIC(89, "SQL_Romanian_CP1250_CS_AS", Encoding.CP1250),
  ROMNC(90, "SQL_Romanian_CP1250_CI_AS", Encoding.CP1250),
  SHLDIC(91, "SQL_Croatian_CP1250_CS_AS", Encoding.CP1250),
  SHLNC(92, "SQL_Croatian_CP1250_CI_AS", Encoding.CP1250),
  SKYDIC(93, "SQL_Slovak_CP1250_CS_AS", Encoding.CP1250),
  SKYNC(94, "SQL_Slovak_CP1250_CI_AS", Encoding.CP1250),
  SLVDIC(95, "SQL_Slovenian_CP1250_CS_AS", Encoding.CP1250),
  SLVNC(96, "SQL_Slovenian_CP1250_CI_AS", Encoding.CP1250),
  POLISH_CS(97, "polish_cs", Encoding.CP1250),
  POLISH_CI(98, "polish_ci", Encoding.CP1250),

  BIN_CP1251(104, "bin_cp1251", Encoding.CP1251),
  DICTIONARY_1251(105, "SQL_Latin1_General_CP1251_CS_AS", Encoding.CP1251),
  NOCASE_1251(106, "SQL_Latin1_General_CP1251_CI_AS", Encoding.CP1251),
  UKRDIC(107, "SQL_Ukrainian_CP1251_CS_AS", Encoding.CP1251),
  UKRNC(108, "SQL_Ukrainian_CP1251_CI_AS", Encoding.CP1251),

  BIN_CP1253(112, "bin_cp1253", Encoding.CP1253),
  DICTIONARY_1253(113, "SQL_Latin1_General_CP1253_CS_AS", Encoding.CP1253),
  NOCASE_1253(114, "SQL_Latin1_General_CP1253_CI_AS", Encoding.CP1253),

  GREEK_MIXEDDICTIONARY(120, "SQL_MixDiction_CP1253_CS_AS", Encoding.CP1253),
  GREEK_ALTDICTIONARY(121, "SQL_AltDiction_CP1253_CS_AS", Encoding.CP1253),
  GREEK_ALTDICTIONARY2(122, "SQL_AltDiction2_CP1253_CS_AS", Encoding.CP1253),
  GREEK_NOCASEDICT(124, "SQL_Latin1_General_CP1253_CI_AI", Encoding.CP1253),
  BIN_CP1254(128, "bin_cp1254", Encoding.CP1254),
  DICTIONARY_1254(129, "SQL_Latin1_General_CP1254_CS_AS", Encoding.CP1254),
  NOCASE_1254(130, "SQL_Latin1_General_CP1254_CI_AS", Encoding.CP1254),

  BIN_CP1255(136, "bin_cp1255", Encoding.CP1255),
  DICTIONARY_1255(137, "SQL_Latin1_General_CP1255_CS_AS", Encoding.CP1255),
  NOCASE_1255(138, "SQL_Latin1_General_CP1255_CI_AS", Encoding.CP1255),

  BIN_CP1256(144, "bin_cp1256", Encoding.CP1256),
  DICTIONARY_1256(145, "SQL_Latin1_General_CP1256_CS_AS", Encoding.CP1256),
  NOCASE_1256(146, "SQL_Latin1_General_CP1256_CI_AS", Encoding.CP1256),

  BIN_CP1257(152, "bin_cp1257", Encoding.CP1257),
  DICTIONARY_1257(153, "SQL_Latin1_General_CP1257_CS_AS", Encoding.CP1257),
  NOCASE_1257(154, "SQL_Latin1_General_CP1257_CI_AS", Encoding.CP1257),
  ETIDIC(155, "SQL_Estonian_CP1257_CS_AS", Encoding.CP1257),
  ETINC(156, "SQL_Estonian_CP1257_CI_AS", Encoding.CP1257),
  LVIDIC(157, "SQL_Latvian_CP1257_CS_AS", Encoding.CP1257),
  LVINC(158, "SQL_Latvian_CP1257_CI_AS", Encoding.CP1257),
  LTHDIC(159, "SQL_Lithuanian_CP1257_CS_AS", Encoding.CP1257),
  LTHNC(160, "SQL_Lithuanian_CP1257_CI_AS", Encoding.CP1257),

  DANNO_NOCASEPREF(183, "SQL_Danish_Pref_CP1_CI_AS", Encoding.CP1252),
  SVFI1_NOCASEPREF(184, "SQL_SwedishPhone_Pref_CP1_CI_AS", Encoding.CP1252),
  SVFI2_NOCASEPREF(185, "SQL_SwedishStd_Pref_CP1_CI_AS", Encoding.CP1252),
  ISLAN_NOCASEPREF(186, "SQL_Icelandic_Pref_CP1_CI_AS", Encoding.CP1252),

  BIN_CP932(192, "bin_cp932", Encoding.CP932),
  NLS_CP932(193, "nls_cp932", Encoding.CP932),
  BIN_CP949(194, "bin_cp949", Encoding.CP949),
  NLS_CP949(195, "nls_cp949", Encoding.CP949),
  BIN_CP950(196, "bin_cp950", Encoding.CP950),
  NLS_CP950(197, "nls_cp950", Encoding.CP950),
  BIN_CP936(198, "bin_cp936", Encoding.CP936),
  NLS_CP936(199, "nls_cp936", Encoding.CP936),
  NLS_CP932_CS(200, "nls_cp932_cs", Encoding.CP932),
  NLS_CP949_CS(201, "nls_cp949_cs", Encoding.CP949),
  NLS_CP950_CS(202, "nls_cp950_cs", Encoding.CP950),
  NLS_CP936_CS(203, "nls_cp936_cs", Encoding.CP936),
  BIN_CP874(204, "bin_cp874", Encoding.CP874),
  NLS_CP874(205, "nls_cp874", Encoding.CP874),
  NLS_CP874_CS(206, "nls_cp874_cs", Encoding.CP874),

  EBCDIC_037(210, "SQL_EBCDIC037_CP1_CS_AS", Encoding.CP1252),
  EBCDIC_273(211, "SQL_EBCDIC273_CP1_CS_AS", Encoding.CP1252),
  EBCDIC_277(212, "SQL_EBCDIC277_CP1_CS_AS", Encoding.CP1252),
  EBCDIC_278(213, "SQL_EBCDIC278_CP1_CS_AS", Encoding.CP1252),
  EBCDIC_280(214, "SQL_EBCDIC280_CP1_CS_AS", Encoding.CP1252),
  EBCDIC_284(215, "SQL_EBCDIC284_CP1_CS_AS", Encoding.CP1252),
  EBCDIC_285(216, "SQL_EBCDIC285_CP1_CS_AS", Encoding.CP1252),
  EBCDIC_297(217, "SQL_EBCDIC297_CP1_CS_AS", Encoding.CP1252);

  private final int id;
  public final String name;
  public final Encoding encoding;

  SortOrder(int id, String name, Encoding encoding) {
    this.id = id;
    this.name = name;
    this.encoding = encoding;
  }

  private static final IntObjectMap<SortOrder> sortOrdersById;

  static {
    SortOrder[] values = values();
    sortOrdersById = new IntObjectHashMap<>(values.length);
    for (SortOrder sortOrder : values) {
      sortOrdersById.put(sortOrder.id, sortOrder);
    }
  }

  public static SortOrder forId(int id) {
    SortOrder sortOrder = sortOrdersById.get(id);
    if (sortOrder == null) {
      throw new IllegalArgumentException("Unknown sort order: " + id);
    }
    return sortOrder;
  }

  @Override
  public String toString() {
    return name;
  }
}
